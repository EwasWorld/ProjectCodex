package eywa.projectcodex.components.shootDetails.headToHead.scorePad

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent
import eywa.projectcodex.components.shootDetails.headToHead.grid.SetDropdownMenuItem

sealed class HeadToHeadScorePadIntent {
    /**
     * Button when there are no matches entered
     */
    data object GoToAddEnd : HeadToHeadScorePadIntent()
    data object GoToAddEndHandled : HeadToHeadScorePadIntent()

    data class EditSighters(val match: Int) : HeadToHeadScorePadIntent()
    data object EditSightersHandled : HeadToHeadScorePadIntent()

    data class SetClicked(val match: Int, val setNumber: Int) : HeadToHeadScorePadIntent()
    data class SetOptionsMenuClicked(
            val match: Int,
            val setNumber: Int,
            val dropdownItem: SetDropdownMenuItem,
    ) : HeadToHeadScorePadIntent()

    data class CloseSetOptionsMenu(val match: Int, val setNumber: Int) : HeadToHeadScorePadIntent()
    data object SetOptionsMenuActionHandled : HeadToHeadScorePadIntent()

    data class OpenMatchOptionsClicked(val match: Int) : HeadToHeadScorePadIntent()
    data class MatchOptionsMenuClicked(
            val match: Int,
            val dropdownItem: MatchDropdownMenuItem,
    ) : HeadToHeadScorePadIntent()

    data class CloseMatchOptionsMenu(val match: Int) : HeadToHeadScorePadIntent()
    data object MatchOptionsMenuActionHandled : HeadToHeadScorePadIntent()

    data object DeleteConfirmationOkClicked : HeadToHeadScorePadIntent()
    data object DeleteConfirmationCancelClicked : HeadToHeadScorePadIntent()
    data class ShootDetailsAction(val action: ShootDetailsIntent) : HeadToHeadScorePadIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : HeadToHeadScorePadIntent()
}
