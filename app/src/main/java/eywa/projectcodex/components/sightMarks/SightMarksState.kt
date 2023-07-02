package eywa.projectcodex.components.sightMarks

import eywa.projectcodex.model.SightMark

private fun List<SightMark>.sortForDisplay(isHighestNumberAtTheTop: Boolean) =
        if (isHighestNumberAtTheTop) sortedBy { it.sightMark } else sortedByDescending { it.sightMark }

sealed class SightMarksState {
    data class Loading(
            val isHighestNumberAtTheTop: Boolean = true,
    ) : SightMarksState() {
        override fun updateSightMarks(value: List<SightMark>) = Loaded(
                sightMarks = value.sortForDisplay(isHighestNumberAtTheTop),
                isHighestNumberAtTheTop = isHighestNumberAtTheTop,
        )

        override fun updateIsHighestNumberAtTheTop(value: Boolean) = copy(isHighestNumberAtTheTop = value)
    }

    data class Loaded(
            val sightMarks: List<SightMark> = listOf(),
            val isHighestNumberAtTheTop: Boolean = true,

            val openSightMarkDetail: Int? = null,
            val createNewSightMark: Boolean = false,
    ) : SightMarksState() {
        override fun updateSightMarks(value: List<SightMark>) =
                copy(sightMarks = value.sortForDisplay(isHighestNumberAtTheTop))

        override fun updateIsHighestNumberAtTheTop(value: Boolean) =
                copy(sightMarks = sightMarks.sortForDisplay(value), isHighestNumberAtTheTop = value)
    }

    abstract fun updateSightMarks(value: List<SightMark>): Loaded
    abstract fun updateIsHighestNumberAtTheTop(value: Boolean): SightMarksState

}
