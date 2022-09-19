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
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.ui.ViewScoreScreen
import eywa.projectcodex.components.viewScores.utils.ViewScoresDropdownMenuItem

class ViewScoresFragment : Fragment(), ActionBarHelp {
    private val viewScoresViewModel: ViewScoresViewModel by activityViewModels()
    private var viewScoreScreen = ViewScoreScreen()

    private val dropDownMenuItems = mapOf(
            ViewScoresEntry::class to listOf(
                    ViewScoresDropdownMenuItem.SCORE_PAD,
                    ViewScoresDropdownMenuItem.CONTINUE,
                    ViewScoresDropdownMenuItem.EMAIL_SCORE,
                    ViewScoresDropdownMenuItem.EDIT_INFO,
                    ViewScoresDropdownMenuItem.DELETE,
                    ViewScoresDropdownMenuItem.CONVERT,
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

                                ViewScoresDropdownMenuItem.SCORE_PAD.onClick(
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
