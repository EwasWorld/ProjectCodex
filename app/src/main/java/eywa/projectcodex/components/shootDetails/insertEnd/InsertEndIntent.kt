package eywa.projectcodex.components.shootDetails.insertEnd

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundError
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsIntent

sealed class InsertEndIntent {
    data class ArrowInputsAction(val action: ArrowInputsIntent) : InsertEndIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : InsertEndIntent()
    data class ShootDetailsAction(val action: ShootDetailsIntent) : InsertEndIntent()
    data class ErrorHandled(val error: ArcherRoundError) : InsertEndIntent()
    object CloseHandled : InsertEndIntent()
}
