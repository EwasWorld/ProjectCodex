package eywa.projectcodex.components.sightMarks

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class SightMarksStateUnitTest {
    @Test
    fun testGetSightMarkAsPercentage() {
        val sights = listOf(
                SightMark(30, true, Calendar.getInstance(), 5f),
                SightMark(30, true, Calendar.getInstance(), 4.5f),
                SightMark(30, true, Calendar.getInstance(), 4f),
                SightMark(30, true, Calendar.getInstance(), 3.5f),
                SightMark(50, false, Calendar.getInstance(), 3f),
        )
        val state = SightMarksState(sightMarks = sights, isHighestNumberAtTheTop = false)

        assertEquals(
                sights.map { (it.sightMark - 3f) / 2f },
                sights.map { state.getSightMarkAsPercentage(it) },
        )
    }

    @Test
    fun testFormatString() {
        listOf(
                FormatStringParams(366.2399f, 100f, "400"),
                FormatStringParams(366.2399f, 10f, "370"),
                FormatStringParams(36.23999f, 10f, "40"),
                FormatStringParams(36.23999f, 1f, "36"),
                FormatStringParams(36.23999f, 0.1f, "36.2"),
                FormatStringParams(36.23999f, 0.01f, "36.24"),
                FormatStringParams(30f, 0.1f, "30.0"),
                FormatStringParams(30f, 0.001f, "30.000"),
        ).forEach { params ->
            val state = SightMarksState(
                    sightMarks = listOf(SightMark(10, true, Calendar.getInstance(), params.majorTickDifference))
            )
            assertEquals(params.expected, state.formatTickLabel(params.value))
        }
    }

    data class FormatStringParams(
            val value: Float,
            val majorTickDifference: Float,
            val expected: String,
    )
}
