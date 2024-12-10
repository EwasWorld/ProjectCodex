package eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd

import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsError
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowDataPreviewHelper
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridState
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadDetail
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeat
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeatPreviewHelper
import eywa.projectcodex.model.SightMark
import eywa.projectcodex.model.headToHead.FullHeadToHeadSet

data class HeadToHeadAddEndState(
        val roundInfo: HeadToHeadRoundInfo? = null,
        val extras: HeadToHeadAddEndExtras = HeadToHeadAddEndExtras(),
        val teamRunningTotal: Int? = null,
        val opponentRunningTotal: Int? = null,
        val isRecurveStyle: Boolean = true,
        val heat: DatabaseHeadToHeadHeat = DatabaseHeadToHeadHeatPreviewHelper.data,
        val editingSet: FullHeadToHeadSet? = null,
) {
    init {
        if (editingSet != null) {
            check(extras.set.setNumber == editingSet.setNumber)
        }
    }

    fun toGridState() = HeadToHeadGridState.SingleEditable(
            enteredArrows = listOf(extras.set),
            selected = extras.selected,
    )

    fun toDbDetails(): List<DatabaseHeadToHeadDetail> = extras.set.asDatabaseDetails(heat.shootId, heat.matchNumber)
}

data class HeadToHeadRoundInfo(
        val distance: Int? = null,
        val isMetric: Boolean? = null,
        val sightMark: SightMark? = null,

        val round: Round? = null,
        val face: RoundFace? = null,
)

data class HeadToHeadAddEndExtras(
        val selectRowTypesDialogState: Map<HeadToHeadArcherType, Boolean>? = null,
        val openAddHeatScreen: Boolean = false,
        val openEditSightMark: Boolean = false,
        val openAllSightMarks: Boolean = false,
        val set: FullHeadToHeadSet = FullHeadToHeadSet(
                data = HeadToHeadGridRowDataPreviewHelper.selfAndOpponent,
                teamSize = 1,
                isShootOffWin = false,
                setNumber = 1,
                isRecurveStyle = true,
        ),
        val selected: HeadToHeadArcherType? = set.data.map { it.type }.minByOrNull { it.ordinal },
        val arrowInputsError: Set<ArrowInputsError> = setOf(),
        val incompleteError: Boolean = false,
        val openSighters: Boolean = false,
        val openCreateNextMatch: Boolean = false,
) {
    val selectedData
        get() = set.data.find { it.type == selected }
}
