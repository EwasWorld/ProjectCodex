package eywa.projectcodex.components.sightMarks

import eywa.projectcodex.components.sightMarks.ui.SightMarkIndicator
import eywa.projectcodex.components.sightMarks.ui.SightMarkIndicatorGroup
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.math.nextDown
import kotlin.math.nextUp

class SightMarkIndicatorGroupUnitTest {
    @Test
    fun testIsOverlapping() {
        val groupAt0 = createIndicatorGroup(50f)
        val groupAt50 = createIndicatorGroup(100f)
        val groupAt99 = createIndicatorGroup(50f.nextDown().nextDown())
        val groupAt100 = createIndicatorGroup(150f)
        val groupAt101 = createIndicatorGroup(50f.nextUp().nextUp())
        val groupAt200 = createIndicatorGroup(250f)

        // Overlapping
        assertTrue(groupAt0.isOverlapping(groupAt0))
        assertTrue(groupAt0.isOverlapping(groupAt0))
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
    fun testMerge() {
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

    @Test
    fun testMaxIndent() {
        fun checkMaxIndent(expected: Int, size: Int) {
            assertEquals(expected, createIndicatorGroup(n = size).maxIndentLevel())
        }

        checkMaxIndent(0, 1)
        checkMaxIndent(0, 2)
        checkMaxIndent(1, 3)
        checkMaxIndent(1, 4)
        checkMaxIndent(2, 5)
        checkMaxIndent(2, 6)
    }

    private fun createIndicatorGroup(
            centreOffset: Float = 0f,
            n: Int = 1,
            indicatorHeight: Int = 100,
    ) =
            SightMarkIndicatorGroup(
                    indicators = List(n) { createMockIndicator(indicatorHeight) },
                    centre = centreOffset,
            )

    private fun createMockIndicator(indicatorHeight: Int): SightMarkIndicator =
            mock {
                on { height } doReturn indicatorHeight
            }
}
