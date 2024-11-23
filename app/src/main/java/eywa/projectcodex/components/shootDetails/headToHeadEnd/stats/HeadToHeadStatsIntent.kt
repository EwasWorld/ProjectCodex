package eywa.projectcodex.components.shootDetails.headToHeadEnd.stats

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent

sealed class HeadToHeadStatsIntent {
    data object ViewQuailfyingRoundClicked : HeadToHeadStatsIntent()
    data object ViewQuailfyingRoundHandled : HeadToHeadStatsIntent()
    data object EditMainInfoClicked : HeadToHeadStatsIntent()
    data object EditMainInfoHandled : HeadToHeadStatsIntent()
    data class ShootDetailsAction(val action: ShootDetailsIntent) : HeadToHeadStatsIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : HeadToHeadStatsIntent()
    data object EditArcherInfoClicked : HeadToHeadStatsIntent()
    data object EditArcherInfoHandled : HeadToHeadStatsIntent()
    data object ExpandHandicapsClicked : HeadToHeadStatsIntent()
    data object ExpandHandicapsHandled : HeadToHeadStatsIntent()
    data object ExpandClassificationsClicked : HeadToHeadStatsIntent()
    data object ExpandClassificationsHandled : HeadToHeadStatsIntent()
}
