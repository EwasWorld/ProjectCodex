package eywa.projectcodex.common.utils.classificationTables

import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationRound.DbRoundRef

data class ClassificationTables(
        private val data: List<ClassificationTableEntry>,
) {
    /**
     * Take a CSV file and create ClassificationTables object
     *
     * @see ClassificationTableEntry.fromString
     */
    constructor(rawString: String) :
            this(rawString.split("\n").mapNotNull { ClassificationTableEntry.fromString(it.removeSuffix("\r")) })

    fun get(
            isGent: Boolean,
            age: ClassificationAge,
            bow: ClassificationBow,
            roundId: Int,
            roundSubTypeId: Int?,
    ) = data.filter {
        it.isGent == isGent
                && it.age == age
                && bow == it.bowStyle
                && it.rounds.contains(DbRoundRef(roundId, roundSubTypeId))
    }.sortedBy { it.classification.ordinal }
}
