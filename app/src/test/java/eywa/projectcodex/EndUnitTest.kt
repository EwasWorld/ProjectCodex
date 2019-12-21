package eywa.projectcodex

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import eywa.projectcodex.database.ScoresViewModel
import eywa.projectcodex.database.entities.ArrowValue
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.lang.IllegalArgumentException


/**
 * See [testing documentation](http://d.android.com/tools/testing).
 * https://medium.com/@daptronic/unit-testing-android-resources-with-kotlin-and-resourceprovider-65874997aa
 * https://github.com/Comcast/resourceprovider
 */
class EndUnitTest {
    private var end: End = End(6, ".", "-")
    private var arrows = arrayOf(
            Arrow(0, false), Arrow(1, false), Arrow(2, false),
            Arrow(3, false), Arrow(4, false), Arrow(5, false),
            Arrow(6, false), Arrow(7, false), Arrow(8, false),
            Arrow(9, false), Arrow(10, false), Arrow(10, true)
    )

    @Before
    fun setup() {
        end = End(6, ".", "-")
    }

    @Test
    fun testCreateEndFromList() {
        val arrowValues = arrows.map { ArrowValue(1, 1, it.score, it.isX) }

        val endArrows = mutableListOf(arrowValues[0], arrowValues[3], arrowValues[11])
        end = End(endArrows, 6, ".", "-")
        assertEquals(13, end.getScore())
        assertEquals(1, end.getGolds(GoldsType.XS))

        endArrows.add(arrowValues[1])
        endArrows.add(arrowValues[1])
        endArrows.add(arrowValues[1])
        end = End(endArrows, 6, ".", "-")
        assertEquals(16, end.getScore())

        try {
            endArrows.add(arrowValues[1])
            end = End(endArrows, 6, ".", "-")
            fail("Too many arrows")
        }
        catch (e: IllegalArgumentException) {
        }
    }

    @Test
    fun testAddArrowStringToEnd() {
        end.addArrowToEnd("1")
        assertEquals(1, end.getScore())

        end.addArrowToEnd("m")
        assertEquals(1, end.getScore())

        end.addArrowToEnd("X")
        assertEquals(11, end.getScore())

        end.addArrowToEnd("3")
        end.addArrowToEnd("3")
        end.addArrowToEnd("3")
        assertEquals(20, end.getScore())

        try {
            end.addArrowToEnd("3")
            fail("Too many arrows")
        }
        catch (e: IllegalStateException) {
        }

        try {
            end.addArrowToEnd("-1")
            fail("Negative number")
        }
        catch (e: NumberFormatException) {
        }

        try {
            end.addArrowToEnd("sdgsgh")
            fail("Not a number")
        }
        catch (e: NumberFormatException) {
        }

        assertEquals(20, end.getScore())
    }

    @Test
    fun testAddArrowValueToEnd() {
        end.addArrowToEnd(arrows[1])
        assertEquals(1, end.getScore())

        end.addArrowToEnd(arrows[0])
        assertEquals(1, end.getScore())

        end.addArrowToEnd(arrows[11])
        assertEquals(11, end.getScore())

        end.addArrowToEnd(arrows[3])
        end.addArrowToEnd(arrows[3])
        end.addArrowToEnd(arrows[3])
        assertEquals(20, end.getScore())

        assertEquals(20, end.getScore())
    }

    @Test
    fun testRemoveLastArrowFromEnd() {
        try {
            end.removeLastArrowFromEnd()
            fail("No arrows in end")
        }
        catch (e: IllegalStateException) {
        }

        end.addArrowToEnd(arrows[7])
        end.addArrowToEnd(arrows[3])
        end.addArrowToEnd(arrows[5])
        assertEquals(15, end.getScore())

        end.removeLastArrowFromEnd()
        assertEquals(10, end.getScore())

        end.removeLastArrowFromEnd()
        assertEquals(7, end.getScore())

        end.removeLastArrowFromEnd()
        assertEquals(0, end.getScore())
    }

