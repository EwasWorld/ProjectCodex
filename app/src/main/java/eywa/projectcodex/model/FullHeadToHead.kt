package eywa.projectcodex.model

import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowData
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHead
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadDetail
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeat

data class FullHeadToHead(
        val headToHead: DatabaseHeadToHead,
        val heats: List<FullHeadToHeadHeat>,
) {
    constructor(
            headToHead: DatabaseHeadToHead,
            heats: List<DatabaseHeadToHeadHeat>,
            details: List<DatabaseHeadToHeadDetail>,
            isEditable: Boolean,
    ) : this(
            headToHead = headToHead,
            heats = details.groupBy { it.heat }.let { grouped ->
                heats.map { heat ->
                    val shootOffSet = HeadToHeadUseCase.shootOffSet(headToHead.teamSize)
                    FullHeadToHeadHeat(
                            heat = heat,
                            sets = (grouped[heat.heat] ?: emptyList())
                                    .groupBy { it.setNumber }
                                    .map { (setNumber, details) ->
                                        val isShootOff = setNumber == shootOffSet
                                        FullHeadToHeadSet(
                                                setNumber = setNumber,
                                                data = details.asRowData(
                                                        endSize = if (isShootOff) 1 else HeadToHeadUseCase.END_SIZE,
                                                        teamSize = headToHead.teamSize,
                                                        isEditable = isEditable,
                                                ),
                                                isShootOff = isShootOff,
                                                teamSize = headToHead.teamSize,
                                                isShootOffWin = heat.isShootOffWin,
                                        )
                                    },
                            teamSize = headToHead.teamSize,
                            isRecurveMatch = headToHead.isRecurveStyle,
                    )
                }
            },
    )

    val hasStarted: Boolean
        get() = heats.any { it.hasStarted }

    val arrowsShot: Int
        get() = heats.sumOf { it.arrowsShot }

    val isComplete: Boolean
        get() = heats.all { it.isComplete() }

    companion object {
        fun List<DatabaseHeadToHeadDetail>.asRowData(
                endSize: Int,
                teamSize: Int,
                isEditable: Boolean,
        ) = groupBy { it.type }
                .map { (type, group) ->
                    val expectedArrowCount = type.expectedArrowCount(endSize, teamSize)
                    require(group.distinctBy { it.isTotal }.size == 1) { "Cannot have total and arrows" }

                    if (group[0].isTotal) {
                        require(group.size == 1) { "Cannot have more than one total" }
                        if (isEditable) {
                            HeadToHeadGridRowData.EditableTotal(type, expectedArrowCount)
                        }
                        else {
                            HeadToHeadGridRowData.Total(type, expectedArrowCount, group[0].score)
                        }
                    }
                    else {
                        HeadToHeadGridRowData.Arrows(type, expectedArrowCount, group.map { Arrow(it.score, it.isX) })
                    }
                }
    }
}

data class FullHeadToHeadHeat(
        val heat: DatabaseHeadToHeadHeat,
        val sets: List<FullHeadToHeadSet>,
        val teamSize: Int,
        val isRecurveMatch: Boolean,
) {
    val results = runningTotals()

    val hasStarted: Boolean
        get() = sets.isNotEmpty()

    val arrowsShot: Int
        get() = sets.sumOf { it.arrowsShot }

    /**
     * @return for each set in [sets], provide cumulative team/opponent score.
     * null if this or any previous set is incomplete
     */
    private fun runningTotals(): List<Pair<Int, Int>?> {
        var hasRunningTotal = true
        var teamScore = 0
        var opponentScore = 0
        return sets.mapIndexed { _, set ->
            val isShootOff = set.isShootOff
            val result = set.result
            hasRunningTotal = hasRunningTotal && result != HeadToHeadResult.INCOMPLETE

            if (!hasRunningTotal) {
                null
            }
            else {
                teamScore +=
                        if (isRecurveMatch && isShootOff) result.shootOffPoints
                        else if (isRecurveMatch) result.defaultPoints
                        else set.teamSetScore ?: 0

                opponentScore +=
                        if (isRecurveMatch && isShootOff) result.opposite().shootOffPoints
                        else if (isRecurveMatch) result.opposite().defaultPoints
                        else set.opponentSetScore ?: 0
                teamScore to opponentScore
            }
        }
    }

    fun isComplete(): Boolean {
        if (results.last() == null) return false

        // Recurve
        if (isRecurveMatch) {
            val winScore = HeadToHeadUseCase.winScore(teamSize)
            return results.last()
                    .let { scores -> scores != null && (scores.first >= winScore || scores.second >= winScore) }
        }

        // Compound
        val shootOffSet = HeadToHeadUseCase.shootOffSet(teamSize)
        if (sets.size == shootOffSet) return true
        if (sets.size < shootOffSet - 1) return false
        return results.last().let { scores -> scores != null && (scores.first != scores.second) }
    }
}

