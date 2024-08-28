package eywa.projectcodex.components.referenceTables.classificationTables

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow

sealed class ClassificationTablesIntent {
    object ToggleIsGent : ClassificationTablesIntent()

    data class AgeSelected(val age: ClassificationAge) : ClassificationTablesIntent()
    object AgeClicked : ClassificationTablesIntent()

    data class BowSelected(val bow: ClassificationBow) : ClassificationTablesIntent()
    object BowClicked : ClassificationTablesIntent()

    object CloseDropdown : ClassificationTablesIntent()

    data class SelectRoundDialogAction(val action: SelectRoundDialogIntent) : ClassificationTablesIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ClassificationTablesIntent()
}
