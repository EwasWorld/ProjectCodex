package eywa.projectcodex.components.shootDetails

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.model.Arrow

sealed class ShootDetailsIntent {
    object ReturnToMenuClicked : ShootDetailsIntent()
    object ReturnToMenuHandled : ShootDetailsIntent()
    object ToggleSimpleView : ShootDetailsIntent()
    data class NavBarClicked(val screen: CodexNavRoute) : ShootDetailsIntent()
    data class NavBarClickHandled(val screen: CodexNavRoute) : ShootDetailsIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ShootDetailsIntent()

    data class SelectScorePadEnd(val endNumber: Int?) : ShootDetailsIntent()
    data class SetInputtedArrows(val arrows: List<Arrow>) : ShootDetailsIntent()
    data class SetScorePadEndSize(val size: Int) : ShootDetailsIntent()
    data class SetAddEndEndSize(val size: Int) : ShootDetailsIntent()
    data object ClearState : ShootDetailsIntent()
}
