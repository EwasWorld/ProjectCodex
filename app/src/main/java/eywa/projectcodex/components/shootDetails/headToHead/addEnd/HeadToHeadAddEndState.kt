package eywa.projectcodex.components.shootDetails.headToHead.addEnd

import eywa.projectcodex.common.sharedUi.previewHelpers.HeadToHeadMatchPreviewHelperDsl
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsError
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGridRowDataPreviewHelper
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGridState
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadMatch
import eywa.projectcodex.model.SightMark
import eywa.projectcodex.model.headToHead.FullHeadToHeadSet

data class HeadToHeadAddEndState(
        val roundInfo: HeadToHeadRoundInfo = HeadToHeadRoundInfo(),
        val extras: HeadToHeadAddEndExtras = HeadToHeadAddEndExtras(),
        val teamRunningTotal: Int? = null,
        val opponentRunningTotal: Int? = null,
        val isRecurveStyle: Boolean = true,
        val match: DatabaseHeadToHeadMatch = HeadToHeadMatchPreviewHelperDsl.data,
        val editingSet: FullHeadToHeadSet? = null,
        val isInserting: Boolean = false,
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

    fun setToDbDetails() = extras.set.asDatabaseDetails(match.shootId, match.matchNumber)
    fun editingToDbDetails() = editingSet?.asDatabaseDetails(match.shootId, match.matchNumber)
}

data class HeadToHeadRoundInfo(
        val distance: Int? = null,
        val isMetric: Boolean? = null,
        val sightMark: SightMark? = null,

        val round: Round? = null,
        val face: RoundFace? = null,
        val endSize: Int = 3,
        val isStandardFormat: Boolean = true,
)

data class HeadToHeadAddEndExtras(
        val selectRowTypesDialogState: Map<HeadToHeadArcherType, Boolean>? = null,
        val selectRowTypesDialogUnknownWarning: ResOrActual<String>? = null,
        val openAddMatchScreen: Boolean = false,
        val openEditSightMark: Boolean = false,
        val openAllSightMarks: Boolean = false,
        val set: FullHeadToHeadSet = FullHeadToHeadSet(
                data = HeadToHeadGridRowDataPreviewHelper.selfAndOpponent,
                teamSize = 1,
                setNumber = 1,
                isSetPointsFormat = true,
                endSize = 3,
        ),
        val selected: HeadToHeadArcherType? = set.data.map { it.type }.minByOrNull { it.ordinal },
        val arrowInputsError: Set<ArrowInputsError> = setOf(),
        val incompleteError: Boolean = false,
        val openSighters: Boolean = false,
        val openCreateNextMatch: Boolean = false,
        val pressBack: Boolean = false,
) {
    val selectedData
        get() = set.data.find { it.type == selected }
}
