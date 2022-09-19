package eywa.projectcodex.components.viewScores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.ui.ViewScoreScreen

// TODO_CURRENT Remove 'compose' from name
class ViewScoresComposeFragment : Fragment(), ActionBarHelp {
    private val viewScoresViewModel: ViewScoresViewModel by activityViewModels()
    private var viewScoreScreen = ViewScoreScreen()

    private val dropDownMenuItems = mapOf(
            ViewScoresEntry::class to listOf(
                    ViewScoreDropdownMenuItem.SCORE_PAD,
                    ViewScoreDropdownMenuItem.CONTINUE,
                    ViewScoreDropdownMenuItem.EMAIL_SCORE,
                    ViewScoreDropdownMenuItem.EDIT_INFO,
                    ViewScoreDropdownMenuItem.DELETE,
                    ViewScoreDropdownMenuItem.CONVERT,
            )
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CodexTheme {
                    val generalError = stringResource(id = R.string.err__try_again_error)

                    viewScoreScreen.ComposeContent(
                            entries = viewScoresViewModel.state.data,
                            convertDialogSelectedIndex = viewScoresViewModel.state.convertDialogSelectedIndex,
                            contextMenuOpenForIndex = viewScoresViewModel.state.openContextMenuEntryIndex,
                            dropdownMenuItems = dropDownMenuItems,
                            dropdownMenuItemClicked = {
                                val entryIndex = viewScoresViewModel.state.openContextMenuEntryIndex
                                if (entryIndex !in viewScoresViewModel.state.data.indices) {
                                    ToastSpamPrevention.displayToast(requireContext(), generalError)
                                    return@ComposeContent
                                }
                                val entry = viewScoresViewModel.state.data[entryIndex!!]
                                if (it.onClick(entry, viewScoresViewModel, requireView())) {
                                    viewScoresViewModel.handle(ViewScoresIntent.CloseContextMenu)
                                }
                            },
                            entryClickedListener = {
                                if (it !in viewScoresViewModel.state.data.indices) {
                                    ToastSpamPrevention.displayToast(requireContext(), generalError)
                                    return@ComposeContent
                                }

                                // TODO_CURRENT make sure this is read properly accessibility
                                if (viewScoresViewModel.state.isInMultiSelectMode) {
                                    viewScoresViewModel.handle(ViewScoresIntent.ToggleEntrySelected(it))
                                    return@ComposeContent
                                }

                                ViewScoreDropdownMenuItem.SCORE_PAD.onClick(
                                        viewScoresViewModel.state.data[it], viewScoresViewModel, requireView()
                                )
                            },
                            entryLongClickedListener = {
                                viewScoresViewModel.handle(ViewScoresIntent.OpenContextMenu(it))
                            },
                            isInMultiSelectMode = viewScoresViewModel.state.isInMultiSelectMode,
                            startMultiSelectListener = {
                                viewScoresViewModel.handle(ViewScoresIntent.SetMultiSelectMode(true))
                            },
                            selectAllOrNoneClickedListener = {
                                viewScoresViewModel.handle(ViewScoresIntent.SelectAllOrNone())
                            },
                            emailSelectedClickedListener = {
                                requireView().findNavController().navigate(R.id.emailFragment)
                            },
                            cancelMultiSelectListener = {
                                viewScoresViewModel.handle(ViewScoresIntent.SetMultiSelectMode(false))
                            },
                            closeDropdownMenuListener = {
                                viewScoresViewModel.handle(ViewScoresIntent.CloseContextMenu)
                            },
                            noRoundsDialogOkListener = {
                                requireView().findNavController().popBackStack()
                            },
                            convertDialogDismissedListener = {
                                viewScoresViewModel.handle(ViewScoresIntent.CloseConvertAndContextMenu)
                            },
                            convertDialogActionListener = { selectedConversionType ->
                                fun toast(@StringRes message: Int) {
                                    ToastSpamPrevention.displayToast(
                                            requireContext(), requireContext().resources.getString(message)
                                    )
                                }

                                val arrows =
                                        viewScoresViewModel.state.data[viewScoresViewModel.state.openContextMenuEntryIndex!!].arrows
                                viewScoresViewModel.handle(ViewScoresIntent.CloseConvertAndContextMenu)
                                val onCompletion = {
                                    toast(R.string.view_score__convert_score_completed_message)
                                }

                                toast(R.string.view_score__convert_score_started_message)

                                if (arrows == null) {
                                    onCompletion()
                                    return@ComposeContent
                                }
                                selectedConversionType.convertScore(arrows, viewScoresViewModel)
                                        ?.invokeOnCompletion { onCompletion() }
                                        ?: onCompletion()
                            },
                            convertDialogSelectionChangedListener = {
                                viewScoresViewModel.handle(ViewScoresIntent.UpdateConvertMenuSelectedIndex(it))
                            },
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = getString(R.string.view_score__title)
    }

    override fun getHelpShowcases() = viewScoreScreen.getHelpShowcases()
    override fun getHelpPriority() = viewScoreScreen.getHelpPriority()

    companion object {
        const val LOG_TAG = "ViewScores"
    }
}

sealed class ViewScoreEffect {

}

enum class ViewScoreDropdownMenuItem(
        @StringRes val title: Int,
        /**
         * Returns true if the action was successfully handled
         */
        val onClick: (ViewScoresEntry, ViewScoresViewModel, View) -> Boolean,
        /**
         * Only display this dropdown menu item if this returns true
         */
        val showCondition: ((ViewScoresEntry) -> Boolean)? = null,
) {
    SCORE_PAD(R.string.view_scores_menu__score_pad, { entry, _, view ->
        val args = Bundle().apply { putInt("archerRoundId", entry.id) }
        view.findNavController().navigate(R.id.scorePadFragment, args)
        true
    }),
    CONTINUE(R.string.view_scores_menu__continue, { entry, _, view ->
        if (entry.isRoundComplete()) {
            CustomLogger.customLogger.w(ViewScoresComposeFragment.LOG_TAG, "Tried to continue completed round")
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
    EMAIL_SCORE(R.string.view_scores_menu__email_score, { entry, _, view ->
        val args = Bundle().apply { putInt("archerRoundId", entry.id) }
        view.findNavController().navigate(R.id.emailFragment, args)
        true
    }),
    EDIT_INFO(R.string.view_scores_menu__edit, { entry, _, view ->
        val args = Bundle().apply { putInt("archerRoundId", entry.id) }
        view.findNavController().navigate(R.id.newScoreFragment, args)
        true
    }),
    DELETE(R.string.view_scores_menu__delete, { entry, viewModel, _ ->
        viewModel.deleteRound(entry.id)
        true
    }),
    CONVERT(R.string.view_scores_menu__convert, { _, viewModel, _ ->
        viewModel.handle(ViewScoresIntent.OpenConvertMenu)
        false
    }),
}