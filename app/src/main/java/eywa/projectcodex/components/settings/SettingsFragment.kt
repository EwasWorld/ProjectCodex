package eywa.projectcodex.components.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eywa.projectcodex.common.helpShowcase.ActionBarHelp

@AndroidEntryPoint
class SettingsFragment : Fragment(), ActionBarHelp {
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                val state by viewModel.state.collectAsState()
                SettingsScreen(state) { viewModel.handle(it) }
            }
        }
    }
}
