package eywa.projectcodex.components.shootDetails.headToHeadEnd.scorePad

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent

sealed class HeadToHeadScorePadIntent {
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : HeadToHeadScorePadIntent()
}
