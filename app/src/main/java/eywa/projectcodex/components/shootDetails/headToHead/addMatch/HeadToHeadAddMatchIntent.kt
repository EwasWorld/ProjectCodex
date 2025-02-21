package eywa.projectcodex.components.shootDetails.headToHead.addMatch

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent

sealed class HeadToHeadAddMatchIntent {
    data object OpenAddEndScreenHandled : HeadToHeadAddMatchIntent()
    data class OpponentUpdated(val opponent: String) : HeadToHeadAddMatchIntent()
    data class OpponentQualiRankUpdated(val rank: String?) : HeadToHeadAddMatchIntent()
    data class MaxPossibleRankUpdated(val rank: String?) : HeadToHeadAddMatchIntent()
    data object ToggleIsBye : HeadToHeadAddMatchIntent()
    data object HeatClicked : HeadToHeadAddMatchIntent()
    data class SelectHeatDialogItemClicked(val heat: Int?) : HeadToHeadAddMatchIntent()
    data object CloseSelectHeatDialog : HeadToHeadAddMatchIntent()
    data object SubmitClicked : HeadToHeadAddMatchIntent()
    data object DeleteClicked : HeadToHeadAddMatchIntent()
    data object ResetClicked : HeadToHeadAddMatchIntent()

    data object ExpandSightMarkClicked : HeadToHeadAddMatchIntent()
    data object EditSightMarkClicked : HeadToHeadAddMatchIntent()
    data object ExpandSightMarkHandled : HeadToHeadAddMatchIntent()
    data object EditSightMarkHandled : HeadToHeadAddMatchIntent()
    data object BackPressedHandled : HeadToHeadAddMatchIntent()
    data class ShootDetailsAction(val action: ShootDetailsIntent) : HeadToHeadAddMatchIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : HeadToHeadAddMatchIntent()
}
