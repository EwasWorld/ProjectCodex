package eywa.projectcodex.components.viewScores

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.viewScores.ui.PreviewEntryProvider
import eywa.projectcodex.components.viewScores.ui.ViewScoreScreen
import eywa.projectcodex.components.viewScores.ui.ViewScoresDropdownMenuItem

// TODO_CURRENT Remove 'compose' from name and delete old frag
class ViewScoresComposeFragment : Fragment(), ActionBarHelp {
    private var viewScoreScreen = ViewScoreScreen()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CodexTheme {
                    viewScoreScreen.ComposeContent(
                            entries = PreviewEntryProvider.generateEntries(20),
                            openContextMenu = null,
                            dropdownMenuItems = listOf(
                                    ViewScoresDropdownMenuItem("Test Action") {
                                        ToastSpamPrevention.displayToast(context, "8")
                                    }
                            ),
                            entryClickedListener = { ToastSpamPrevention.displayToast(context, "1") },
                            entryLongClickedListener = { ToastSpamPrevention.displayToast(context, "2") },
                            isInMultiSelectMode = false,
                            multiSelectClickedListener = { ToastSpamPrevention.displayToast(context, "3") },
                            selectAllOrNoneClickedListener = { ToastSpamPrevention.displayToast(context, "4") },
                            emailSelectedClickedListener = { ToastSpamPrevention.displayToast(context, "5") },
                            cancelMultiSelectClickedListener = { ToastSpamPrevention.displayToast(context, "6") },
                            closeDropdownMenuListener = { ToastSpamPrevention.displayToast(context, "7") },
                            noRoundsDialogOkListener = { ToastSpamPrevention.displayToast(context, "8") },
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
}