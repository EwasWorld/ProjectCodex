package eywa.projectcodex.components.referenceTables.handicapTables

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogIntent

sealed class HandicapTablesIntent {
    data object ToggleInput : HandicapTablesIntent()
    data object ToggleIsCompound : HandicapTablesIntent()
    data object ToggleHandicapSystem : HandicapTablesIntent()
    data class InputChanged(val newSize: String?) : HandicapTablesIntent()
    data class SelectRoundDialogAction(val action: SelectRoundDialogIntent) : HandicapTablesIntent()
    data class SelectFaceDialogAction(val action: SelectRoundFaceDialogIntent) : HandicapTablesIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : HandicapTablesIntent()
    data object ToggleSimpleView : HandicapTablesIntent()
}
