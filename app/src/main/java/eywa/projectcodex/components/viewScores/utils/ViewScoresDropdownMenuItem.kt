package eywa.projectcodex.components.viewScores.utils

import androidx.annotation.StringRes
import eywa.projectcodex.R
import eywa.projectcodex.common.diActivityHelpers.ArcherRoundIdsUseCase
import eywa.projectcodex.components.viewScores.ViewScoresState
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry

enum class ViewScoresDropdownMenuItem(
        @StringRes val title: Int,
        val handleClick: ViewScoresState.(archerRoundIdsUseCase: ArcherRoundIdsUseCase) -> ViewScoresState,
        /**
         * Only display this dropdown menu item if this returns true
         */
        val shouldShow: ((ViewScoresEntry) -> Boolean)? = null,
) {
    SCORE_PAD(
            title = R.string.view_scores_menu__score_pad,
            handleClick = { copy(openScorePadClicked = true, dropdownMenuOpen = false) }
    ),
    CONTINUE(
            title = R.string.view_scores_menu__continue,
            handleClick = {
                if (data.find { it.id == lastClickedEntryId }?.isRoundComplete() == true) {
                    copy(openInputEndOnCompletedRound = true)
                }
                else {
                    copy(openInputEndClicked = true, dropdownMenuOpen = false)
                }
            },
            shouldShow = { entry -> !entry.isRoundComplete() }
    ),
    EMAIL_SCORE(
            title = R.string.view_scores_menu__email_score,
            handleClick = {
                if (lastClickedEntryId == null) {
                    this
                }
                else {
                    it.setItems(listOf(lastClickedEntryId))
                    copy(openEmailClicked = true, dropdownMenuOpen = false)
                }
            },
    ),
    EDIT_INFO(
            title = R.string.view_scores_menu__edit,
            handleClick = { copy(openEditInfoClicked = true, dropdownMenuOpen = false) },
    ),
    DELETE(
            title = R.string.view_scores_menu__delete,
            handleClick = { copy(deleteDialogOpen = true, dropdownMenuOpen = false) },
    ),
    CONVERT(
            title = R.string.view_scores_menu__convert,
            handleClick = { copy(convertScoreDialogOpen = true, dropdownMenuOpen = false) },
    ),
}
