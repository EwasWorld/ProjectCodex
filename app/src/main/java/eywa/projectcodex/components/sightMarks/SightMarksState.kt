package eywa.projectcodex.components.sightMarks

import eywa.projectcodex.components.sightMarks.diagram.SightMarksDiagramHelper
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

            val shiftAndScaleState: ShiftAndScaleState? = null
    ) : SightMarksState() {
        init {
            require(sightMarks.isNotEmpty() || shiftAndScaleState == null)
        }

        val diagramHelper = sightMarks
                .takeIf { it.isNotEmpty() }
                ?.let { SightMarksDiagramHelper(it, isHighestNumberAtTheTop) }

        val newShiftAndScaleState
            get() = ShiftAndScaleState(diagramHelper!!)

        override fun updateSightMarks(value: List<SightMark>) =
                copy(sightMarks = value.sortForDisplay(isHighestNumberAtTheTop))

        override fun updateIsHighestNumberAtTheTop(value: Boolean) =
                copy(sightMarks = sightMarks.sortForDisplay(value), isHighestNumberAtTheTop = value)

        fun getShiftedAndScaledSightMarksState(): Loaded =
                shiftAndScaleState?.let {
                    var isHighestAtTop = isHighestNumberAtTheTop
                    if (shiftAndScaleState.flipScale) {
                        isHighestAtTop = !isHighestAtTop
                    }
                    Loaded(
                            sightMarks = sightMarks.map {
                                it.copy(sightMark = shiftAndScaleState.shiftAndScale(it.sightMark))
                            },
                            isHighestNumberAtTheTop = isHighestAtTop,
                    )
                } ?: this
    }

    abstract fun updateSightMarks(value: List<SightMark>): Loaded
    abstract fun updateIsHighestNumberAtTheTop(value: Boolean): SightMarksState
}
