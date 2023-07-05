package eywa.projectcodex.components.viewScores.utils

import androidx.annotation.StringRes
import eywa.projectcodex.R
import eywa.projectcodex.components.viewScores.ViewScoresState
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry

enum class ViewScoresDropdownMenuItem(
        @StringRes val title: Int,
        val handleClick: ViewScoresState.() -> ViewScoresState,
        /**
         * Only display this dropdown menu item if this returns true
         */
        val shouldShow: ((ViewScoresEntry) -> Boolean)? = null,
) {
    SCORE_PAD(
            title = R.string.view_scores_menu__score_pad,
            handleClick = { copy(openScorePadClicked = true, dropdownItems = null) }
    ),
    CONTINUE(
            title = R.string.view_scores_menu__continue,
            handleClick = {
                if (data.find { it.id == lastClickedEntryId }?.isRoundComplete() == true) {
                    copy(openInputEndOnCompletedRound = true)
                }
                else {
                    copy(openInputEndClicked = true, dropdownItems = null)
                }
            },
            shouldShow = { entry -> !entry.isRoundComplete() }
    ),
    EMAIL_SCORE(
            title = R.string.view_scores_menu__email_score,
            handleClick = { copy(openEmailClicked = true, dropdownItems = null) },
    ),
    EDIT_INFO(
            title = R.string.view_scores_menu__edit,
            handleClick = {
                // TODO_CURRENT Update use case with ids
                copy(openEditInfoClicked = true, dropdownItems = null)
            },
    ),
    DELETE(
            title = R.string.view_scores_menu__delete,
            handleClick = { copy(deleteDialogOpen = true, dropdownItems = null) },
    ),
    CONVERT(
            title = R.string.view_scores_menu__convert,
            handleClick = { copy(convertScoreDialogOpen = true, dropdownItems = null) },
    ),
}
