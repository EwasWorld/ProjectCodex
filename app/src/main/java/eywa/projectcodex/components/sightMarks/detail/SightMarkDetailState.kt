package eywa.projectcodex.components.sightMarks.detail

import eywa.projectcodex.common.sharedUi.NumberValidator
import eywa.projectcodex.common.sharedUi.NumberValidatorGroup
import eywa.projectcodex.common.sharedUi.TypeValidator
import eywa.projectcodex.database.bow.DEFAULT_BOW_ID
import eywa.projectcodex.database.sightMarks.DatabaseSightMark
import eywa.projectcodex.model.SightMark
import java.util.*

data class SightMarkDetailState(
        val distance: String = "",
        val isMetric: Boolean = true,
        val sightMark: String = "",
        val note: String? = null,
        val isMarked: Boolean = false,
        val isArchived: Boolean = false,

        val originalSightMark: SightMark? = null,

        val sightMarkIsDirty: Boolean = false,
        val distanceIsDirty: Boolean = false,

        val updateDateSet: Boolean = true,
        val closeScreen: Boolean = false,
) {
    val distanceValidatorError = distanceValidators.getFirstError(distance, distanceIsDirty)
    private val parsedDistance = distanceValidators.parse(distance)

    val sightMarkValidatorError = sightMarkValidators.getFirstError(sightMark, sightMarkIsDirty)
    private val parsedSightMark = sightMarkValidators.parse(sightMark)

    val isFormValid = parsedDistance != null && parsedSightMark != null

    fun asDatabaseSightMark() = DatabaseSightMark(
            id = originalSightMark?.id ?: 0,
            bowId = DEFAULT_BOW_ID,
            distance = parsedDistance!!,
            isMetric = isMetric,
            dateSet = originalSightMark?.dateSet?.takeIf { !updateDateSet }
                    ?: Calendar.getInstance(Locale.getDefault()),
            sightMark = parsedSightMark!!,
            isMarked = isMarked,
            isArchived = isArchived,
            note = note,
    )

    companion object {
        private val distanceValidators = NumberValidatorGroup(TypeValidator.IntValidator, NumberValidator.POSITIVE)
        private val sightMarkValidators = NumberValidatorGroup(TypeValidator.FloatValidator)

        fun fromOriginalSightMark(originalSightMark: SightMark) =
                SightMarkDetailState(
                        distance = originalSightMark.distance.toString(),
                        isMetric = originalSightMark.isMetric,
                        sightMark = originalSightMark.sightMark.toString(),
                        note = originalSightMark.note,
                        isMarked = originalSightMark.isMarked,
                        isArchived = originalSightMark.isArchived,
                        originalSightMark = originalSightMark,
                )
    }
}
