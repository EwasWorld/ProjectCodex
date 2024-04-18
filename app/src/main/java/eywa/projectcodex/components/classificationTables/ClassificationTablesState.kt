package eywa.projectcodex.components.classificationTables

import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.utils.classificationTables.ClassificationTableEntry
import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.database.rounds.RoundRepo
import eywa.projectcodex.datastore.DatastoreKey

data class ClassificationTablesState(
        val isGent: Boolean = true,
        val age: ClassificationAge = ClassificationAge.SENIOR,
        val bow: ClassificationBow = ClassificationBow.RECURVE,
        val expanded: Dropdown? = null,
        private val officialClassifications: List<ClassificationTableEntry> = emptyList(),
        private val roughHandicaps: List<ClassificationTableEntry> = emptyList(),
        val use2023Handicaps: Boolean = DatastoreKey.Use2023HandicapSystem.defaultValue,
        val selectRoundDialogState: SelectRoundDialogState = SelectRoundDialogState(),
        val updateDefaultRoundsState: UpdateDefaultRoundsState = UpdateDefaultRoundsState.NotStarted,
) {
    val scores = Classification.values().mapNotNull { classification ->
        val roundScore = officialClassifications.find { it.classification == classification }
        if (roundScore != null) {
            return@mapNotNull roundScore to true
        }

        val roughHandicap = roughHandicaps.find { it.classification == classification }
        if (roughHandicap != null) {
            return@mapNotNull roughHandicap to false
        }

        null
    }

    val wa1440RoundInfo
        get() = selectRoundDialogState.allRounds
                ?.find { it.round.defaultRoundId == RoundRepo.WA_1440_DEFAULT_ROUND_ID }

    enum class Dropdown { AGE, BOW }
}
