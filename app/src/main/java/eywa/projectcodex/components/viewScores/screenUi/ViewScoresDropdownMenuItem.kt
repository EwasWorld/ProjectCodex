package eywa.projectcodex.components.viewScores.screenUi

import androidx.annotation.StringRes
import eywa.projectcodex.R
import eywa.projectcodex.common.diActivityHelpers.ShootIdsUseCase
import eywa.projectcodex.components.viewScores.ViewScoresState
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry

enum class ViewScoresDropdownMenuItem(
        @StringRes val title: Int,
        val handleClick: ViewScoresState.(shootIdsUseCase: ShootIdsUseCase) -> ViewScoresState,
        /**
         * Only display this dropdown menu item if this returns true
         */
        val shouldShow: ((ViewScoresEntry) -> Boolean)? = null,
) {
    SCORE_PAD(
            title = R.string.view_scores_menu__score_pad,
            handleClick = {
                val entry = data?.find { it.id == lastClickedEntryId }
                when {
                    entry?.info?.h2h != null -> copy(openH2hScorePadClicked = true)
                    else -> copy(openScorePadClicked = true)
                }
            },
            shouldShow = { !it.isCount },
    ),
    CONTINUE(
            title = R.string.view_scores_menu__continue,
            handleClick = {
                val entry = data?.find { it.id == lastClickedEntryId }
                when {
                    entry?.info?.h2h != null -> copy(openH2hAddClicked = true)
                    entry?.isRoundComplete() == true -> copy(openAddEndOnCompletedRound = true)
                    else -> copy(openAddEndClicked = true)
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
                    copy(openEmailClicked = true)
                }
            },
    ),
    EDIT_INFO(
            title = R.string.view_scores_menu__edit,
            handleClick = { copy(openEditInfoClicked = true) },
    ),
    DELETE(
            title = R.string.view_scores_menu__delete,
            handleClick = { copy(deleteDialogOpen = true) },
    ),
    CONVERT(
            title = R.string.view_scores_menu__convert,
            handleClick = { copy(convertScoreDialogOpen = true) },
            shouldShow = { !it.isCount },
    ),
    VIEW(
            title = R.string.view_scores_menu__view,
            handleClick = {
                val entry = data?.find { it.id == lastClickedEntryId }
                when {
                    entry?.info?.h2h != null -> copy(openH2hAddClicked = true)
                    else -> copy(openAddCountClicked = true)
                }
            },
    ),
}
