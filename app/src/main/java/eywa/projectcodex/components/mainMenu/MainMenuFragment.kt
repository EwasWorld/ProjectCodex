package eywa.projectcodex.components.mainMenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import kotlin.system.exitProcess

class MainMenuFragment : Fragment(), ActionBarHelp {
    private var isAlertDialogShown = mutableStateOf(false)
    private var mainMenuScreen = MainMenuScreen()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CodexTheme {
                    mainMenuScreen.ComposeContent(
                            isExitDialogOpen = isAlertDialogShown.value,
                            onExitAlertClicked = { isPositiveButton ->
                                isAlertDialogShown.value = false
                                if (isPositiveButton) {
                                    requireActivity().finish()
                                    exitProcess(0)
                                }
                            },
                            onStartNewScoreClicked = {
                                findNavController().navigate(
                                        MainMenuFragmentDirections.actionMainMenuFragmentToNewScoreFragment()
                                )
                            },
                            onViewScoresClicked = {
                                findNavController().navigate(
                                        MainMenuFragmentDirections.actionMainMenuFragmentToViewScoresFragment()
                                )
                            },
                            onHandicapTablesClicked = {
                                findNavController().navigate(
                                        MainMenuFragmentDirections.actionMainMenuFragmentToHandicapTablesFragment()
                                )
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

    override fun getHelpShowcases() = mainMenuScreen.getHelpShowcases()
    override fun getHelpPriority() = mainMenuScreen.getHelpPriority()
}
