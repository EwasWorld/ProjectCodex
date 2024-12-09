package eywa.projectcodex.common.sharedUi.previewHelpers

import eywa.projectcodex.common.utils.CodexPreviewHelperDsl
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowData
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowDataPreviewHelper
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHead
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeat
import eywa.projectcodex.model.headToHead.FullHeadToHead
import eywa.projectcodex.model.headToHead.FullHeadToHeadHeat
import eywa.projectcodex.model.headToHead.FullHeadToHeadSet

@CodexPreviewHelperDsl
class HeadToHeadPreviewHelperDsl(shootId: Int) {
    var headToHead = DatabaseHeadToHead(
            shootId = shootId,
            isRecurveStyle = true,
            isStandardFormat = true,
            teamSize = 1,
            qualificationRank = null,
    )
    private var heats = listOf<FullHeadToHeadHeat>()

    fun addHeat(config: HeadToHeadHeatPreviewHelperDsl.() -> Unit) {
        heats = heats + HeadToHeadHeatPreviewHelperDsl(
                matchNumber = heats.size + 1,
                shootId = headToHead.shootId,
                teamSize = headToHead.teamSize,
                isRecurveStyle = headToHead.isRecurveStyle,
                isStandardFormat = headToHead.isStandardFormat,
                heat = heats.mapNotNull { it.heat.heat }.minOrNull()?.minus(1) ?: 3,
        ).apply(config).asFull()

        require(heats.distinctBy { it.heat.heat }.size == heats.size) { "Duplicate heat" }
    }

    fun asFull() = FullHeadToHead(headToHead = headToHead, heats = heats)
}

@CodexPreviewHelperDsl
class HeadToHeadHeatPreviewHelperDsl(
        shootId: Int,
        matchNumber: Int,
        val teamSize: Int,
        val isRecurveStyle: Boolean,
        val isStandardFormat: Boolean,
        heat: Int = 3,
) {
    var heat = DatabaseHeadToHeadHeat(
            matchNumber = matchNumber,
            shootId = shootId,
            heat = heat,
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
                isRecurveStyle = isRecurveStyle,
        ).apply(config).asFull()

        require(
                !set.isShootOff || (set.result == HeadToHeadResult.WIN) == heat.isShootOffWin,
        ) { "isShootOffWin mismatch" }
        require(sets.distinctBy { it.setNumber }.size == sets.size) { "Duplicate setNumber" }

        sets = sets + set
    }

    fun asFull() =
            FullHeadToHeadHeat(
                    heat = heat,
                    sets = sets,
                    teamSize = teamSize,
                    isRecurveStyle = isRecurveStyle,
                    isStandardFormat = isStandardFormat,
            )
}

@CodexPreviewHelperDsl
class HeadToHeadSetPreviewHelperDsl(
        private val setNumber: Int,
        private val teamSize: Int,
        private val isShootOffWin: Boolean,
        private val isRecurveStyle: Boolean,
) {
    private var data = listOf<HeadToHeadGridRowData>()
        set(value) {
            check(value.distinctBy { it.type }.size == value.size) { "Duplicate type" }
            field = value
        }

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

        val isShootOff = setNumber == HeadToHeadUseCase.shootOffSet(teamSize)
        val self = selfScore ?: (if (result == HeadToHeadResult.LOSS) loserScore else winnerScore)

        if (result == HeadToHeadResult.UNKNOWN) {
            HeadToHeadGridRowDataPreviewHelper.create(
                    teamSize = teamSize,
                    isShootOff = isShootOff,
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
                    isRecurveStyle = isRecurveStyle,
            )
}
