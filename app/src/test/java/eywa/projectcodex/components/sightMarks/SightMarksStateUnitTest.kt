package eywa.projectcodex.components.sightMarks

import eywa.projectcodex.components.sightMarks.diagram.SightMarksDiagramHelper
import eywa.projectcodex.model.SightMark
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class SightMarksStateUnitTest {
    private val originalSightMarks = listOf(2f, 3.5f).map {
        SightMark(1, 10, true, Calendar.getInstance(), it)
    }
    private val originalState = SightMarksState.Loaded(
            sightMarks = originalSightMarks,
            shiftAndScaleState = ShiftAndScaleState(
                    diagramHelper = SightMarksDiagramHelper(originalSightMarks, false),
                    currentShift = 0f,
                    currentScale = 1f,
            ),
    )

    private fun SightMarksState.Loaded.getShiftedSightMarkValues() =
            getShiftedAndScaledSightMarksState().sightMarks.map { it.sightMark }

    private fun SightMarksState.Loaded.copyShiftState(
            currentScale: Float? = null,
            currentShift: Float? = null,
            flipScale: Boolean? = null,
    ) = copy(
            shiftAndScaleState = shiftAndScaleState?.copy(
                    currentScale = currentScale ?: shiftAndScaleState!!.currentScale,
                    currentShift = currentShift ?: shiftAndScaleState!!.currentShift,
                    flipScale = flipScale ?: shiftAndScaleState!!.flipScale,
            ),
    )

    @Test
    fun testGetShiftAndScaleState_Unchanged() {
        // Not in preview mode
        assertEquals(
                listOf(2f, 3.5f),
                originalState.copy(shiftAndScaleState = null).getShiftedSightMarkValues(),
        )
        // No scaling/shift
        assertEquals(
                listOf(2f, 3.5f),
                originalState.getShiftedSightMarkValues(),
        )
    }


    @Test
    fun testGetShiftAndScaleState_Scale() {
        // Scaling
        assertEquals(
                listOf(4f, 7f),
                originalState.copyShiftState(currentScale = 2f).getShiftedSightMarkValues(),
        )
        assertEquals(
                listOf(1f, 1.75f),
                originalState.copyShiftState(currentScale = 0.5f).getShiftedSightMarkValues(),
        )
    }

    @Test
    fun testGetShiftAndScaleState_Shift() {
        // Shifting
        assertEquals(
                listOf(5.33f, 6.83f),
                originalState.copyShiftState(currentShift = 3.33f).getShiftedSightMarkValues(),
        )
        assertEquals(
                listOf(1.5f, 3f),
                originalState.copyShiftState(currentShift = -0.5f).getShiftedSightMarkValues(),
        )
    }

    @Test
    fun testGetShiftAndScaleState_Flip() {
        assertEquals(
                listOf(3.5f, 2f),
                originalState.copyShiftState(flipScale = true).getShiftedSightMarkValues(),
        )
    }

    @Test
    fun testGetShiftAndScaleState_ShiftAndScale() {
        assertEquals(
                listOf(5.2f, 8.2f),
                originalState.copyShiftState(currentShift = 1.2f, currentScale = 2f).getShiftedSightMarkValues(),
        )
    }

    @Test
    fun testGetShiftAndScaleState_ShiftAndFlip() {
        assertEquals(
                listOf(4.7f, 3.2f),
                originalState.copyShiftState(currentShift = 1.2f, flipScale = true).getShiftedSightMarkValues(),
        )
    }

    @Test
    fun testGetShiftAndScaleState_ScaleAndFlip() {
        assertEquals(
                listOf(7f, 4f),
                originalState.copyShiftState(currentScale = 2f, flipScale = true).getShiftedSightMarkValues(),
        )
    }

    @Test
    fun testGetShiftAndScaleState_ShiftAndScaleAndFlip() {
        assertEquals(
                listOf(8.2f, 5.2f),
                originalState
                        .copyShiftState(currentShift = 1.2f, currentScale = 2f, flipScale = true)
                        .getShiftedSightMarkValues(),
        )
    }

    @Test
    fun testGetShiftAndScaleState_Rounding() {
        fun scaleOriginalState(scale: Float): SightMarksState.Loaded {
            val newSightMarks = originalSightMarks.map { it.copy(sightMark = it.sightMark * scale) }
            return originalState.copy(
                    sightMarks = newSightMarks,
                    shiftAndScaleState = originalState.shiftAndScaleState!!.copy(
                            diagramHelper = SightMarksDiagramHelper(newSightMarks, false)
                    )
            )
        }

        assertEquals(
                listOf(2.11f, 3.61f),
                originalState.copyShiftState(currentShift = 0.11111f).getShiftedSightMarkValues(),
        )

        assertEquals(
                listOf(0.311f, 0.461f),
                scaleOriginalState(0.1f).copyShiftState(currentShift = 0.11111f).getShiftedSightMarkValues(),
        )

        assertEquals(
                listOf(20.1f, 35.1f),
                scaleOriginalState(10f).copyShiftState(currentShift = 0.11111f).getShiftedSightMarkValues(),
        )
    }
}
