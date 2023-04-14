package eywa.projectcodex.components.viewScores

import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.utils.ViewScoresDropdownMenuItem
import eywa.projectcodex.database.Filters
import eywa.projectcodex.database.archerRound.ArcherRoundsFilter

data class ViewScoresState(
        val isInMultiSelectMode: Boolean = false,
        val data: List<ViewScoresEntry> = listOf(),
        val noRoundsDialogOkClicked: Boolean = false,

        val multiSelectEmailClicked: Boolean = false,
        val multiSelectEmailNoSelection: Boolean = false,

        val lastClickedEntryId: Int? = null,
        /**
         * The [ViewScoresDropdownMenuItem] that should be shown for entry [lastClickedEntryId].
         * Empty/null if no dropdown should be shown
         */
        val dropdownItems: List<ViewScoresDropdownMenuItem>? = null,

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
