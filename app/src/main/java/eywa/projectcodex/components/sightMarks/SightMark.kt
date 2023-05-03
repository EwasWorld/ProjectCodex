package eywa.projectcodex.components.sightMarks

import java.util.*

data class SightMark(
        val id: Int = 0,
        val distance: Int,
        val isMetric: Boolean,
        val dateSet: Calendar,
        val sightMark: Float,
        val note: String? = null,
        val marked: Boolean = false,
        val isArchive: Boolean = false,
)
