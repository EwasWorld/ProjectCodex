package eywa.projectcodex.common.sharedUi.selectRoundDialog

import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundSubType

sealed class SelectRoundDialogIntent {
    object OpenRoundSelectDialog : SelectRoundDialogIntent()
    object CloseRoundSelectDialog : SelectRoundDialogIntent()
    object NoRoundSelected : SelectRoundDialogIntent()
    data class RoundSelected(val round: Round) : SelectRoundDialogIntent()
    data class SelectRoundDialogFilterClicked(val filter: SelectRoundFilter) : SelectRoundDialogIntent()
    object SelectRoundDialogClearFilters : SelectRoundDialogIntent()

    object OpenSubTypeSelectDialog : SelectRoundDialogIntent()
    object CloseSubTypeSelectDialog : SelectRoundDialogIntent()
    data class SubTypeSelected(val subType: RoundSubType) : SelectRoundDialogIntent()
}
