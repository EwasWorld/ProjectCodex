package eywa.projectcodex.components.handicapTables

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
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme

@AndroidEntryPoint
class HandicapTablesFragment : Fragment(), ActionBarHelp {
    // TODO_CURRENT Test
    private var screen = HandicapTablesScreen()
    private val viewModel: HandicapTablesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CodexTheme {
                    val state by viewModel.state.collectAsState()
                    screen.ComposeContent(state) { viewModel.handle(it) }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = getString(R.string.handicap_tables__title)
    }

    override fun getHelpShowcases() = screen.getHelpShowcases()
    override fun getHelpPriority() = screen.getHelpPriority()
}