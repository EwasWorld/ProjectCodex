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
            val isHighestNumberAtTheTop: Boolean = false,

            val openSightMarkDetail: Int? = null,
            val createNewSightMark: Boolean = false,

            val scaleAmount: Float? = null,
            val shiftAmount: Float? = null,
            val flipScale: Boolean = false,
            val isConfirmShiftAndScaleDialogOpen: Boolean = false,
    ) : SightMarksState() {
        init {
            require(
                    (scaleAmount == null && shiftAmount == null)
                            || (scaleAmount != null && shiftAmount != null)
            ) { "Scale/shift amounts must be set together" }
        }

        val isShiftAndScalePreview
            get() = sightMarks.isNotEmpty() && scaleAmount != null

        val canLargeScaleLower
            get() = scaleAmount != null && scaleAmount > LARGE_SCALE_AMOUNT

        val canSmallScaleLower
            get() = scaleAmount != null && scaleAmount > SMALL_SCALE_AMOUNT

        override fun updateSightMarks(value: List<SightMark>) =
                copy(sightMarks = value.sortForDisplay(isHighestNumberAtTheTop))

        override fun updateIsHighestNumberAtTheTop(value: Boolean) =
                copy(sightMarks = sightMarks.sortForDisplay(value), isHighestNumberAtTheTop = value)

        fun getShiftAndScaleState(): Loaded {
            if (!isShiftAndScalePreview) return this

            val maxSightMark = sightMarks.maxOf { it.sightMark }
            val minSightMark = sightMarks.minOf { it.sightMark }
            fun Float.shiftAndScale(): Float = this
                    .let { if (flipScale) maxSightMark - it + minSightMark else it }
                    .let { it * (scaleAmount ?: ZERO_SCALE_VALUE) }
                    .let { it + (shiftAmount ?: ZERO_SHIFT_VALUE) }
                    // TODO Round to the correct amount based on magnitude
                    .roundToDp(2)

            return Loaded(sightMarks = sightMarks.map { it.copy(sightMark = it.sightMark.shiftAndScale()) })
        }
    }

    abstract fun updateSightMarks(value: List<SightMark>): Loaded
    abstract fun updateIsHighestNumberAtTheTop(value: Boolean): SightMarksState

    companion object {
        const val LARGE_SCALE_AMOUNT = 1f
        const val SMALL_SCALE_AMOUNT = 0.1f

        /**
         * The scale value for which there is no scaling (1:1)
         */
        const val ZERO_SCALE_VALUE = 1f

        // TODO Change amount based on current scale
        const val LARGE_SHIFT_AMOUNT = 1f
        const val SMALL_SHIFT_AMOUNT = 0.1f

        /**
         * The shift value for which the sight marks are not shifted
         */
        const val ZERO_SHIFT_VALUE = 0f
    }
}
