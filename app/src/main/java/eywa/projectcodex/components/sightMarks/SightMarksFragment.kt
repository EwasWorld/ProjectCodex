package eywa.projectcodex.components.sightMarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import eywa.projectcodex.components.sightMarks.SightMarksFragmentDirections.actionSightMarksFragmentToSightMarkDetailFragment
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SightMarksFragment : Fragment(), ActionBarHelp {
    private val viewModel: SightMarksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.sight_marks__title)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                val state by viewModel.state.collectAsState()

                LaunchedEffect(state) { launch { handleEffects(state) } }

                SightMarksScreen(
                        state = state,
                        listener = { viewModel.handle(it) },
                )
            }
        }
    }

    private fun handleEffects(state: SightMarksState) {
        if (state.openSightMarkDetail != null) {
            findNavController().navigate(actionSightMarksFragmentToSightMarkDetailFragment(state.openSightMarkDetail))
            viewModel.handle(SightMarksIntent.OpenSightMarkHandled)
        }

        if (state.createNewSightMark) {
            findNavController().navigate(actionSightMarksFragmentToSightMarkDetailFragment(NULL_ID))
            viewModel.handle(SightMarksIntent.CreateSightMarkHandled)
        }
    }

    companion object {
        const val NULL_ID = -1
    }
}
