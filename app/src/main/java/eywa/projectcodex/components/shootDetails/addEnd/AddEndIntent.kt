package eywa.projectcodex.components.shootDetails.addEnd

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsError
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsIntent

sealed class AddEndIntent {
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : AddEndIntent()
    data class ArrowInputsAction(val action: ArrowInputsIntent) : AddEndIntent()
    data class ShootDetailsAction(val action: ShootDetailsIntent) : AddEndIntent()
    data class ErrorHandled(val error: ArrowInputsError) : AddEndIntent()
    object SightersClicked : AddEndIntent()
    object SightersHandled : AddEndIntent()
    object RoundFullDialogOkClicked : AddEndIntent()
    object RoundCompleteDialogOkClicked : AddEndIntent()
    object FullSightMarksClicked : AddEndIntent()
    object FullSightMarksHandled : AddEndIntent()
    object EditSightMarkClicked : AddEndIntent()
    object EditSightMarkHandled : AddEndIntent()
}
