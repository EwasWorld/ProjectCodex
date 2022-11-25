package eywa.projectcodex.components.viewScores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.ui.ViewScoresScreen
import eywa.projectcodex.components.viewScores.ui.rememberViewScoresListActionState
import eywa.projectcodex.components.viewScores.utils.ConvertScoreType
import eywa.projectcodex.components.viewScores.utils.ViewScoresDropdownMenuItem

@AndroidEntryPoint
class ViewScoresFragment : Fragment(), ActionBarHelp {
    private val viewScoresViewModel: ViewScoresViewModel by activityViewModels()
    private var viewScoresScreen = ViewScoresScreen()

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

    private val entrySingleClickActions = mapOf(
            ViewScoresEntry::class to ViewScoresDropdownMenuItem.SCORE_PAD,
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CodexTheme {
                    viewScoresScreen.ComposeContent(
                            entries = viewScoresViewModel.state.data,
                            rememberViewScoresListActionState(
                                    singleClickActions = entrySingleClickActions,
                                    dropdownMenuItems = dropDownMenuItems
                            ),
                            isInMultiSelectMode = viewScoresViewModel.state.isInMultiSelectMode,
                            listener = ViewScoreScreenListenerImpl(viewScoresViewModel, requireView()) {
                                ToastSpamPrevention.displayToast(
                                        requireContext(),
                                        requireContext().resources.getString(it)
                                )
                            }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = getString(R.string.view_score__title)
    }

    override fun getHelpShowcases() = viewScoresScreen.getHelpShowcases()
    override fun getHelpPriority() = viewScoresScreen.getHelpPriority()

    companion object {
        const val LOG_TAG = "ViewScores"
    }

    class ViewScoreScreenListenerImpl(
            private val viewScoresViewModel: ViewScoresViewModel,
            private val view: View,
            private val displayToast: (messageId: Int) -> Unit
    ) : ViewScoresScreen.ViewScoreScreenListener() {
        override fun dropdownMenuItemClicked(entry: ViewScoresEntry, menuItem: ViewScoresDropdownMenuItem): Boolean {
            return menuItem.onClick(entry, view, contextMenuState)
        }

        override fun toggleListItemSelected(entryIndex: Int) =
                viewScoresViewModel.handle(ViewScoresIntent.ToggleEntrySelected(entryIndex))

        override fun toggleMultiSelectMode() =
                viewScoresViewModel.handle(ViewScoresIntent.ToggleMultiSelectMode)

        override fun selectAllOrNoneClicked() = viewScoresViewModel.handle(ViewScoresIntent.SelectAllOrNone())

        override fun multiSelectEmailClicked() {
            if (viewScoresViewModel.state.data.all { !it.isSelected }) {
                displayToast(R.string.err_view_score__no_rounds_selected)
                return
            }
            view.findNavController().navigate(R.id.emailFragment)
        }

        override fun noRoundsDialogDismissedListener() {
            view.findNavController().popBackStack()
        }

        override fun convertScoreDialogOkListener(entryIndex: Int?, convertType: ConvertScoreType) {
            if (entryIndex == null) {
                displayToast(R.string.err__try_again_error)
                return
            }
            viewScoresViewModel.state.data[entryIndex].arrows
                    ?.let { oldArrows -> convertType.convertScore(oldArrows) }
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { viewScoresViewModel.handle(ViewScoresIntent.UpdateArrowValues(it)) }
        }

        override fun deleteDialogOkListener(entryIndex: Int?) {
            if (entryIndex == null) {
                displayToast(R.string.err__try_again_error)
                return
            }
            viewScoresViewModel.handle(ViewScoresIntent.DeleteRound(viewScoresViewModel.state.data[entryIndex].id))
        }
    }
}
