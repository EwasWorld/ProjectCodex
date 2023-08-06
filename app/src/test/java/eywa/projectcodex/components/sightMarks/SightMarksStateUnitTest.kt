package eywa.projectcodex.components.sightMarks

import eywa.projectcodex.model.SightMark
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class SightMarksStateUnitTest {
    private fun createSightMark(sightMark: Float) =
            SightMark(1, 10, true, Calendar.getInstance(), sightMark)

    private fun SightMarksState.Loaded.getShiftedSightMarkValues() =
            getShiftAndScaleState().sightMarks.map { it.sightMark }


    @Test
    fun testGetShiftAndScaleState_Unchanged() {
        val sightMarks = SightMarksState.Loaded(
                sightMarks = listOf(2f, 3.5f).map { createSightMark(it) },
                shiftAmount = 0f,
                scaleAmount = 1f,
        )

        // Not in preview mode
        assertEquals(
                listOf(2f, 3.5f),
                sightMarks.copy(scaleAmount = null, shiftAmount = null).getShiftedSightMarkValues(),
        )
        // No scaling/shift
        assertEquals(
                listOf(2f, 3.5f),
                sightMarks.getShiftedSightMarkValues(),
        )
    }


    @Test
    fun testGetShiftAndScaleState_Scale() {
        val sightMarks = SightMarksState.Loaded(
                sightMarks = listOf(2f, 3.5f).map { createSightMark(it) },
                shiftAmount = 0f,
                scaleAmount = 1f,
        )

        // Scaling
        assertEquals(
                listOf(4f, 7f),
                sightMarks.copy(scaleAmount = 2f).getShiftedSightMarkValues(),
        )
        assertEquals(
                listOf(1f, 1.75f),
                sightMarks.copy(scaleAmount = 0.5f).getShiftedSightMarkValues(),
        )
    }

    @Test
    fun testGetShiftAndScaleState_Shift() {
        val sightMarks = SightMarksState.Loaded(
                sightMarks = listOf(2f, 3.5f).map { createSightMark(it) },
                shiftAmount = 0f,
                scaleAmount = 1f,
        )

        // Shifting
        assertEquals(
                listOf(5.33f, 6.83f),
                sightMarks.copy(shiftAmount = 3.33f).getShiftedSightMarkValues(),
        )
        assertEquals(
                listOf(1.5f, 3f),
                sightMarks.copy(shiftAmount = -0.5f).getShiftedSightMarkValues(),
        )
    }

    @Test
    fun testGetShiftAndScaleState_Flip() {
        val sightMarks = SightMarksState.Loaded(
                sightMarks = listOf(2f, 3.5f).map { createSightMark(it) },
                shiftAmount = 0f,
                scaleAmount = 1f,
        )

        assertEquals(
                listOf(3.5f, 2f),
                sightMarks.copy(flipScale = true).getShiftedSightMarkValues(),
        )
    }

    @Test
    fun testGetShiftAndScaleState_ShiftAndScale() {
        val sightMarks = SightMarksState.Loaded(
                sightMarks = listOf(2f, 3.5f).map { createSightMark(it) },
                shiftAmount = 0f,
                scaleAmount = 1f,
        )

        assertEquals(
                listOf(5.2f, 8.2f),
                sightMarks.copy(shiftAmount = 1.2f, scaleAmount = 2f).getShiftedSightMarkValues(),
        )
    }

    @Test
    fun testGetShiftAndScaleState_ShiftAndFlip() {
        val sightMarks = SightMarksState.Loaded(
                sightMarks = listOf(2f, 3.5f).map { createSightMark(it) },
                shiftAmount = 0f,
                scaleAmount = 1f,
        )

        assertEquals(
                listOf(4.7f, 3.2f),
                sightMarks.copy(shiftAmount = 1.2f, flipScale = true).getShiftedSightMarkValues(),
        )
    }

    @Test
    fun testGetShiftAndScaleState_ScaleAndFlip() {
        val sightMarks = SightMarksState.Loaded(
                sightMarks = listOf(2f, 3.5f).map { createSightMark(it) },
                shiftAmount = 0f,
                scaleAmount = 1f,
        )

        assertEquals(
                listOf(7f, 4f),
                sightMarks.copy(scaleAmount = 2f, flipScale = true).getShiftedSightMarkValues(),
        )
    }

    @Test
    fun testGetShiftAndScaleState_ShiftAndScaleAndFlip() {
        val sightMarks = SightMarksState.Loaded(
                sightMarks = listOf(2f, 3.5f).map { createSightMark(it) },
                shiftAmount = 0f,
                scaleAmount = 1f,
        )

        assertEquals(
                listOf(8.2f, 5.2f),
                sightMarks.copy(shiftAmount = 1.2f, scaleAmount = 2f, flipScale = true).getShiftedSightMarkValues(),
        )
    }
}
