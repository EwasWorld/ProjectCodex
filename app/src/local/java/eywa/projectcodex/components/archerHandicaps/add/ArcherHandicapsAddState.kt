package eywa.projectcodex.components.archerHandicaps.add

import eywa.projectcodex.common.sharedUi.numberField.NumberFieldState
import eywa.projectcodex.common.sharedUi.numberField.NumberValidator
import eywa.projectcodex.common.sharedUi.numberField.NumberValidatorGroup
import eywa.projectcodex.common.sharedUi.numberField.TypeValidator
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.database.archer.DEFAULT_ARCHER_ID
import eywa.projectcodex.database.archer.DatabaseArcherHandicap
import eywa.projectcodex.database.archer.HandicapType
import eywa.projectcodex.model.Handicap
import java.util.Calendar

internal data class ArcherHandicapsAddState(
        val shouldCloseDialog: Boolean = false,
        val bowStyle: ClassificationBow = ClassificationBow.RECURVE,
        val date: Calendar = Calendar.getInstance(),
        val handicap: NumberFieldState<Int> = NumberFieldState(
                NumberValidatorGroup(
                        TypeValidator.IntValidator,
                        NumberValidator.InRange(Handicap.MIN_HANDICAP..Handicap.maxHandicap(true)),
                ),
        ),
) {
    val addDatabaseValue
        get() = DatabaseArcherHandicap(
                archerHandicapId = 0,
                archerId = DEFAULT_ARCHER_ID,
                bowStyle = bowStyle,
                handicapType = HandicapType.OUTDOOR,
                handicap = handicap.parsed!!,
                dateSet = Calendar.getInstance(),
        )
}
