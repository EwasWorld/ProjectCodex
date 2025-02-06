package eywa.projectcodex.components.shootDetails.headToHead.scorePad

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent
import eywa.projectcodex.components.shootDetails.headToHead.grid.DropdownMenuItem

sealed class HeadToHeadScorePadIntent {
    data object GoToAddEnd : HeadToHeadScorePadIntent()
    data object GoToAddEndHandled : HeadToHeadScorePadIntent()
    data class EditSighters(val match: Int) : HeadToHeadScorePadIntent()
    data class EditMatchInfo(val match: Int) : HeadToHeadScorePadIntent()
    data class SetClicked(val match: Int, val setNumber: Int) : HeadToHeadScorePadIntent()
    data class OptionsMenuClicked(
            val match: Int,
            val setNumber: Int,
            val dropdownItem: DropdownMenuItem,
    ) : HeadToHeadScorePadIntent()

    data class CloseSetOptionsMenu(val match: Int, val setNumber: Int) : HeadToHeadScorePadIntent()
    data object DeleteConfirmationOkClicked : HeadToHeadScorePadIntent()
    data object DeleteConfirmationCancelClicked : HeadToHeadScorePadIntent()
    data object OptionsMenuActionHandled : HeadToHeadScorePadIntent()
    data object EditSightersHandled : HeadToHeadScorePadIntent()
    data object EditMatchInfoHandled : HeadToHeadScorePadIntent()
    data class AddNewSet(val match: Int) : HeadToHeadScorePadIntent()
    data class ShootDetailsAction(val action: ShootDetailsIntent) : HeadToHeadScorePadIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : HeadToHeadScorePadIntent()
}
