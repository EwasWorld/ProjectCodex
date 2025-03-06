package eywa.projectcodex.components.shootDetails.headToHead.addEnd

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsError
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsIntent
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadArcherType

sealed class HeadToHeadAddEndIntent {
    data class ArrowInputsErrorHandled(val error: ArrowInputsError) : HeadToHeadAddEndIntent()
    data object OpenAddMatchScreenHandled : HeadToHeadAddEndIntent()
    data object ResetClicked : HeadToHeadAddEndIntent()
    data object DeleteClicked : HeadToHeadAddEndIntent()
    data object SubmitClicked : HeadToHeadAddEndIntent()
    data object PressBackHandled : HeadToHeadAddEndIntent()
    data object CreateNextMatchClicked : HeadToHeadAddEndIntent()
    data object CreateNextMatchHandled : HeadToHeadAddEndIntent()
    data object EditTypesClicked : HeadToHeadAddEndIntent()
    data class EditTypesItemClicked(val item: HeadToHeadArcherType) : HeadToHeadAddEndIntent()
    data object CloseEditTypesDialog : HeadToHeadAddEndIntent()
    data object CompleteEditTypesDialog : HeadToHeadAddEndIntent()
    data object SightersClicked : HeadToHeadAddEndIntent()
    data object SightersHandled : HeadToHeadAddEndIntent()
    data object ToggleShootOff : HeadToHeadAddEndIntent()
    data class GridRowClicked(val row: HeadToHeadArcherType) : HeadToHeadAddEndIntent()
    data class GridTextValueChanged(val type: HeadToHeadArcherType, val text: String?) : HeadToHeadAddEndIntent()
    data class ArrowInputAction(val action: ArrowInputsIntent) : HeadToHeadAddEndIntent()

    data object ExpandSightMarkClicked : HeadToHeadAddEndIntent()
    data object EditSightMarkClicked : HeadToHeadAddEndIntent()
    data object ExpandSightMarkHandled : HeadToHeadAddEndIntent()
    data object EditSightMarkHandled : HeadToHeadAddEndIntent()
    data class ShootDetailsAction(val action: ShootDetailsIntent) : HeadToHeadAddEndIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : HeadToHeadAddEndIntent()
}
