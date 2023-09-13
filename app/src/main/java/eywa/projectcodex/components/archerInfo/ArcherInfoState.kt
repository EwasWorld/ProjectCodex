package eywa.projectcodex.components.archerInfo

import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.database.archer.DatabaseArcher

data class ArcherInfoState(
        val defaultArcher: DatabaseArcher? = null,
        val expanded: Dropdown? = null,
) {
    val isGent = defaultArcher?.isGent ?: true
    val age = defaultArcher?.age ?: ClassificationAge.SENIOR
    val bow = defaultArcher?.bow ?: ClassificationBow.RECURVE

    enum class Dropdown { AGE, BOW }
}
