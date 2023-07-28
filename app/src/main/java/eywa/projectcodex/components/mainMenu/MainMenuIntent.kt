package eywa.projectcodex.components.mainMenu

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.navigation.NavRoute

sealed class MainMenuIntent {
    object WhatsNewOpen : MainMenuIntent()
    data class WhatsNewClose(val latestUpdateAppVersion: AppVersion) : MainMenuIntent()
    object OpenExitDialog : MainMenuIntent()
    object ExitDialogOkClicked : MainMenuIntent()
    object ExitDialogCloseClicked : MainMenuIntent()
    object CloseApplicationHandled : MainMenuIntent()
    data class Navigate(val route: NavRoute) : MainMenuIntent()
    object NavigateHandled : MainMenuIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : MainMenuIntent()
}
