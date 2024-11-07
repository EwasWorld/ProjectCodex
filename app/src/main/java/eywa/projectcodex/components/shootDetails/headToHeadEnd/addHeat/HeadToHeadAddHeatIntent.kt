package eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent

sealed class HeadToHeadAddHeatIntent {
    data object OpenAddEndScreenHandled : HeadToHeadAddHeatIntent()
    data class OpponentUpdated(val opponent: String) : HeadToHeadAddHeatIntent()
    data class OpponentQualiRankUpdated(val rank: String?) : HeadToHeadAddHeatIntent()
    data object ToggleIsBye : HeadToHeadAddHeatIntent()
    data object HeatClicked : HeadToHeadAddHeatIntent()
    data class SelectHeatDialogItemClicked(val heat: Int) : HeadToHeadAddHeatIntent()
    data object CloseSelectHeatDialog : HeadToHeadAddHeatIntent()
    data object SubmitClicked : HeadToHeadAddHeatIntent()

    data object ExpandSightMarkClicked : HeadToHeadAddHeatIntent()
    data object EditSightMarkClicked : HeadToHeadAddHeatIntent()
    data object ExpandSightMarkHandled : HeadToHeadAddHeatIntent()
    data object EditSightMarkHandled : HeadToHeadAddHeatIntent()
    data class ShootDetailsAction(val action: ShootDetailsIntent) : HeadToHeadAddHeatIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : HeadToHeadAddHeatIntent()
}
