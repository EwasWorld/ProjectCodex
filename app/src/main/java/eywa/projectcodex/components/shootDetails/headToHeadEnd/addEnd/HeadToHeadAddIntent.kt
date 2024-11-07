package eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsIntent
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType

sealed class HeadToHeadAddIntent {
    data object ExpandSightMarkClicked : HeadToHeadAddIntent()
    data object EditSightMarkClicked : HeadToHeadAddIntent()
    data object ExpandSightMarkHandled : HeadToHeadAddIntent()
    data object EditSightMarkHandled : HeadToHeadAddIntent()
    data class ShootDetailsAction(val action: ShootDetailsIntent) : HeadToHeadAddIntent()

    data class AddHeatAction(val action: HeadToHeadAddHeatIntent) : HeadToHeadAddIntent()
    data class AddEndAction(val action: HeadToHeadAddEndIntent) : HeadToHeadAddIntent()

    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : HeadToHeadAddIntent()
}

sealed class HeadToHeadAddEndIntent {
    data object SubmitClicked : HeadToHeadAddEndIntent()
    data object SightersClicked : HeadToHeadAddEndIntent()
    data object SightersHandled : HeadToHeadAddEndIntent()
    data object ToggleShootOffWin : HeadToHeadAddEndIntent()
    data class GridRowClicked(val row: HeadToHeadArcherType) : HeadToHeadAddEndIntent()
    data class GridTextValueChanged(val type: HeadToHeadArcherType, val text: String?) : HeadToHeadAddEndIntent()
    data class ArrowInputAction(val action: ArrowInputsIntent) : HeadToHeadAddEndIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : HeadToHeadAddEndIntent()
}

sealed class HeadToHeadAddHeatIntent {
    data class OpponentUpdated(val opponent: String) : HeadToHeadAddHeatIntent()
    data class OpponentQualiRankUpdated(val rank: String?) : HeadToHeadAddHeatIntent()
    data object ToggleIsBye : HeadToHeadAddHeatIntent()
    data object HeatClicked : HeadToHeadAddHeatIntent()
    data class SelectHeatDialogItemClicked(val heat: Int) : HeadToHeadAddHeatIntent()
    data object CloseSelectHeatDialog : HeadToHeadAddHeatIntent()
    data object SubmitClicked : HeadToHeadAddHeatIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : HeadToHeadAddHeatIntent()
}