data class FullHeadToHeadSet(
        val setNumber: Int,
        val data: List<HeadToHeadGridRowData>,
        val isShootOff: Boolean,
        val teamSize: Int,
        val isShootOffWin: Boolean,
) {
    val endSize
        get() = if (isShootOff) 1 else HeadToHeadUseCase.END_SIZE

    init {
        check(data.distinctBy { it.type }.size == data.size) { "Duplicate types found" }
    }

    val opponentSetScore = data
            .find { it.type == HeadToHeadArcherType.OPPONENT }
            ?.takeIf { it.isComplete }
            ?.totalScore
    val teamSetScore = getTeamTotal(teamSize)
    val result = result(isShootOff, isShootOffWin)

    val arrowsShot: Int
        get() =
            data.find { it.type == HeadToHeadArcherType.SELF }?.arrowsShot
                    ?: data.find { it.type == HeadToHeadArcherType.TEAM }
                            ?.takeIf { it.isComplete }
                            ?.totalScore
                    ?: 0

    fun showExtraColumnTotal() = data
            .map { it.type }
            .let {
                it.contains(HeadToHeadArcherType.SELF) && it.contains(HeadToHeadArcherType.TEAM_MATE)
            }

    private fun result(isShootOff: Boolean, isShootOffWin: Boolean): HeadToHeadResult {
        val points = data
                .find { it.type == HeadToHeadArcherType.RESULT }
                ?.takeIf { it.isComplete }
                ?.totalScore

        if (points != null) {
            check(points in 0..2) { "Points must be 0, 1, or 2" }
            return HeadToHeadResult.defaultPointsBackwardsMap[points]!!
        }

        if (opponentSetScore == null || teamSetScore == null) return HeadToHeadResult.INCOMPLETE
        return when {
            teamSetScore > opponentSetScore -> HeadToHeadResult.WIN
            teamSetScore < opponentSetScore -> HeadToHeadResult.LOSS
            isShootOff && isShootOffWin -> HeadToHeadResult.WIN
            isShootOff -> HeadToHeadResult.LOSS
            else -> HeadToHeadResult.TIE
        }
    }

    private fun getTeamTotal(teamSize: Int): Int? {
        check(teamSize > 0) { "Team size must be > 0" }

        val all = data.filter { it.type.isTeam }
        // Individuals should use SELF
        check(teamSize > 1 || all.map { it.type } == listOf(HeadToHeadArcherType.SELF))

        val team = all.find { it.type == HeadToHeadArcherType.TEAM }
        if (team != null && team.isComplete) {
            return team.totalScore
        }

        // Individual, already checked they use SELF
        if (teamSize == 1) {
            return if (all[0].isComplete) all[0].totalScore else null
        }

        // No team total, not an individual, therefore requires SELF and TEAM_MATES
        if (all.size < 2 || all.any { !it.isComplete }) return null

        return all.sumOf { it.totalScore }
    }
}
