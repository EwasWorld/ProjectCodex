package eywa.projectcodex.components.sightMarks

import eywa.projectcodex.database.bow.DEFAULT_BOW_ID
import eywa.projectcodex.database.sightMarks.DatabaseSightMark
import java.util.*

object SightMarksPreviewHelper {
    val sightMarks = listOf(
            DatabaseSightMark(
                    1,
                    DEFAULT_BOW_ID,
                    18,
                    isMetric = true,
                    dateSet = Calendar.getInstance(),
                    sightMark = 4.5f,
                    isArchived = true
            ),
            DatabaseSightMark(
                    2,
                    DEFAULT_BOW_ID,
                    20,
                    isMetric = true,
                    dateSet = Calendar.getInstance(),
                    sightMark = 4.09f,
                    isArchived = true
            ),
            DatabaseSightMark(
                    3,
                    DEFAULT_BOW_ID,
                    25,
                    isMetric = true,
                    dateSet = Calendar.getInstance(),
                    sightMark = 3.8f,
                    isArchived = true
            ),
            DatabaseSightMark(
                    4,
                    DEFAULT_BOW_ID,
                    30,
                    isMetric = true,
                    dateSet = Calendar.getInstance(),
                    sightMark = 3.3f
            ),
            DatabaseSightMark(
                    5,
                    DEFAULT_BOW_ID,
                    50,
                    isMetric = true,
                    dateSet = Calendar.getInstance(),
                    sightMark = 1.75f
            ),
            DatabaseSightMark(
                    6,
                    DEFAULT_BOW_ID,
                    70,
                    isMetric = true,
                    dateSet = Calendar.getInstance(),
                    sightMark = 1.1f,
                    isMarked = true,
                    note = "Hi I'm a note"
            ),
            DatabaseSightMark(
                    7,
                    DEFAULT_BOW_ID,
                    60,
                    isMetric = true,
                    dateSet = Calendar.getInstance(),
                    sightMark = 1.0f,
                    note = "Hi I'm a note"
            ),

            DatabaseSightMark(
                    8,
                    DEFAULT_BOW_ID,
                    20,
                    isMetric = false,
                    dateSet = Calendar.getInstance(),
                    sightMark = 4.1f
            ),
            DatabaseSightMark(
                    9,
                    DEFAULT_BOW_ID,
                    40,
                    isMetric = false,
                    dateSet = Calendar.getInstance(),
                    sightMark = 3.15f,
                    isArchived = true
            ),
            DatabaseSightMark(
                    10,
                    DEFAULT_BOW_ID,
                    30,
                    isMetric = false,
                    dateSet = Calendar.getInstance(),
                    sightMark = 3.1f,
                    isArchived = true
            ),
            DatabaseSightMark(
                    11,
                    DEFAULT_BOW_ID,
                    50,
                    isMetric = false,
                    dateSet = Calendar.getInstance(),
                    sightMark = 2.0f
            ),
            DatabaseSightMark(
                    12,
                    DEFAULT_BOW_ID,
                    60,
                    isMetric = false,
                    dateSet = Calendar.getInstance(),
                    sightMark = 1.4f
            ),
            DatabaseSightMark(
                    13,
                    DEFAULT_BOW_ID,
                    80,
                    isMetric = false,
                    dateSet = Calendar.getInstance(),
                    sightMark = 0.9f,
                    isMarked = true,
                    note = "Hi I'm a note"
            ),
    )
}
