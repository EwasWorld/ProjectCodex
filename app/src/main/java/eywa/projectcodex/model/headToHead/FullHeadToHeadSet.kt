package eywa.projectcodex.model.headToHead

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGridRowData
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadDetail
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.Either

sealed class HeadToHeadNoResult {
    data object Incomplete : HeadToHeadNoResult()
    data object Unknown : HeadToHeadNoResult()
    data class Partial(val score: Int) : HeadToHeadNoResult()
}

sealed class RowArrows {
    abstract val total: Int
    abstract val arrowCount: Int

    data class Arrows(val arrows: List<Arrow>) : RowArrows() {
        override val total: Int
            get() = arrows.sumOf { it.score }
        override val arrowCount: Int
            get() = arrows.size
    }

    data class Total(override val total: Int, override val arrowCount: Int) : RowArrows()

    operator fun plus(other: RowArrows?): RowArrows =
            if (other == null) this
            else if (this is Arrows && other is Arrows) Arrows(arrows + other.arrows)
            else Total(total + other.total, arrowCount + other.arrowCount)

    val averageArrowScore
        get() = if (arrowCount == 0) null else (total.toFloat() / arrowCount)
}

data class FullHeadToHeadSet(
        val setNumber: Int,
        val data: List<HeadToHeadGridRowData>,
        val teamSize: Int,
        /**
         * Has a value if and only if the set is a shoot off, else null
         */
        val isShootOffWin: Boolean?,
        val isSetPointsFormat: Boolean,
        val endSize: Int,
) {
    init {
        check(data.distinctBy { it.type }.size == data.size) { "Duplicate types found" }
        check(teamSize > 0) { "Team size must be > 0" }
        check(
                // <= teamSize rather than 1 because in a team of 3 the OPPONENT size will be 3 (1 for each archer)
                !isShootOff || data.all { it.expectedArrowCount <= teamSize },
        ) { "expectedArrowCount for shoot off is incorrect" }
    }

    private val team = getTeamTotal()
    private val opponent = getOpponentTotal()
    val opponentEndScore
        get() = opponent.left
    val teamEndScore
        get() = team.left ?: team.right?.let { (it as? HeadToHeadNoResult.Partial)?.score }
    val result = result()
    val isShootOff
        get() = isShootOffWin != null

    fun arrowsShot(type: HeadToHeadArcherType) =
            if (result == HeadToHeadResult.INCOMPLETE) 0
            else type.expectedArrowCount(endSize = endSize, teamSize = teamSize)

    fun getArrows(type: HeadToHeadArcherType): RowArrows? {
        fun HeadToHeadGridRowData.asRowArrows(): RowArrows? =
                when {
                    !isComplete -> null
                    this is HeadToHeadGridRowData.Arrows -> RowArrows.Arrows(arrows)
                    else -> RowArrows.Total(totalScore, type.expectedArrowCount(endSize, teamSize))
                }

        val row = data.find { it.type == type }?.asRowArrows()
        if (row != null) return row

        if (type == HeadToHeadArcherType.SELF && teamSize == 1) {
            return data.find { it.type == HeadToHeadArcherType.TEAM }?.asRowArrows()
        }
        if (type != HeadToHeadArcherType.TEAM) return null

        val self = data.find { it.type == HeadToHeadArcherType.SELF }?.asRowArrows() ?: return null
        if (teamSize == 1) return self

        val teamMates = data.find { it.type == HeadToHeadArcherType.TEAM_MATE }?.asRowArrows() ?: return null
        return self + teamMates
    }

    val isComplete
        get() = result.isComplete || data.all { it.isComplete }

    val requiredRowsString: ResOrActual<String>?
        get() {
            if (result != HeadToHeadResult.UNKNOWN) return null

            val opponentRequired = opponent.right == HeadToHeadNoResult.Unknown
            return if (team.right != HeadToHeadNoResult.Unknown) {
                ResOrActual.StringResource(R.string.head_to_head_add_end__unknown_result_warning_opponent)
            }
            else {
                val types = data.map { it.type }
                val selfRequired = !types.contains(HeadToHeadArcherType.SELF)
                val teamMatesRequired = !types.contains(HeadToHeadArcherType.TEAM_MATE) && teamSize > 1

                val teamString = ResOrActual.StringResource(
                        if (!selfRequired) R.string.head_to_head_add_end__unknown_result_warning_team_mates
                        else if (!teamMatesRequired) R.string.head_to_head_add_end__unknown_result_warning_self
                        else R.string.head_to_head_add_end__unknown_result_warning_both,
                )

                if (opponentRequired) ResOrActual.StringResource(
                        R.string.head_to_head_add_end__unknown_result_warning_connector,
                        listOf(teamString),
                )
                else teamString
            }
        }

    fun showExtraColumnTotal() = data
            .map { it.type }
            .let { it.contains(HeadToHeadArcherType.SELF) && it.contains(HeadToHeadArcherType.TEAM_MATE) }

    private fun result(): HeadToHeadResult {
        val result = data.find { it.type == HeadToHeadArcherType.RESULT }
        if (result != null) {
            if (!isSetPointsFormat) throw IllegalStateException("Cannot give results for non-recurve style matches")
            if (!result.isComplete) return HeadToHeadResult.INCOMPLETE
            if (!result.isTotalRow) throw IllegalStateException("Result must be total row")

            val finalResult = getResult(result.totalScore)
            if (finalResult == HeadToHeadResult.TIE && isShootOff) {
                throw IllegalStateException("Shoot-off result cannot be a tie")
            }
            return finalResult
        }

        if (team.right == HeadToHeadNoResult.Unknown || opponent.right == HeadToHeadNoResult.Unknown) {
            return HeadToHeadResult.UNKNOWN
        }
        if (opponent.left == null || team.left == null) return HeadToHeadResult.INCOMPLETE
        return when {
            team.left!! > opponent.left!! -> HeadToHeadResult.WIN
            team.left!! < opponent.left!! -> HeadToHeadResult.LOSS
            isShootOff && isShootOffWin!! -> HeadToHeadResult.WIN
            isShootOff -> HeadToHeadResult.LOSS
            else -> HeadToHeadResult.TIE
        }
    }

    /**
     * [HeadToHeadArcherType.TEAM] takes precedence over [HeadToHeadArcherType.SELF] and [HeadToHeadArcherType.TEAM_MATE]
     *
     * @return total or no result
     */
    private fun getTeamTotal(): Either<Int, HeadToHeadNoResult> {
        val all = data.filter { it.type.isTeam }

        val team = all.find { it.type == HeadToHeadArcherType.TEAM }
        if (team != null) {
            return if (teamSize == 1) throw IllegalStateException()
            else if (!team.isComplete) Either.Right(HeadToHeadNoResult.Incomplete)
            else Either.Left(team.totalScore)
        }

        val self = all.find { it.type == HeadToHeadArcherType.SELF }
        if (teamSize == 1) {
            return if (self == null) Either.Right(HeadToHeadNoResult.Unknown)
            else if (!self.isComplete) Either.Right(HeadToHeadNoResult.Incomplete)
            else Either.Left(self.totalScore)
        }

        val teamMate = all.find { it.type == HeadToHeadArcherType.TEAM_MATE }
        return if (self == null || teamMate == null) Either.Right(HeadToHeadNoResult.Unknown)
        else if (!self.isComplete || !teamMate.isComplete) {
            if (!self.isComplete && !teamMate.isComplete) Either.Right(HeadToHeadNoResult.Incomplete)
            else if (self.isComplete) Either.Right(HeadToHeadNoResult.Partial(self.totalScore))
            else Either.Right(HeadToHeadNoResult.Partial(teamMate.totalScore))
        }
        else Either.Left(self.totalScore + teamMate.totalScore)
    }

    /**
     * @return total or no result
     */
    private fun getOpponentTotal(): Either<Int, HeadToHeadNoResult> {
        val opponent = data.find { it.type == HeadToHeadArcherType.OPPONENT }
                ?: return Either.Right(HeadToHeadNoResult.Unknown)
        if (!opponent.isComplete) return Either.Right(HeadToHeadNoResult.Incomplete)
        return Either.Left(opponent.totalScore)
    }

    fun asDatabaseDetails(shootId: Int, matchNumber: Int) =
            DatabaseHeadToHeadDetail(
                    shootId = shootId,
                    matchNumber = matchNumber,
                    setNumber = setNumber,

                    // Dummy values
                    type = HeadToHeadArcherType.TEAM,
                    isTotal = false,
                    headToHeadArrowScoreId = 0,
                    arrowNumber = 0,
                    score = 0,
                    isX = false,
            ).let { mainData ->
                data.flatMap { rowData ->
                    val typeData = mainData.copy(
                            type = rowData.type,
                            isTotal = rowData.isTotalRow,
                    )

                    when (rowData) {
                        is HeadToHeadGridRowData.Arrows -> {
                            rowData.arrows.mapIndexed { index, arrow ->
                                typeData.copy(
                                        headToHeadArrowScoreId = rowData.dbIds?.getOrNull(index) ?: 0,
                                        arrowNumber = index + 1,
                                        score = arrow.score,
                                        isX = arrow.isX,
                                )
                            }
                        }

                        is HeadToHeadGridRowData.Total ->
                            listOf(
                                    typeData.copy(
                                            headToHeadArrowScoreId = rowData.dbId ?: 0,
                                            arrowNumber = 1,
                                            score = rowData.total!!,
                                            isX = false,
                                    ),
                            )

                        is HeadToHeadGridRowData.EditableTotal ->
                            listOf(
                                    typeData.copy(
                                            headToHeadArrowScoreId = rowData.dbId ?: 0,
                                            arrowNumber = 1,
                                            score = rowData.total!!,
                                            isX = false,
                                    ),
                            )
                    }
                }
            }

    companion object {
        fun getResult(key: Int) =
                when (key) {
                    0 -> HeadToHeadResult.LOSS
                    1 -> HeadToHeadResult.TIE
                    2 -> HeadToHeadResult.WIN
                    else -> throw IllegalStateException("Points must be 0, 1, or 2")
                }
    }
}
