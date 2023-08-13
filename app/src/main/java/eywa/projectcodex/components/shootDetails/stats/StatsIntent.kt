package eywa.projectcodex.components.shootDetails.stats

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent

sealed class StatsIntent {
    object EditClicked : StatsIntent()
    object EditHandled : StatsIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : StatsIntent()
    data class ShootDetailsAction(val action: ShootDetailsIntent) : StatsIntent()
}
