package eywa.projectcodex.components.mainMenu

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent

sealed class MainMenuIntent {
    object HandicapDialogClicked : MainMenuIntent()
    object ExitDialogOkClicked : MainMenuIntent()
    object ExitDialogCloseClicked : MainMenuIntent()
    object StartNewScoreClicked : MainMenuIntent()
    object ViewScoresClicked : MainMenuIntent()
    object HandicapTablesClicked : MainMenuIntent()
    object SettingsClicked : MainMenuIntent()
    object AboutClicked : MainMenuIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : MainMenuIntent()
}
