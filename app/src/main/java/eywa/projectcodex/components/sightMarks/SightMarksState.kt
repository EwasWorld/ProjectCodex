package eywa.projectcodex.components.sightMarks

import eywa.projectcodex.common.utils.roundToDp
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

            val scaleAmount: Float? = null,
            val shiftAmount: Float? = null,
            val flipScale: Boolean = false,
    ) : SightMarksState() {
        init {
            require(
                    (scaleAmount == null && shiftAmount == null)
                            || (scaleAmount != null && shiftAmount != null)
            ) { "Scale/shift amounts must be set together" }
        }

        val isShiftAndScalePreview
            get() = sightMarks.isNotEmpty() && scaleAmount != null

        val canScaleLower
            get() = scaleAmount != null && scaleAmount > SMALL_SCALE_AMOUNT

        override fun updateSightMarks(value: List<SightMark>) =
                copy(sightMarks = value.sortForDisplay(isHighestNumberAtTheTop))

        override fun updateIsHighestNumberAtTheTop(value: Boolean) =
                copy(sightMarks = sightMarks.sortForDisplay(value), isHighestNumberAtTheTop = value)

        fun getShiftAndScaleState(): Loaded {
            val maxSightMark = sightMarks.maxOf { it.sightMark }
            fun Float.shiftAndScale(): Float = this
                    .let { if (flipScale) maxSightMark - it else it }
                    .let { it * (scaleAmount ?: 1f) }
                    .let { it + (shiftAmount ?: 0f) }
                    .roundToDp(2)

            return Loaded(
                    sightMarks = sightMarks.map { it.copy(sightMark = it.sightMark.shiftAndScale()) },
                    isHighestNumberAtTheTop = isHighestNumberAtTheTop.let { if (flipScale) !it else it },
            )
        }
    }

    abstract fun updateSightMarks(value: List<SightMark>): Loaded
    abstract fun updateIsHighestNumberAtTheTop(value: Boolean): SightMarksState

    companion object {
        const val LARGE_SCALE_AMOUNT = 1f
        const val SMALL_SCALE_AMOUNT = 0.1f

        // TODO Change amount based on current scale
        const val LARGE_SHIFT_AMOUNT = 1f
        const val SMALL_SHIFT_AMOUNT = 0.1f
    }
}
