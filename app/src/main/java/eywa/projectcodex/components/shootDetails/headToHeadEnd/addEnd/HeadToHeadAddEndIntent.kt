package eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsIntent
import eywa.projectcodex.model.Arrow

sealed class HeadToHeadAddEndIntent {
    data class ArrowInputted(val arrowInput: Arrow) : HeadToHeadAddEndIntent()
    data class ArrowInputAction(val action: ArrowInputsIntent) : HeadToHeadAddEndIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : HeadToHeadAddEndIntent()
}
