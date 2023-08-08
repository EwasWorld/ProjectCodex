package eywa.projectcodex.components.sightMarks

import eywa.projectcodex.common.utils.roundToDp
import eywa.projectcodex.components.sightMarks.SightMarksIntent.ShiftAndScaleIntent.*
import eywa.projectcodex.components.sightMarks.diagram.SightMarksDiagramHelper
import kotlin.math.absoluteValue
import kotlin.math.pow

data class ShiftAndScaleState(
        val diagramHelper: SightMarksDiagramHelper,
        val currentScale: Float = ZERO_SCALE_VALUE,
        val currentShift: Float = ZERO_SHIFT_VALUE,
        val flipScale: Boolean = false,
        val isConfirmDialogOpen: Boolean = false,
) {
    private val largeShiftAmount = 10f.pow(diagramHelper.majorTickDifferenceLog10)
    private val smallShiftAmount = 10f.pow(diagramHelper.majorTickDifferenceLog10 - 1)
    private val largeScaleAmount = 1f
    private val smallScaleAmount = 0.1f

    val canDoLargeScaleDecrease
        get() = currentScale > largeScaleAmount

    val canDoSmallScaleDecrease
        get() = currentScale > smallScaleAmount

    fun shiftAndScale(value: Float) = value
            .let {
                if (flipScale) diagramHelper.highestSightMark - it + diagramHelper.lowestSightMark
                else it
            }
            .let { it * currentScale }
            .let { it + currentShift }
            .let { v ->
                val dp = (diagramHelper.majorTickDifferenceLog10 - 2)
                v.roundToDp(dp.takeIf { it < 0 }?.absoluteValue ?: 2)
            }

    fun handle(action: SightMarksIntent.ShiftAndScaleIntent): ShiftAndScaleState? =
            when (action) {
                is Scale -> {
                    var change = if (action.bigger) largeScaleAmount else smallScaleAmount
                    if (!action.increased) change *= -1

                    val newAmount = currentScale + change
                    if (newAmount <= 0) this else copy(currentScale = newAmount)
                }

                is Shift -> {
                    var change = if (action.bigger) largeShiftAmount else smallShiftAmount
                    if (!action.increased) change *= -1

                    copy(currentShift = currentShift + change)
                }

                FlipClicked -> copy(flipScale = !flipScale)
                SubmitClicked -> copy(isConfirmDialogOpen = true)
                CancelSubmitClicked -> copy(isConfirmDialogOpen = false)
                ScaleReset -> copy(currentScale = ZERO_SCALE_VALUE)
                ShiftReset -> copy(currentShift = ZERO_SHIFT_VALUE)
                EndShiftAndScale -> null
            }

    companion object {
        /**
         * The scale value for which there is no scaling (1:1)
         */
        private const val ZERO_SCALE_VALUE = 1f

        /**
         * The shift value for which the sight marks are not shifted
         */
        private const val ZERO_SHIFT_VALUE = 0f
    }
}
