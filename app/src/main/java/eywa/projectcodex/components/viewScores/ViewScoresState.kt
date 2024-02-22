package eywa.projectcodex.components.viewScores

import eywa.projectcodex.components.viewScores.actionBar.filters.ViewScoresFiltersState
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.database.Filters
import eywa.projectcodex.database.shootData.ShootFilter

data class ViewScoresState(
        val isInMultiSelectMode: Boolean = false,
        val rawData: Pair<List<ViewScoresEntry>, Filters<ShootFilter>>? = null,
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

        val filtersState: ViewScoresFiltersState = ViewScoresFiltersState(),
) {
    val lastClickedEntry by lazy {
        lastClickedEntryId?.let { id -> data?.find { it.id == id } }
    }

    val data = rawData?.first

    val filters = filtersState.filters

    val actionBarExtended
        get() = isInMultiSelectMode || filtersState.isExpanded

    val isLoading: Boolean
        get() = rawData == null || (rawData.first.isEmpty() && filters.size == 0 && rawData.second != filters)

    val showNoItemsDialog
        get() =
            rawData != null && data!!.isEmpty()
                    && filters.size == 0 // A softer error is shown if filters cause the no data
                    && rawData.second == filters // Data is not out of date
}
