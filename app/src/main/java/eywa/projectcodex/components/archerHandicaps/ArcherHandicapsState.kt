package eywa.projectcodex.components.archerHandicaps

import eywa.projectcodex.common.sharedUi.numberField.NumberValidator
import eywa.projectcodex.common.sharedUi.numberField.NumberValidatorGroup
import eywa.projectcodex.common.sharedUi.numberField.TypeValidator
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.database.archer.DEFAULT_ARCHER_ID
import eywa.projectcodex.database.archer.DatabaseArcherHandicap
import eywa.projectcodex.database.archer.HandicapType
import java.util.*

data class ArcherHandicapsState(
        /**
         * Most recent handicap of each type
         */
        val archerHandicaps: List<DatabaseArcherHandicap> = emptyList(),
        val selectedBowStyle: ClassificationBow = ClassificationBow.RECURVE,
        val menuShownForId: Int? = null,
        val addDialogOpen: Boolean = false,
        val editDialogOpen: Boolean = false,

        val addHandicapIsDirty: Boolean = false,
        val addHandicap: String = "",
        val addHandicapType: HandicapType = HandicapType.OUTDOOR,
        val selectHandicapTypeDialogOpen: Boolean = false,
) {
    val handicapTypeDuplicateErrorShown
        get() = archerHandicaps.any { it.handicapType == addHandicapType }

    val handicapValidatorError = handicapValidators.getFirstError(addHandicap, addHandicapIsDirty)
    private val parsedHandicap = handicapValidators.parse(addHandicap)

    val displayHandicaps = archerHandicaps
            .filter { it.bowStyle == selectedBowStyle }
            .sortedBy { it.handicapType.ordinal }

    val getEditingHandicap
        get() = archerHandicaps.firstOrNull { it.archerHandicapId == menuShownForId }

    val addDatabaseValue
        get() = DatabaseArcherHandicap(
                archerHandicapId = 0,
                archerId = DEFAULT_ARCHER_ID,
                bowStyle = ClassificationBow.RECURVE,
                handicapType = addHandicapType,
                handicap = parsedHandicap!!,
                dateSet = Calendar.getInstance(),
        )

    companion object {
        val handicapValidators = NumberValidatorGroup(TypeValidator.IntValidator, NumberValidator.InRange(0..150))
    }
}