    @Test
    fun testGetHits() {
        assertEquals(0, end.getHits())

        end.addArrowToEnd(arrows[0])
        assertEquals(0, end.getHits())

        end.addArrowToEnd(arrows[1])
        assertEquals(1, end.getHits())

        end.addArrowToEnd(arrows[0])
        assertEquals(1, end.getHits())

        end.addArrowToEnd(arrows[4])
        end.addArrowToEnd(arrows[7])
        end.addArrowToEnd(arrows[11])
        assertEquals(4, end.getHits())
    }

    @Test
    fun testGetGolds() {
        assertEquals(0, end.getGolds(GoldsType.NINES))

        end.addArrowToEnd(arrows[0])
        end.addArrowToEnd(arrows[4])
        assertEquals(0, end.getGolds(GoldsType.NINES))

        end.addArrowToEnd(arrows[10])
        end.addArrowToEnd(arrows[11])
        assertEquals(2, end.getGolds(GoldsType.NINES))
    }

    @Test
    fun testToString() {
        assertEquals(".-.-.-.-.-.", end.toString())

        end.addArrowToEnd(arrows[1])
        assertEquals("1-.-.-.-.-.", end.toString())

        end.addArrowToEnd(arrows[0])
        end.addArrowToEnd(arrows[3])
        end.addArrowToEnd(arrows[11])
        assertEquals("1-m-3-X-.-.", end.toString())

        end.addArrowToEnd(arrows[7])
        end.addArrowToEnd(arrows[7])
        assertEquals("1-m-3-X-7-7", end.toString())
    }

    @Test
    fun testReorderScores() {
        end.addArrowToEnd(arrows[1])
        assertEquals("1-.-.-.-.-.", end.toString())

        end.reorderScores()
        assertEquals("1-.-.-.-.-.", end.toString())

        end.addArrowToEnd(arrows[11])
        end.addArrowToEnd(arrows[0])
        end.addArrowToEnd(arrows[3])
        end.addArrowToEnd(arrows[10])
        assertEquals("1-X-m-3-10-.", end.toString())

        end.reorderScores()
        assertEquals("X-10-3-1-m-.", end.toString())
    }

    @Test
    fun testAddArrowsToDatabase() {
        val arrowScores = arrayOf(1, 3, 5, 6, 10, 10)
        for (arrow in arrowScores) {
            end.addArrowToEnd(arrows[arrow])
        }
        // Swap the last arrow to an X
        end.removeLastArrowFromEnd()
        end.addArrowToEnd(arrows[11])
        assertEquals(arrowScores.sum(), end.getScore())

        val viewModel = mock<ScoresViewModel>()
        val archerRoundID = 1
        var arrowNumber = 1
        end.addArrowsToDatabase(archerRoundID, arrowNumber, viewModel)

        argumentCaptor<ArrowValue>().apply {
            verify(viewModel, times(6)).insert(capture())
            for (arrow in allValues) {
                assertEquals(archerRoundID, arrow.archerRoundsID)
                assertEquals(arrowNumber, arrow.arrowNumber)
                assertEquals(arrowScores[arrowNumber - 1], arrow.score)
                // Only the last arrow, 6, should be an X
                assertEquals(arrowNumber == 6, arrow.isX)
                arrowNumber++
            }
        }
    }

    @Test
    fun testClear() {
        assertEquals(".-.-.-.-.-.", end.toString())
        end.clear()
        assertEquals(".-.-.-.-.-.", end.toString())

        end.addArrowToEnd(arrows[1])
        assertEquals("1-.-.-.-.-.", end.toString())
        end.clear()
        assertEquals(".-.-.-.-.-.", end.toString())

        end.addArrowToEnd(arrows[1])
        end.addArrowToEnd(arrows[0])
        end.addArrowToEnd(arrows[3])
        end.addArrowToEnd(arrows[11])
        end.addArrowToEnd(arrows[7])
        end.addArrowToEnd(arrows[7])
        assertEquals("1-m-3-X-7-7", end.toString())
        end.clear()
        assertEquals(".-.-.-.-.-.", end.toString())
    }
}
