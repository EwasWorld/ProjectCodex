package eywa.projectcodex.components.newScore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import kotlinx.coroutines.launch

class NewScoreFragment : Fragment(), ActionBarHelp {
    private val args: NewScoreFragmentArgs by navArgs()

    private val viewModel: NewScoreViewModel by viewModels()
    private val screen = NewScoreScreen()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.handle(
                NewScoreIntent.Initialise(
                        roundBeingEditedId = args.archerRoundId.takeIf { it != -1 },
                )
        )

        lifecycleScope.launch {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is NewScoreEffect.NavigateToInputEnd -> findNavController().navigate(
                            NewScoreFragmentDirections.actionNewScoreFragmentToInputEndFragment(effect.archerRoundId)
                    )
                    NewScoreEffect.PopBackstack -> findNavController().popBackStack()
                }
            }
        }

        return ComposeView(requireContext()).apply {
            setContent {
                screen.ComposeContent(viewModel.state) { viewModel.handle(it) }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(
                if (viewModel.state.isEditing) R.string.create_round__title else R.string.create_round__edit_title
        )
    }

    override fun getHelpShowcases() = screen.getHelpShowcases()
    override fun getHelpPriority() = screen.getHelpPriority()
}
