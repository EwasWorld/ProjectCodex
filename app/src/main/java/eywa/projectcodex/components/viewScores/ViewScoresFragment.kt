package eywa.projectcodex.components.viewScores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen
import eywa.projectcodex.components.viewScores.ui.ViewScoresScreen
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ViewScoresFragment : Fragment(), ActionBarHelp {
    private val viewScoresViewModel: ViewScoresViewModel by activityViewModels()
    private var viewScoresScreen = ViewScoresScreen()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CodexTheme {
                    val state by viewScoresViewModel.state.collectAsState()

                    LaunchedEffect(state) { launch { handleEffects(state) } }

                    viewScoresScreen.ComposeContent(
                            state = state,
                            listener = { viewScoresViewModel.handle(it) },
                    )
                }
            }
        }
    }

    private fun handleEffects(state: ViewScoresState) {
        if (state.multiSelectEmailClicked) {
            findNavController().navigate(R.id.emailFragment)
            viewScoresViewModel.handle(ViewScoresIntent.HandledEmailClicked)
        }

        if (state.multiSelectEmailNoSelection) {
            ToastSpamPrevention.displayToast(
                    requireContext(),
                    requireContext().resources.getString(R.string.err_view_score__no_rounds_selected)
            )
            viewScoresViewModel.handle(ViewScoresIntent.HandledEmailNoSelection)
        }

        if (state.openScorePadClicked) {
            if (state.lastClickedEntryId != null) {
                val args = Bundle().apply {
                    putString("screen", ArcherRoundScreen.SCORE_PAD.name)
                    putInt("archerRoundId", state.lastClickedEntryId)
                }
                findNavController().navigate(R.id.archerRoundFragment, args)
            }
            viewScoresViewModel.handle(ViewScoresIntent.HandledScorePadOpened)
        }

        if (state.openInputEndOnCompletedRound) {
            if (state.lastClickedEntryId != null) {
                CustomLogger.customLogger.w(LOG_TAG, "Tried to continue completed round")
                ToastSpamPrevention.displayToast(
                        requireContext(),
                        resources.getString(R.string.err_view_score__round_already_complete)
                )
            }
            viewScoresViewModel.handle(ViewScoresIntent.HandledInputEndOnCompletedRound)
        }

        if (state.openInputEndClicked) {
            if (state.lastClickedEntryId != null) {
                val args = Bundle().apply {
                    putString("screen", ArcherRoundScreen.INPUT_END.name)
                    putInt("archerRoundId", state.lastClickedEntryId)
                }
                findNavController().navigate(R.id.archerRoundFragment, args)
            }
            viewScoresViewModel.handle(ViewScoresIntent.HandledInputEndOpened)
        }

        if (state.openEmailClicked) {
            if (state.lastClickedEntryId != null) {
                val args = Bundle().apply {
                    putInt("archerRoundId", state.lastClickedEntryId)
                }
                findNavController().navigate(R.id.emailFragment, args)
            }
            viewScoresViewModel.handle(ViewScoresIntent.HandledEmailOpened)
        }

        if (state.openEditInfoClicked) {
            if (state.lastClickedEntryId != null) {
                val args = Bundle().apply {
                    putInt("archerRoundId", state.lastClickedEntryId)
                }
                findNavController().navigate(R.id.newScoreFragment, args)
            }
            viewScoresViewModel.handle(ViewScoresIntent.HandledEditInfoOpened)
        }

        if (state.noRoundsDialogOkClicked) {
            findNavController().popBackStack()
            viewScoresViewModel.handle(ViewScoresIntent.HandledNoRoundsDialogOkClicked)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = getString(R.string.view_score__title)
    }

    companion object {
        const val LOG_TAG = "ViewScores"
    }
}
