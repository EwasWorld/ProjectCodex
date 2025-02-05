package eywa.projectcodex.components.shootDetails.headToHead.scorePad

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent

sealed class HeadToHeadScorePadIntent {
    data object GoToAddEnd : HeadToHeadScorePadIntent()
    data object GoToAddEndHandled : HeadToHeadScorePadIntent()
    data class EditSighters(val match: Int) : HeadToHeadScorePadIntent()
    data class EditMatchInfo(val match: Int) : HeadToHeadScorePadIntent()
    data class EditSet(val match: Int, val setNumber: Int) : HeadToHeadScorePadIntent()
    data object EditSightersHandled : HeadToHeadScorePadIntent()
    data object EditMatchInfoHandled : HeadToHeadScorePadIntent()
    data object EditSetHandled : HeadToHeadScorePadIntent()
    data class AddNewSet(val match: Int) : HeadToHeadScorePadIntent()
    data class ShootDetailsAction(val action: ShootDetailsIntent) : HeadToHeadScorePadIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : HeadToHeadScorePadIntent()
}
