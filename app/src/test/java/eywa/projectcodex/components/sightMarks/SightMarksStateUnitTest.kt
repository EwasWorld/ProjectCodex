package eywa.projectcodex.components.sightMarks

import androidx.compose.ui.layout.Placeable
import org.junit.Assert.*
import org.junit.Test
import java.util.*
import kotlin.math.nextDown
import kotlin.math.nextUp

class SightMarksStateUnitTest {
    @Test
    fun testState_GetSightMarkAsPercentage() {
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
    fun testState_FormatString() {
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

    @Test
    fun testIndicatorGroup_isOverlapping() {
        val groupAt0 = createIndicatorGroup(50f)
        val groupAt50 = createIndicatorGroup(100f)
        val groupAt99 = createIndicatorGroup(50f.nextDown().nextDown())
        val groupAt100 = createIndicatorGroup(150f)
        val groupAt101 = createIndicatorGroup(50f.nextUp().nextUp())
        val groupAt200 = createIndicatorGroup(250f)

        // Overlapping
        assertTrue(groupAt0.isOverlapping(groupAt50))
        assertTrue(groupAt50.isOverlapping(groupAt0))
        assertTrue(groupAt0.isOverlapping(groupAt99))
        assertTrue(groupAt99.isOverlapping(groupAt0))
        assertTrue(groupAt100.isOverlapping(groupAt101))
        assertTrue(groupAt101.isOverlapping(groupAt100))

        // Not touching or overlapping
        assertFalse(groupAt0.isOverlapping(groupAt200))
        assertFalse(groupAt200.isOverlapping(groupAt0))

        // Touching
        assertFalse(groupAt0.isOverlapping(groupAt100))
        assertFalse(groupAt100.isOverlapping(groupAt0))
        assertFalse(groupAt100.isOverlapping(groupAt200))
        assertFalse(groupAt200.isOverlapping(groupAt100))
    }

    @Test
    fun testIndicatorGroup_merge() {
        val top = createIndicatorGroup(50f)

        fun check(expected: SightMarkIndicatorGroup, actual: SightMarkIndicatorGroup) {
            assertEquals(expected.indicators, actual.indicators)
            assertEquals(expected.topOffset, actual.topOffset)
        }

        val bottom = createIndicatorGroup(110f)
        val expectedStd = SightMarkIndicatorGroup(listOf(top.indicators.first(), bottom.indicators.first()), 80f)
        check(expectedStd, top.mergeWith(bottom))
        check(expectedStd, bottom.mergeWith(top))

        // Same overlap but bars are different sizes so larger one pulls smaller one's centre more
        val bottomBig = createIndicatorGroup(110f, indicatorHeight = 200)
        val expectedBig = SightMarkIndicatorGroup(listOf(top.indicators.first(), bottomBig.indicators.first()), 90f)
        check(expectedBig, top.mergeWith(bottomBig))
        check(expectedBig, bottomBig.mergeWith(top))
    }

    private fun createIndicatorGroup(centreOffset: Float, n: Int = 1, indicatorHeight: Int = 100) =
            SightMarkIndicatorGroup(List(n) { FakeSightMarkIndicator(indicatorHeight) }, centreOffset)

    data class FormatStringParams(
            val value: Float,
            val majorTickDifference: Float,
            val expected: String,
    )

    class FakeSightMarkIndicator(
            override val height: Int = 100
    ) : SightMarkIndicator {
        override val width: Int = 0
        override fun isLeft(): Boolean = false
        override val originalCentreOffset: Float
            get() = throw NotImplementedError()
        override val placeable: Placeable
            get() = throw NotImplementedError()
    }
}
