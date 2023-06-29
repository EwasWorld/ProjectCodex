package eywa.projectcodex.components.mainActivity

import eywa.projectcodex.common.helpShowcase.ActionBarHelp

sealed class MainActivityIntent {
    data class StartHelpShowcase(val screen: ActionBarHelp?) : MainActivityIntent()
    object GoToNextHelpShowcaseItem : MainActivityIntent()
    object CloseHelpShowcase : MainActivityIntent()
    object ClearNoHelpShowcaseFlag : MainActivityIntent()
}
