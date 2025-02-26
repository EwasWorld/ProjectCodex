package eywa.projectcodex.common.sharedUi.previewHelpers

import eywa.projectcodex.common.utils.CodexPreviewHelperDsl
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGridRowData
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGridRowDataPreviewHelper
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHead
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadMatch
import eywa.projectcodex.database.shootData.headToHead.setShootOffResult
import eywa.projectcodex.model.headToHead.FullHeadToHead
import eywa.projectcodex.model.headToHead.FullHeadToHeadMatch
import eywa.projectcodex.model.headToHead.FullHeadToHeadSet

@CodexPreviewHelperDsl
class HeadToHeadPreviewHelperDsl(shootId: Int) {
    var headToHead = DatabaseHeadToHead(
            shootId = shootId,
            isSetPointsFormat = true,
            endSize = null,
            teamSize = 1,
            qualificationRank = null,
            totalArchers = null,
    )
    private var matches = listOf<FullHeadToHeadMatch>()

    fun addMatch(config: HeadToHeadMatchPreviewHelperDsl.() -> Unit) {
        matches = matches + HeadToHeadMatchPreviewHelperDsl(
                matchNumber = matches.size + 1,
                shootId = headToHead.shootId,
                teamSize = headToHead.teamSize,
                isRecurveStyle = headToHead.isSetPointsFormat,
                endSize = headToHead.endSize,
                heat = matches.mapNotNull { it.match.heat }.minOrNull()?.minus(1) ?: 3,
        ).apply(config).asFull()

        require(matches.distinctBy { it.match.heat }.size == matches.size) { "Duplicate heat" }
    }

    fun asFull() = FullHeadToHead(headToHead = headToHead, matches = matches)
}

@CodexPreviewHelperDsl
class HeadToHeadMatchPreviewHelperDsl(
        shootId: Int,
        matchNumber: Int,
        val teamSize: Int,
        val isRecurveStyle: Boolean,
        val endSize: Int?,
        heat: Int = 3,
) {
    var match = DatabaseHeadToHeadMatch(
            matchNumber = matchNumber,
            shootId = shootId,
            heat = heat,
            opponent = null,
            opponentQualificationRank = null,
            shootOffSets = mapOf(),
            sightersCount = 0,
            isBye = false,
            maxPossibleRank = 1,
    )
    private var sets = listOf<FullHeadToHeadSet>()

    fun addSet(config: HeadToHeadSetPreviewHelperDsl.() -> Unit) {
        val setNumber = sets.size + 1
        val shootOffResult = match.shootOffSets[setNumber]

        val set = HeadToHeadSetPreviewHelperDsl(
                setNumber = setNumber,
                teamSize = teamSize,
                isShootOffWin = shootOffResult ?: false,
                isRecurveStyle = isRecurveStyle,
                endSize = endSize,
        ).apply(config).asFull()

        require(
                !set.isShootOff || (set.result == HeadToHeadResult.WIN) == set.isShootOffWin,
        ) { "isShootOffWin mismatch" }
        require(sets.distinctBy { it.setNumber }.size == sets.size) { "Duplicate setNumber" }

        match = match.copy(shootOffSets = match.shootOffSets.setShootOffResult(setNumber, set.isShootOffWin))

        sets = sets + set
    }

    fun asFull() =
            FullHeadToHeadMatch(
                    match = match,
                    sets = sets,
                    teamSize = teamSize,
                    isSetPointsFormat = isRecurveStyle,
                    isStandardFormat = endSize == null,
            )

    companion object {
        val data = DatabaseHeadToHeadMatch(
                shootId = 1,
                matchNumber = 1,
                heat = 1,
                opponent = "Jessica Summers",
                opponentQualificationRank = 1,
                shootOffSets = mapOf(),
                sightersCount = 6,
                isBye = false,
                maxPossibleRank = 1,
        )
    }
}

@CodexPreviewHelperDsl
class HeadToHeadSetPreviewHelperDsl(
        private val setNumber: Int,
        private val teamSize: Int,
        private val isShootOffWin: Boolean?,
        private val isRecurveStyle: Boolean,
        endSize: Int?,
) {
    private var data = listOf<HeadToHeadGridRowData>()
        set(value) {
            check(value.distinctBy { it.type }.size == value.size) { "Duplicate type" }
            field = value
        }
    val isShootOff
        get() = isShootOffWin != null

    private val actualEndSize = endSize ?: HeadToHeadUseCase.endSize(teamSize = teamSize, isShootOff = isShootOff)

    fun addRows(
            result: HeadToHeadResult = HeadToHeadResult.WIN,
            typesToIsTotal: Map<HeadToHeadArcherType, Boolean> = mapOf(
                    HeadToHeadArcherType.TEAM to false,
                    HeadToHeadArcherType.OPPONENT to true,
            ),
            isEditable: Boolean = false,
            winnerScore: Int = 30,
            loserScore: Int = 20,
            selfScore: Int? = null,
            dbIds: List<List<Int>>? = null,
    ) {
        if (typesToIsTotal.isEmpty()) return

        val self = selfScore ?: (if (result == HeadToHeadResult.LOSS) loserScore else winnerScore)

        if (result == HeadToHeadResult.UNKNOWN) {
            HeadToHeadGridRowDataPreviewHelper.create(
                    teamSize = teamSize,
                    isShootOff = isShootOff,
                    endSize = actualEndSize,
                    result = HeadToHeadResult.INCOMPLETE,
                    typesToIsTotal = typesToIsTotal
                            .minus(HeadToHeadArcherType.RESULT)
                            .minus(HeadToHeadArcherType.OPPONENT),
                    isEditable = isEditable,
                    winnerScore = winnerScore,
                    loserScore = loserScore,
                    selfScore = self,
            ).let { data = data + it }

            return
        }

        HeadToHeadGridRowDataPreviewHelper.create(
                teamSize = teamSize,
                isShootOff = isShootOff,
                result = result,
                typesToIsTotal = typesToIsTotal,
                isEditable = isEditable,
                winnerScore = winnerScore,
                loserScore = loserScore,
                selfScore = self,
                dbIds = dbIds,
        ).let { data = data + it }
    }

    fun removeRow(type: HeadToHeadArcherType) {
        data = data.filter { it.type == type }
    }

    fun asFull() =
            FullHeadToHeadSet(
                    setNumber = setNumber,
                    data = data,
                    teamSize = teamSize,
                    isShootOffWin = isShootOffWin,
                    isSetPointsFormat = isRecurveStyle,
                    endSize = actualEndSize,
            )
}
