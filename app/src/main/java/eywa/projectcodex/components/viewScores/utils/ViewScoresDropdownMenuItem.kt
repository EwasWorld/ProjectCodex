package eywa.projectcodex.components.viewScores.utils

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.navigation.findNavController
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.viewScores.ViewScoresFragment
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.ui.ViewScoresListActionState

enum class ViewScoresDropdownMenuItem(
        @StringRes val title: Int,
        /**
         * Returns true if the action was successfully handled
         */
        val onClick: (ViewScoresEntry, View, ViewScoresListActionState) -> Boolean,
        /**
         * Only display this dropdown menu item if this returns true
         */
        val showCondition: ((ViewScoresEntry) -> Boolean)? = null,
) {
    SCORE_PAD(R.string.view_scores_menu__score_pad, { entry, view, _ ->
        val args = Bundle().apply { putInt("archerRoundId", entry.id) }
        view.findNavController().navigate(R.id.scorePadFragment, args)
        true
    }),
    CONTINUE(R.string.view_scores_menu__continue, { entry, view, _ ->
        if (entry.isRoundComplete()) {
            CustomLogger.customLogger.w(ViewScoresFragment.LOG_TAG, "Tried to continue completed round")
            ToastSpamPrevention.displayToast(
                    view.context,
                    view.resources.getString(R.string.err_view_score__round_already_complete)
            )
            false
        }
        else {
            val args = Bundle().apply { putInt("archerRoundId", entry.id) }
            view.findNavController().navigate(R.id.inputEndFragment, args)
            true
        }
    }, { entry -> !entry.isRoundComplete() }),
    EMAIL_SCORE(R.string.view_scores_menu__email_score, { entry, view, _ ->
        val args = Bundle().apply { putInt("archerRoundId", entry.id) }
        view.findNavController().navigate(R.id.emailFragment, args)
        true
    }),
    EDIT_INFO(R.string.view_scores_menu__edit, { entry, view, _ ->
        val args = Bundle().apply { putInt("archerRoundId", entry.id) }
        view.findNavController().navigate(R.id.newScoreFragment, args)
        true
    }),
    DELETE(R.string.view_scores_menu__delete, { _, _, state ->
        state.isDeleteDialogOpen = true
        true
    }),
    CONVERT(R.string.view_scores_menu__convert, { _, _, state ->
        state.isConvertScoreOpen = true
        true
    }),
}