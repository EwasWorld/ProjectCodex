package eywa.projectcodex.components.shootDetails.editEnd

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundError
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsIntent

sealed class EditEndIntent {
    data class ArrowInputsAction(val action: ArrowInputsIntent) : EditEndIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : EditEndIntent()
    data class ShootDetailsAction(val action: ShootDetailsIntent) : EditEndIntent()
    data class ErrorHandled(val error: ArcherRoundError) : EditEndIntent()
    object CloseHandled : EditEndIntent()
}
