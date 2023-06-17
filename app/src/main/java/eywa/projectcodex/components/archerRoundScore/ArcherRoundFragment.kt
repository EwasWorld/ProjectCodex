package eywa.projectcodex.components.archerRoundScore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent.*
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArcherRoundFragment : Fragment(), ActionBarHelp {
    private val viewModel: ArcherRoundViewModel by viewModels()

    private val backButtonCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            viewModel.handle(ArrowInputsIntent.CancelClicked)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state by viewModel.state.collectAsState()

                val errors = (state as? ArcherRoundState.Loaded)?.errors
                val returnToMainMenu = (state as? ArcherRoundState.InvalidArcherRoundError)?.mainMenuClicked ?: false
                LaunchedEffect(errors, returnToMainMenu) {
                    launch {
                        errors?.forEach {
                            ToastSpamPrevention.displayToast(requireContext(), resources.getString(it.messageId))
                            viewModel.handle(ErrorHandled(it))
                        }
                        if (returnToMainMenu) {
                            findNavController().popBackStack(R.id.mainMenuFragment, false)
                            viewModel.handle(InvalidArcherRoundIntent.ReturnToMenuHandled)
                        }
                    }
                }

                LaunchedEffect(state.interruptBackButtonListener) {
                    backButtonCallback.isEnabled = state.interruptBackButtonListener
                }

                ArcherRoundMainScreen(state) { viewModel.handle(it) }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.archer_round_title)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backButtonCallback)
    }
}
