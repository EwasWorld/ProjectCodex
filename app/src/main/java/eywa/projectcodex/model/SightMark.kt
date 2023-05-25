package eywa.projectcodex.model

import eywa.projectcodex.database.sightMarks.DatabaseSightMark
import java.util.*

data class SightMark(
        val id: Int,
        val distance: Int,
        val isMetric: Boolean,
        val dateSet: Calendar,
        val sightMark: Float,
        val note: String? = null,
        val isMarked: Boolean = false,
        val isArchived: Boolean = false,
        val useInPredictions: Boolean = true,
        val bowId: Int? = null,
) {
    constructor(dbSightMark: DatabaseSightMark) : this(
            id = dbSightMark.id,
            bowId = dbSightMark.bowId,
            distance = dbSightMark.distance,
            isMetric = dbSightMark.isMetric,
            dateSet = dbSightMark.dateSet,
            sightMark = dbSightMark.sightMark,
            note = dbSightMark.note,
            isMarked = dbSightMark.isMarked,
            isArchived = dbSightMark.isArchived,
            useInPredictions = dbSightMark.useInPredictions,
    )
}
