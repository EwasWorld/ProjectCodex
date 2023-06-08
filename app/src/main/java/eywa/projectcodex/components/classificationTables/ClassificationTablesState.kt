package eywa.projectcodex.components.classificationTables

import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundEnabledFilters
import eywa.projectcodex.common.utils.classificationTables.ClassificationTableEntry
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.datastore.DatastoreKey

data class ClassificationTablesState(
        val isGent: Boolean = true,
        val age: ClassificationAge = ClassificationAge.SENIOR,
        val bow: ClassificationBow = ClassificationBow.RECURVE,
        val expanded: Dropdown? = null,
        val round: FullRoundInfo? = null,
        val roundFilters: SelectRoundEnabledFilters = SelectRoundEnabledFilters(),
        val subType: Int? = null,
        val allRounds: List<FullRoundInfo>? = null,
        val isSelectRoundDialogOpen: Boolean = false,
        val isSelectSubtypeDialogOpen: Boolean = false,
        val scores: List<ClassificationTableEntry> = emptyList(),
        val use2023Handicaps: Boolean = DatastoreKey.Use2023HandicapSystem.defaultValue,
) {
    enum class Dropdown { AGE, BOW }
}
