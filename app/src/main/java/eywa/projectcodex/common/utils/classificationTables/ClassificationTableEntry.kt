package eywa.projectcodex.common.utils.classificationTables

import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationRound

data class ClassificationTableEntry(
        val classification: Classification,
        val isGent: Boolean,
        val bowStyle: ClassificationBow,
        val rounds: List<ClassificationRound.DbRoundRef>,
        val age: ClassificationAge,
        val score: Int,
) {
    companion object {
        /**
         * Create a ClassificationTableEntry from a CSV line
         */
        fun fromString(value: String): ClassificationTableEntry? {
            if (value.isBlank()) return null

            val split = value.split(",")
            return ClassificationTableEntry(
                    classification = Classification.values()[split[0].toInt() - 1],
                    isGent = split[1] == "Men",
                    bowStyle = ClassificationBow.backwardsMap[split[2]]!!,
                    age = ClassificationAge.backwardsMap[split[3]]!!,
                    rounds = ClassificationRound.backwardsMap[split[4]]!!.rounds,
                    score = split[5].toInt(),
            )
        }
    }
}
