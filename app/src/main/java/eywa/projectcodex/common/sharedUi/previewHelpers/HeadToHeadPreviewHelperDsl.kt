package eywa.projectcodex.common.sharedUi.previewHelpers

import eywa.projectcodex.common.utils.CodexPreviewHelperDsl
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowData
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowDataPreviewHelper
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHead
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeat
import eywa.projectcodex.model.FullHeadToHead
import eywa.projectcodex.model.FullHeadToHeadHeat
import eywa.projectcodex.model.FullHeadToHeadSet

@CodexPreviewHelperDsl
class HeadToHeadPreviewHelperDsl(shootId: Int) {
    var headToHead =
            DatabaseHeadToHead(shootId = shootId, isRecurveStyle = true, teamSize = 1, qualificationRank = null)
    private var heats = listOf<FullHeadToHeadHeat>()

    fun addHeat(config: HeadToHeadHeatPreviewHelperDsl.() -> Unit) {
        heats = heats + HeadToHeadHeatPreviewHelperDsl(
                shootId = headToHead.shootId,
                teamSize = headToHead.teamSize,
                isRecurveStyle = headToHead.isRecurveStyle,
        ).apply(config).asFull()

        require(heats.distinctBy { it.heat.heat }.size == heats.size) { "Duplicate heat" }
    }

    fun asFull() = FullHeadToHead(headToHead = headToHead, heats = heats)
}

@CodexPreviewHelperDsl
class HeadToHeadHeatPreviewHelperDsl(
        shootId: Int,
        private val teamSize: Int,
        private val isRecurveStyle: Boolean,
) {
    var heat = DatabaseHeadToHeadHeat(
            shootId = shootId,
            heat = 3,
            opponent = null,
            opponentQualificationRank = null,
            isShootOffWin = false,
            sightersCount = 0,
            isBye = false,
    )
    private var sets = listOf<FullHeadToHeadSet>()

    fun addSet(config: HeadToHeadSetPreviewHelperDsl.() -> Unit) {
        val set = HeadToHeadSetPreviewHelperDsl(
                setNumber = sets.size + 1,
                teamSize = teamSize,
                isShootOffWin = heat.isShootOffWin,
        ).apply(config).asFull()
        require(!set.isShootOff || (set.result == HeadToHeadResult.WIN) == heat.isShootOffWin) { "isShootOffWin mismatch" }
        sets = sets + set
    }

    fun asFull() =
            FullHeadToHeadHeat(
                    heat = heat,
                    sets = sets,
                    teamSize = teamSize,
                    isRecurveMatch = isRecurveStyle,
            )
}

@CodexPreviewHelperDsl
class HeadToHeadSetPreviewHelperDsl(
        private val setNumber: Int,
        private val teamSize: Int,
        private val isShootOffWin: Boolean,
) {
    private val isShootOff = HeadToHeadUseCase.shootOffSet(teamSize) == setNumber
    private var data = listOf<HeadToHeadGridRowData>()

    fun addRows(
            result: HeadToHeadResult = HeadToHeadResult.WIN,
            typesToIsTotal: Map<HeadToHeadArcherType, Boolean> = mapOf(
                    HeadToHeadArcherType.SELF to false,
                    HeadToHeadArcherType.OPPONENT to true,
            ),
            isEditable: Boolean = false,
            winnerScore: Int,
            loserScore: Int,
            selfScore: Int,
    ) {
        require(result != HeadToHeadResult.INCOMPLETE) { "Cannot have an incomplete set" }

        HeadToHeadGridRowDataPreviewHelper.create(
                teamSize = teamSize,
                isShootOff = isShootOff,
                result = result,
                typesToIsTotal = typesToIsTotal,
                isEditable = isEditable,
                winnerScore = winnerScore,
                loserScore = loserScore,
                selfScore = selfScore,
        ).let { data = data + it }
    }

    fun asFull() =
            FullHeadToHeadSet(
                    setNumber = setNumber,
                    data = data,
                    isShootOff = isShootOff,
                    teamSize = teamSize,
                    isShootOffWin = isShootOffWin,
            )
}
