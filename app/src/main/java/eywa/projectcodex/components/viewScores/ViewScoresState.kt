package eywa.projectcodex.components.viewScores

import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.utils.ViewScoresDropdownMenuItem

data class ViewScoresState(
        val isInMultiSelectMode: Boolean = false,
        val data: List<ViewScoresEntry> = listOf(),
        val personalBestArcherRoundIds: List<Int> = listOf(),
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
) {
    val lastClickedEntry by lazy {
        lastClickedEntryId?.let { id -> data.find { it.id == id } }
    }
}
