package eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsError
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowData
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowDataPreviewHelper
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridState
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadDetail
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeat
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeatPreviewHelper
import eywa.projectcodex.model.FullHeadToHeadSet
import eywa.projectcodex.model.SightMark

data class HeadToHeadAddEndState(
        val headToHeadRoundInfo: HeadToHeadRoundInfo? = null,
        val extras: HeadToHeadAddEndExtras = HeadToHeadAddEndExtras(),
        val teamRunningTotal: Int = 0,
        val opponentRunningTotal: Int = 2,
        val isRecurveStyle: Boolean = true,
        val heat: DatabaseHeadToHeadHeat = DatabaseHeadToHeadHeatPreviewHelper.data,
        val dbSet: FullHeadToHeadSet? = null,
) {
    init {
        if (dbSet != null) {
            check(extras.set.setNumber == dbSet.setNumber)
        }
    }

    fun toGridState() = HeadToHeadGridState(
            enteredArrows = listOf(extras.set),
            selected = extras.selected,
            isSingleEditableSet = true,
            runningTotals = null,
            finalResult = null,
    )

    fun toDbDetails(): List<DatabaseHeadToHeadDetail> =
            DatabaseHeadToHeadDetail(
                    headToHeadArrowScoreId = 0,
                    shootId = heat.shootId,
                    heat = heat.heat,
                    setNumber = extras.set.setNumber,

                    // Dummy values
                    type = HeadToHeadArcherType.TEAM,
                    isTotal = false,
                    arrowNumber = 0,
                    score = 0,
                    isX = false,
            ).let { mainData ->
                extras.set.data.flatMap { rowData ->
                    val typeData = mainData.copy(
                            type = rowData.type,
                            isTotal = rowData.isTotalRow,
                    )

                    if (rowData is HeadToHeadGridRowData.Arrows) {
                        rowData.arrows.mapIndexed { index, arrow ->
                            typeData.copy(arrowNumber = index + 1, score = arrow.score, isX = arrow.isX)
                        }
                    }
                    else {
                        listOf(typeData.copy(arrowNumber = 1, score = rowData.totalScore, isX = false))
                    }
                }
            }
}

data class HeadToHeadRoundInfo(
        val distance: Int? = 70,
        val isMetric: Boolean? = true,
        val sightMark: SightMark? = null,

        val round: Round? = RoundPreviewHelper.wa70RoundData.round,
        val face: RoundFace? = RoundFace.FULL,
)

data class HeadToHeadAddEndExtras(
        val openAddHeatScreen: Boolean = false,
        val openEditSightMark: Boolean = false,
        val openAllSightMarks: Boolean = false,
        val set: FullHeadToHeadSet = FullHeadToHeadSet(
                data = HeadToHeadGridRowDataPreviewHelper.selfAndOpponent,
                teamSize = 1,
                isShootOff = false,
                isShootOffWin = false,
                setNumber = 1,
        ),
        val selected: HeadToHeadArcherType? = set.data.map { it.type }.minByOrNull { it.ordinal },
        val arrowInputsError: ArrowInputsError? = null,
        val incompleteError: Boolean = false,
        val openSighters: Boolean = false,
)
