package eywa.projectcodex.components.mainMenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme

@AndroidEntryPoint
class MainMenuFragment : Fragment(), ActionBarHelp {
    private var isAlertDialogShown = mutableStateOf(false)
    private var mainMenuScreen = MainMenuScreen()
    private val viewModel: MainMenuViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CodexTheme {
                    val state by viewModel.state.collectAsState()
                    mainMenuScreen.ComposeContent(
//                            isExitDialogOpen = isAlertDialogShown.value,
                            state = state,
                            listener = {
//                                when (it) {
//                                    is ExitDialogCloseClicked -> isAlertDialogShown.value = false
//                                    is ExitDialogOkClicked -> {
//                                        isAlertDialogShown.value = false
//                                        requireActivity().finish()
//                                        exitProcess(0)
//                                    }
//                                    is StartNewScoreClicked ->
//                                        findNavController().navigate(actionMainMenuFragmentToNewScoreFragment())
//                                    is ViewScoresClicked ->
//                                        findNavController().navigate(actionMainMenuFragmentToViewScoresFragment())
//                                    is HandicapTablesClicked ->
//                                        findNavController().navigate(actionMainMenuFragmentToHandicapTablesFragment())
//                                    is SightMarksClicked ->
//                                        findNavController().navigate(actionMainMenuFragmentToSightMarksFragment())
//                                    is SettingsClicked ->
//                                        findNavController().navigate(actionMainMenuFragmentToSettingsFragment())
//                                    is AboutClicked ->
//                                        findNavController().navigate(actionMainMenuFragmentToAboutFragment())
//                                    is ClassificationTablesClicked ->
//                                        findNavController().navigate(actionMainMenuFragmentToClassificationFragment())
//                                    else -> viewModel.handle(it)
//                                }
                            },
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = getString(R.string.main_menu__title)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            isAlertDialogShown.value = !(isAlertDialogShown.value)
        }.isEnabled = true
    }
}
