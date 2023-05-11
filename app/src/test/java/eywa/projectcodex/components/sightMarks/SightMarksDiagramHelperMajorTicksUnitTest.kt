package eywa.projectcodex.components.sightMarks

import eywa.projectcodex.components.sightMarks.diagram.SightMarksDiagramHelper
import eywa.projectcodex.model.SightMark
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*

@RunWith(Parameterized::class)
class SightMarksDiagramHelperMajorTicksUnitTest(private val param: Params) {
    @Test
    fun testMajorTicks() {
        val state = SightMarksDiagramHelper(
                sightMarks = listOfNotNull(
                        SightMark(1, 30, true, Calendar.getInstance(), param.sightMarkA),
                        param.sightMarkB?.let { SightMark(1, 50, false, Calendar.getInstance(), it) },
                ),
                isHighestNumberAtTheTop = true,
        )

        Assert.assertEquals(param.expectedMajorTickDifference, state.majorTickDifference, 0.0002f)
        Assert.assertEquals(param.expectedMaxMajorTick, state.maxMajorTick, 0.0002f)
        Assert.assertEquals(param.expectedMinMajorTick, state.minMajorTick, 0.0002f)
        Assert.assertEquals(param.expectedTotalMajorTicks, state.totalMajorTicks)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun generate() = listOf(
                /*
                 * General
                 */
                Params(3.15f, 2.1f, 1f, 4f, 2f, 2),
                // Note: actually want maxMajor to be 3.15 but float arithmetic errors are causing rounding errors
                Params(3.15f, 3.1f, 0.01f, 3.16f, 3.1f, 5),

                /*
                 * Magnitude
                 */
                Params(3.15f, 0.021f, 1f, 4f, 0f, 4),
                Params(0.315f, 0.021f, 0.1f, 0.4f, 0f, 4),
                Params(0.315f, 0.21f, 0.1f, 0.4f, 0.2f, 2),
                Params(31.5f, 2.1f, 10f, 40f, 0f, 4),

                /*
                 * Negative
                 */
                Params(3.15f, -2.1f, 1f, 4f, -3f, 7),
                Params(-3.15f, 2.1f, 1f, 3f, -4f, 7),
                Params(-3.15f, -2.1f, 1f, -2f, -4f, 2),

                /*
                 * Single sight mark
                 */
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
