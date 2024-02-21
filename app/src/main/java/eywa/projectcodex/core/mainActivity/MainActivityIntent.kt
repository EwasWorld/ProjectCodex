package eywa.projectcodex.core.mainActivity

import androidx.compose.ui.geometry.Size
import eywa.projectcodex.common.helpShowcase.ActionBarHelp

sealed class MainActivityIntent {
    data class SetScreenSize(val size: Size) : MainActivityIntent()
    data class StartHelpShowcase(val screen: ActionBarHelp?) : MainActivityIntent()
    object GoToNextHelpShowcaseItem : MainActivityIntent()
    object CloseHelpShowcase : MainActivityIntent()
    object ClearNoHelpShowcaseFlag : MainActivityIntent()
    object PressDetected : MainActivityIntent()
}
