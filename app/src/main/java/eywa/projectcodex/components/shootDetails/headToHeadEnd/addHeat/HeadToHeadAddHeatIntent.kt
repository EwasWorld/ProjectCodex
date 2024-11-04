package eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent

sealed class HeadToHeadAddHeatIntent {
    data class OpponentUpdated(val opponent: String) : HeadToHeadAddHeatIntent()
    data class OpponentQualiRankUpdated(val rank: String?) : HeadToHeadAddHeatIntent()
    data object ToggleIsBye : HeadToHeadAddHeatIntent()
    data object HeatClicked : HeadToHeadAddHeatIntent()
    data class SelectHeatDialogItemClicked(val heat: Int) : HeadToHeadAddHeatIntent()
    data object CloseSelectHeatDialog : HeadToHeadAddHeatIntent()
    data object ShouldCloseScreenHandled : HeadToHeadAddHeatIntent()
    data object SubmitClicked : HeadToHeadAddHeatIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : HeadToHeadAddHeatIntent()
}
