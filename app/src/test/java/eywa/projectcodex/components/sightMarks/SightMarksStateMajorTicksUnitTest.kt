package eywa.projectcodex.components.sightMarks

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*

@RunWith(Parameterized::class)
class SightMarksStateMajorTicksUnitTest(private val param: Params) {
    @Test
    fun testMajorTicks() {
        val state = SightMarksState(
                sightMarks = listOfNotNull(
                        SightMark(30, true, Calendar.getInstance(), param.sightMarkA),
                        param.sightMarkB?.let { SightMark(50, false, Calendar.getInstance(), it) },
                ),
        )

        Assert.assertEquals(param.expectedMajorTickDifference, state.majorTickDifference)
        Assert.assertEquals(param.expectedMaxMajorTick, state.maxMajorTick)
        Assert.assertEquals(param.expectedMinMajorTick, state.minMajorTick)
        Assert.assertEquals(param.expectedTotalMajorTicks, state.totalMajorTicks)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun generate() = listOf(
                // General
                Params(3.15f, 2.1f, 1f, 4f, 2f, 2),
                // Magnitude
                Params(3.15f, 0.021f, 1f, 4f, 0f, 4),
                Params(0.315f, 0.021f, 0.1f, 0.4f, 0f, 4),
                Params(0.315f, 0.21f, 0.1f, 0.4f, 0.2f, 2),
                Params(31.5f, 2.1f, 10f, 40f, 0f, 4),
                // Negative
                Params(3.15f, -2.1f, 1f, 4f, -3f, 7),
                Params(-3.15f, 2.1f, 1f, 3f, -4f, 7),
                Params(-3.15f, -2.1f, 1f, -2f, -4f, 2),
                // Single
                Params(3.15f, null, 1f, 6f, 1f, 5),
                Params(0.0315f, null, 0.01f, 0.06f, 0.01f, 5),
                Params(31.5f, null, 10f, 60f, 10f, 5),
        )
    }

    data class Params(
            val sightMarkA: Float,
            val sightMarkB: Float?,
            val expectedMajorTickDifference: Float,
            val expectedMaxMajorTick: Float,
            val expectedMinMajorTick: Float,
            val expectedTotalMajorTicks: Int,
    ) {
        override fun toString(): String {
            return "$sightMarkA - $sightMarkB"
        }
    }
}
