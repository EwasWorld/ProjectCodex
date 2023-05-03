package eywa.projectcodex.components.sightMarks

data class SightMarksState(
        val sightMarks: List<SightMark> = listOf(),
        val isHighestNumberAtTheTop: Boolean = true,

        val openSightMarkDetail: Int? = null,
        val createNewSightMark: Boolean = false,
)
