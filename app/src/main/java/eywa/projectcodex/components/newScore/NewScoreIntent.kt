package eywa.projectcodex.components.newScore

import eywa.projectcodex.common.utils.UpdateCalendarInfo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundSubType

sealed class NewScoreIntent {
    data class Initialise(val roundBeingEditedId: Int?) : NewScoreIntent()

    data class DateChanged(val info: UpdateCalendarInfo) : NewScoreIntent()

    object OpenRoundSelectDialog : NewScoreIntent()
    object CloseRoundSelectDialog : NewScoreIntent()
    object NoRoundSelected : NewScoreIntent()
    data class RoundSelected(val round: Round) : NewScoreIntent()
    data class SelectRoundDialogFilterClicked(val filter: NewScoreRoundFilter) : NewScoreIntent()
    object SelectRoundDialogClearFilters : NewScoreIntent()

    object OpenSubTypeSelectDialog : NewScoreIntent()
    object CloseSubTypeSelectDialog : NewScoreIntent()
    data class SubTypeSelected(val subType: RoundSubType) : NewScoreIntent()

    object Submit : NewScoreIntent()
    object CancelEditInfo : NewScoreIntent()
    object ResetEditInfo : NewScoreIntent()
}
