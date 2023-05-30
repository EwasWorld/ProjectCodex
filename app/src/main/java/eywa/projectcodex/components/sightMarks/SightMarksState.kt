package eywa.projectcodex.components.sightMarks

import eywa.projectcodex.model.SightMark

sealed class SightMarksState {
    data class Loading(
            val isHighestNumberAtTheTop: Boolean = true,
    ) : SightMarksState() {
        override fun updateSightMarks(value: List<SightMark>) = Loaded(
                sightMarks = value,
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
        override fun updateSightMarks(value: List<SightMark>) = copy(sightMarks = value)
        override fun updateIsHighestNumberAtTheTop(value: Boolean) = copy(isHighestNumberAtTheTop = value)
    }

    abstract fun updateSightMarks(value: List<SightMark>): Loaded
    abstract fun updateIsHighestNumberAtTheTop(value: Boolean): SightMarksState

}
