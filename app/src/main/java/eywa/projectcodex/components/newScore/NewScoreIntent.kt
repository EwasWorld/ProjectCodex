package eywa.projectcodex.components.newScore

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.UpdateCalendarInfo
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogIntent

sealed class NewScoreIntent {
    data class DateChanged(val info: UpdateCalendarInfo) : NewScoreIntent()

    data class SelectRoundDialogAction(val action: SelectRoundDialogIntent) : NewScoreIntent()
    data class SelectFaceDialogAction(val action: SelectRoundFaceDialogIntent) : NewScoreIntent()

    object Submit : NewScoreIntent()
    object CancelEditInfo : NewScoreIntent()
    object ResetEditInfo : NewScoreIntent()

    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : NewScoreIntent()

    object HandleNavigate : NewScoreIntent()
    object HandlePopBackstack : NewScoreIntent()
}
