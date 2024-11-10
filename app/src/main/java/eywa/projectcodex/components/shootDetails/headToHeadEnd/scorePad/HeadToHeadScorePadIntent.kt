package eywa.projectcodex.components.shootDetails.headToHeadEnd.scorePad

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent

sealed class HeadToHeadScorePadIntent {
    data object GoToAddEnd : HeadToHeadScorePadIntent()
    data object GoToAddEndHandled : HeadToHeadScorePadIntent()
    data class EditSighters(val heat: Int) : HeadToHeadScorePadIntent()
    data class EditHeatInfo(val heat: Int) : HeadToHeadScorePadIntent()
    data class ShootDetailsAction(val action: ShootDetailsIntent) : HeadToHeadScorePadIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : HeadToHeadScorePadIntent()
}
