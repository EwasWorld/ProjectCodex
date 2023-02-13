package eywa.projectcodex.components.handicapTables

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent

sealed class HandicapTablesIntent {
    object ToggleInput : HandicapTablesIntent()
    object ToggleHandicapSystem : HandicapTablesIntent()
    data class InputChanged(val newSize: Int?) : HandicapTablesIntent()
    data class SelectRoundDialogAction(val action: SelectRoundDialogIntent) : HandicapTablesIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : HandicapTablesIntent()
}
