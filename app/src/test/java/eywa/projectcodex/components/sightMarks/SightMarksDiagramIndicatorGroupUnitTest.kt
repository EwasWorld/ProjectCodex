package eywa.projectcodex.components.sightMarks

import eywa.projectcodex.components.sightMarks.diagram.SightMarkIndicatorGroup
import eywa.projectcodex.components.sightMarks.diagram.SightMarksDiagramIndicator
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.math.nextDown
import kotlin.math.nextUp

class SightMarksDiagramIndicatorGroupUnitTest {
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
        // TODO Update for new merge logic
        val top = createIndicatorGroup(50f, sightMark = 2f)

        fun check(expected: SightMarkIndicatorGroup, actual: SightMarkIndicatorGroup) {
            assertEquals(expected.indicators, actual.indicators)
            assertEquals(expected.topOffset, actual.topOffset)
        }

        fun test(
                expected: SightMarkIndicatorGroup,
                a: SightMarkIndicatorGroup,
                b: SightMarkIndicatorGroup,
                highestAtTop: Boolean,
        ) {
            check(expected, a.mergeWith(b, highestAtTop))
            check(expected, b.mergeWith(a, highestAtTop))
        }

        // Both same size and at same mark
        val bottom = createIndicatorGroup(110f, sightMark = 2f)
        val expectedStd = SightMarkIndicatorGroup(listOf(top.indicators.first(), bottom.indicators.first()), 80f)
        test(expectedStd, top, bottom, false)

        // Mark but bars are different sizes so larger one pulls smaller one's centre more
        val bottomBig = createIndicatorGroup(110f, indicatorHeight = 200, sightMark = 2f)
        val expectedBig = SightMarkIndicatorGroup(listOf(top.indicators.first(), bottomBig.indicators.first()), 90f)
        test(expectedBig, top, bottomBig, false)
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
            sightMark: Float = 2f,
    ) =
            SightMarkIndicatorGroup(
                    indicators = List(n) { createMockIndicator(indicatorHeight, sightMark) },
                    centre = centreOffset,
            )

    private fun createMockIndicator(
            indicatorHeight: Int,
            sightMark: Float,
    ): SightMarksDiagramIndicator =
            mock {
                on { height() } doReturn indicatorHeight
                on { this.sightMark } doReturn sightMark
            }
}
