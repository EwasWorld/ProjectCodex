package eywa.projectcodex.components.viewScores

import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.ui.filters.ViewScoresFiltersState
import eywa.projectcodex.database.Filters
import eywa.projectcodex.database.shootData.ShootFilter

data class ViewScoresState(
        val isInMultiSelectMode: Boolean = false,
        val data: List<ViewScoresEntry>? = null,
        val noRoundsDialogOkClicked: Boolean = false,

        val multiSelectEmailNoSelection: Boolean = false,

        val lastClickedEntryId: Int? = null,
        /**
         * True if dropdown should be shown for entry [lastClickedEntryId]
         */
        val dropdownMenuOpen: Boolean = false,

        val convertScoreDialogOpen: Boolean = false,
        val deleteDialogOpen: Boolean = false,

        val openAddEndOnCompletedRound: Boolean = false,
        val openAddEndClicked: Boolean = false,
        val openAddCountClicked: Boolean = false,
        val openScorePadClicked: Boolean = false,
        val openEmailClicked: Boolean = false,
        val openEditInfoClicked: Boolean = false,

        val viewScoresFiltersState: ViewScoresFiltersState = ViewScoresFiltersState(),
) {
    val lastClickedEntry by lazy {
        lastClickedEntryId?.let { id -> data?.find { it.id == id } }
    }

    val filters: Filters<ShootFilter>
        get() = viewScoresFiltersState.filters

    val actionBarExtended
        get() = isInMultiSelectMode || viewScoresFiltersState.isExpanded

    val showNoItemsDialog: Boolean
        get() = data != null && data.isEmpty() && filters.size == 0
}
