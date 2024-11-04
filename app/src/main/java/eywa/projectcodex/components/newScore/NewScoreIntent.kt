package eywa.projectcodex.components.newScore

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.UpdateCalendarInfo
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogIntent

sealed class NewScoreIntent {
    data class DateChanged(val info: UpdateCalendarInfo) : NewScoreIntent()
    data object TypeChanged : NewScoreIntent()
    data object H2hStyleChanged : NewScoreIntent()
    data class H2hTeamSizeChanged(val value: String?) : NewScoreIntent()
    data class H2hQualiRankChanged(val value: String?) : NewScoreIntent()

    data class SelectRoundDialogAction(val action: SelectRoundDialogIntent) : NewScoreIntent()
    data class SelectFaceDialogAction(val action: SelectRoundFaceDialogIntent) : NewScoreIntent()

    data object Submit : NewScoreIntent()
    data object CancelEditInfo : NewScoreIntent()
    data object ResetEditInfo : NewScoreIntent()

    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : NewScoreIntent()

    data object HandleNavigate : NewScoreIntent()
    data object HandlePopBackstack : NewScoreIntent()
}
