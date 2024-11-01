package eywa.projectcodex.components.shootDetails

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.model.Arrow

sealed class ShootDetailsIntent {
    data object ReturnToMenuClicked : ShootDetailsIntent()
    data object ReturnToMenuHandled : ShootDetailsIntent()
    data object ToggleSimpleView : ShootDetailsIntent()
    data object BackClicked : ShootDetailsIntent()
    data object BackHandled : ShootDetailsIntent()
    data class NavBarClicked(val screen: CodexNavRoute) : ShootDetailsIntent()
    data class NavBarClickHandled(val screen: CodexNavRoute) : ShootDetailsIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ShootDetailsIntent()

    data class SelectScorePadEnd(val endNumber: Int?) : ShootDetailsIntent()
    data class SetInputtedArrows(val arrows: List<Arrow>) : ShootDetailsIntent()
    data class SetScorePadEndSize(val size: Int) : ShootDetailsIntent()
    data class SetAddEndEndSize(val size: Int) : ShootDetailsIntent()
}
