package eywa.projectcodex.common.utils.classificationTables

import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationRound

data class ClassificationTableEntry(
        val classification: Classification,
        val isGent: Boolean,
        val bowStyle: ClassificationBow,
        val round: ClassificationRound.DbRoundRef,
        val age: ClassificationAge,
        val score: Int?,
        val handicap: Int? = null,
) {
    companion object {
        /**
         * Create a ClassificationTableEntry from a CSV line.
         * [handicap] will be null
         */
        fun fromString(value: String): ClassificationTableEntry? {
            if (value.isBlank()) return null

            val split = value.split(",")
            return ClassificationTableEntry(
                    classification = Classification.backwardsMap[split[0].toInt()]!!,
                    isGent = split[1] == "Men",
                    bowStyle = ClassificationBow.backwardsMap[split[2]]!!,
                    age = ClassificationAge.backwardsMap[split[3]]!!,
                    round = ClassificationRound.backwardsMap[split[4]]!!.rounds,
                    score = split[5].toInt(),
            )
        }
    }
}
