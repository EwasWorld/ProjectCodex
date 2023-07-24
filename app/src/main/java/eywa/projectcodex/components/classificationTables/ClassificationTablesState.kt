package eywa.projectcodex.components.classificationTables

import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.utils.classificationTables.ClassificationTableEntry
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.datastore.DatastoreKey

data class ClassificationTablesState(
        val isGent: Boolean = true,
        val age: ClassificationAge = ClassificationAge.SENIOR,
        val bow: ClassificationBow = ClassificationBow.RECURVE,
        val expanded: Dropdown? = null,
        val scores: List<ClassificationTableEntry> = emptyList(),
        val use2023Handicaps: Boolean = DatastoreKey.Use2023HandicapSystem.defaultValue,
        val selectRoundDialogState: SelectRoundDialogState = SelectRoundDialogState(),
) {
    enum class Dropdown { AGE, BOW }
}
