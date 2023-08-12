package eywa.projectcodex.components.shootDetails

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.navigation.CodexNavRoute

sealed class ShootDetailsIntent {
    object ReturnToMenuClicked : ShootDetailsIntent()
    object ReturnToMenuHandled : ShootDetailsIntent()
    data class NavBarClicked(val screen: CodexNavRoute) : ShootDetailsIntent()
    data class NavBarClickHandled(val screen: CodexNavRoute) : ShootDetailsIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ShootDetailsIntent()

    data class SelectScorePadEnd(val endNumber: Int?) : ShootDetailsIntent()
}
