package eywa.projectcodex.components.viewScores

import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.database.Filters
import eywa.projectcodex.database.shootData.ArcherRoundsFilter

data class ViewScoresState(
        val isInMultiSelectMode: Boolean = false,
        val data: List<ViewScoresEntry> = listOf(),
        val noRoundsDialogOkClicked: Boolean = false,

        val multiSelectEmailClicked: Boolean = false,
        val multiSelectEmailNoSelection: Boolean = false,

        val lastClickedEntryId: Int? = null,
        /**
         * True if dropdown should be shown for entry [lastClickedEntryId]
         */
        val dropdownMenuOpen: Boolean = false,

        val convertScoreDialogOpen: Boolean = false,
        val deleteDialogOpen: Boolean = false,

        val openInputEndOnCompletedRound: Boolean = false,
        val openInputEndClicked: Boolean = false,
        val openScorePadClicked: Boolean = false,
        val openEmailClicked: Boolean = false,
        val openEditInfoClicked: Boolean = false,

        val filters: Filters<ArcherRoundsFilter> = Filters(),
) {
    val lastClickedEntry by lazy {
        lastClickedEntryId?.let { id -> data.find { it.id == id } }
    }
}
