package eywa.projectcodex.components.archerRoundScore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.archerRoundScore.ArcherRoundEffect.Error
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArcherRoundFragment : Fragment(), ActionBarHelp {
    private val args: ArcherRoundFragmentArgs by navArgs()

    private val viewModel: ArcherRoundViewModel by viewModels()

    private val backButtonCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            viewModel.handle(ArcherRoundIntent.ScreenCancelClicked)
        }
    }

//    private val screen = ArcherRoundScreen()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.handle(
                ArcherRoundIntent.Initialise(
                        screen = ArcherRoundScreen.valueOf(args.screen),
                        archerRoundId = args.archerRoundId,
                )
        )

        lifecycleScope.launch {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is Error -> {
                        context?.let {
                            val message = when (effect) {
                                Error.EndFullCannotAddMore -> R.string.err_input_end__end_full
                                Error.NoArrowsCannotBackSpace -> R.string.err_input_end__end_empty
                                Error.NotEnoughArrowsInputted -> R.string.err_input_end__end_not_full
                            }
                            ToastSpamPrevention.displayToast(it, it.resources.getString(message))
                        }
                    }
                }
            }
        }

        return ComposeView(requireContext()).apply {
            setContent {
                LaunchedEffect(viewModel.state.interruptBackButtonListener) {
                    backButtonCallback.isEnabled = viewModel.state.interruptBackButtonListener
                }

                ArcherRoundScreen(viewModel.state) { viewModel.handle(it) }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.archer_round_title)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backButtonCallback)
    }

    // TODO_CURRENT help showcases
    override fun getHelpShowcases() = listOf<HelpShowcaseItem>()
    override fun getHelpPriority() = null
//    override fun getHelpShowcases() = screen.getHelpShowcases()
//    override fun getHelpPriority() = screen.getHelpPriority()
}