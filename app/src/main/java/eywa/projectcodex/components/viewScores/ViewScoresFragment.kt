package eywa.projectcodex.components.viewScores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.ui.ViewScoreScreen
import eywa.projectcodex.components.viewScores.utils.ConvertScoreType
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
                    viewScoreScreen.ComposeContent(
                            entries = viewScoresViewModel.state.data,
                            dropdownMenuItems = dropDownMenuItems,
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

    override fun getHelpShowcases() = viewScoreScreen.getHelpShowcases()
    override fun getHelpPriority() = viewScoreScreen.getHelpPriority()

    companion object {
        const val LOG_TAG = "ViewScores"
    }

    class ViewScoreScreenListenerImpl(
            private val viewScoresViewModel: ViewScoresViewModel,
            private val view: View,
            private val displayToast: (messageId: Int) -> Unit
    ) : ViewScoreScreen.ViewScoreScreenListener() {
        override fun dropdownMenuItemClicked(entryIndex: Int?, menuItem: ViewScoresDropdownMenuItem): Boolean {
            if (entryIndex !in viewScoresViewModel.state.data.indices) {
                displayToast(R.string.err__try_again_error)
                return false
            }
            val entry = viewScoresViewModel.state.data[entryIndex!!]
            return menuItem.onClick(entry, viewScoresViewModel, view, contextMenuState)
        }

        // TODO_CURRENT put this elsewhere?
        override fun entryClicked(entryId: Int) {
            if (entryId !in viewScoresViewModel.state.data.indices) {
                displayToast(R.string.err__try_again_error)
                return
            }

            // TODO_CURRENT make sure this is read properly accessibility
            if (viewScoresViewModel.state.isInMultiSelectMode) {
                viewScoresViewModel.handle(ViewScoresIntent.ToggleEntrySelected(entryId))
                return
            }

            ViewScoresDropdownMenuItem.SCORE_PAD.onClick(
                    viewScoresViewModel.state.data[entryId], viewScoresViewModel, view, contextMenuState
            )
        }

        override fun selectAllOrNoneClicked() = viewScoresViewModel.handle(ViewScoresIntent.SelectAllOrNone())

        override fun emailClicked() = view.findNavController().navigate(R.id.emailFragment)

        override fun noRoundsDialogDismissedListener() {
            view.findNavController().popBackStack()
        }

        override fun convertDialogActionListener(entryIndex: Int?, convertType: ConvertScoreType) {
            if (entryIndex == null) {
                displayToast(R.string.err__try_again_error)
                return
            }
            viewScoresViewModel.state.data[entryIndex].arrows?.let {
                convertType.convertScore(it, viewScoresViewModel)
            }
        }
    }
}
