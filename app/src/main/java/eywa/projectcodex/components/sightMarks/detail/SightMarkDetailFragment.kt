package eywa.projectcodex.components.sightMarks.detail

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
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SightMarkDetailFragment : Fragment(), ActionBarHelp {
    val viewModel: SightMarkDetailViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                val state by viewModel.state.collectAsState()

                LaunchedEffect(state) { launch { handleEffects(state) } }

                SightMarkDetailScreen(state = state) { viewModel.handle(it) }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.sight_marks__detail_title)
    }

    private fun handleEffects(state: SightMarkDetailState?) {
        if (state == null) return

        if (state.closeScreen) {
            viewModel.handle(SightMarkDetailIntent.CloseHandled)
            findNavController().popBackStack()
        }
    }
}
