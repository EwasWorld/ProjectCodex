package eywa.projectcodex.components.shootDetails.stats

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent
import eywa.projectcodex.components.shootDetails.stats.ui.StatsScreenPastRecordsTabs

sealed class StatsIntent {
    object EditShootClicked : StatsIntent()
    object EditShootHandled : StatsIntent()
    object EditArcherInfoClicked : StatsIntent()
    object EditArcherInfoHandled : StatsIntent()
    object EditHandicapInfoClicked : StatsIntent()
    object EditHandicapInfoHandled : StatsIntent()
    object ExpandHandicapsClicked : StatsIntent()
    object ExpandHandicapsHandled : StatsIntent()
    object ExpandClassificationsClicked : StatsIntent()
    object ExpandClassificationsHandled : StatsIntent()
    object PastRoundRecordsClicked : StatsIntent()
    object PastRoundRecordsDismissed : StatsIntent()
    data class PastRecordsTabClicked(val tab: StatsScreenPastRecordsTabs) : StatsIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : StatsIntent()
    data class ShootDetailsAction(val action: ShootDetailsIntent) : StatsIntent()
}
