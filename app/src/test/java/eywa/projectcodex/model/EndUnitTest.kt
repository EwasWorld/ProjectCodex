package eywa.projectcodex.model

import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.database.UpdateType
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.exceptions.UserException
import eywa.projectcodex.testUtils.TestData
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*


/**
 * See [testing documentation](http://d.android.com/tools/testing).
 * https://medium.com/@daptronic/unit-testing-android-resources-with-kotlin-and-resourceprovider-65874997aa
 * https://github.com/Comcast/resourceprovider
 */
class EndUnitTest {
    private lateinit var end: End
    private val endSize = 6
    val shootId = 1

    @Before
    fun setup() {
        CustomLogger.customLogger = mock(CustomLogger::class.java)
        end = End(endSize, TestData.ARROW_PLACEHOLDER, TestData.ARROW_DELIMINATOR)
    }

    @Test
    fun testCreateEndFromList() {
        val arrowScores = TestData.ARROWS.map { DatabaseArrowScore(1, 1, it.score, it.isX) }

        val endArrows = mutableListOf(arrowScores[0], arrowScores[3], arrowScores[11])
        end = End(endArrows, TestData.ARROW_PLACEHOLDER, TestData.ARROW_DELIMINATOR)
        assertEquals(13, end.getScore())
        assertEquals(1, end.getGolds(GoldsType.XS))

        endArrows.add(arrowScores[1])
        endArrows.add(arrowScores[1])
        endArrows.add(arrowScores[1])
        end = End(endArrows, TestData.ARROW_PLACEHOLDER, TestData.ARROW_DELIMINATOR)
        assertEquals(16, end.getScore())
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
    fun testAddArrowScoreToEnd() {
        end.addArrowToEnd(TestData.ARROWS[1])
        assertEquals(1, end.getScore())

        end.addArrowToEnd(TestData.ARROWS[0])
        assertEquals(1, end.getScore())

        end.addArrowToEnd(TestData.ARROWS[11])
        assertEquals(11, end.getScore())

        end.addArrowToEnd(TestData.ARROWS[3])
        end.addArrowToEnd(TestData.ARROWS[3])
        end.addArrowToEnd(TestData.ARROWS[3])
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

        end.addArrowToEnd(TestData.ARROWS[7])
        end.addArrowToEnd(TestData.ARROWS[3])
        end.addArrowToEnd(TestData.ARROWS[5])
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

        end.addArrowToEnd(TestData.ARROWS[0])
        assertEquals(0, end.getHits())

        end.addArrowToEnd(TestData.ARROWS[1])
        assertEquals(1, end.getHits())

        end.addArrowToEnd(TestData.ARROWS[0])
        assertEquals(1, end.getHits())

        end.addArrowToEnd(TestData.ARROWS[4])
        end.addArrowToEnd(TestData.ARROWS[7])
        end.addArrowToEnd(TestData.ARROWS[11])
        assertEquals(4, end.getHits())
    }

    @Test
    fun testGetGolds() {
        assertEquals(0, end.getGolds(GoldsType.NINES))

        end.addArrowToEnd(TestData.ARROWS[0])
        end.addArrowToEnd(TestData.ARROWS[4])
        assertEquals(0, end.getGolds(GoldsType.NINES))

        end.addArrowToEnd(TestData.ARROWS[10])
        end.addArrowToEnd(TestData.ARROWS[11])
        assertEquals(2, end.getGolds(GoldsType.NINES))
    }

    @Test
    fun testToString() {
        assertEquals(".-.-.-.-.-.", end.toString())

        end.addArrowToEnd(TestData.ARROWS[1])
        assertEquals("1-.-.-.-.-.", end.toString())

        end.addArrowToEnd(TestData.ARROWS[0])
        end.addArrowToEnd(TestData.ARROWS[3])
        end.addArrowToEnd(TestData.ARROWS[11])
        assertEquals("1-m-3-X-.-.", end.toString())

        end.addArrowToEnd(TestData.ARROWS[7])
        end.addArrowToEnd(TestData.ARROWS[7])
        assertEquals("1-m-3-X-7-7", end.toString())
    }

    @Test
    fun testReorderScores() {
        end.addArrowToEnd(TestData.ARROWS[1])
        assertEquals("1-.-.-.-.-.", end.toString())

        end.reorderScores()
        assertEquals("1-.-.-.-.-.", end.toString())

        end.addArrowToEnd(TestData.ARROWS[11])
        end.addArrowToEnd(TestData.ARROWS[0])
        end.addArrowToEnd(TestData.ARROWS[3])
        end.addArrowToEnd(TestData.ARROWS[10])
        assertEquals("1-X-m-3-10-.", end.toString())

        end.reorderScores()
        assertEquals("X-10-3-1-m-.", end.toString())
    }

    @Test
    fun testAddArrowsToDatabase() {
        val arrowScores = arrayOf(1, 3, 5, 6, 10, 10)
        for (arrow in arrowScores) {
            end.addArrowToEnd(TestData.ARROWS[arrow])
        }
        // Swap the last arrow to an X
        end.removeLastArrowFromEnd()
        end.addArrowToEnd(TestData.ARROWS[11])
        assertEquals(arrowScores.sum(), end.getScore())

        var arrowNumber = 1
        val output = end.getDatabaseUpdates(shootId, arrowNumber)
        assertEquals(UpdateType.NEW, output.first)
        for (arrow in output.second) {
            assertEquals(shootId, arrow.shootId)
            assertEquals(arrowNumber, arrow.arrowNumber)
            assertEquals(arrowScores[arrowNumber - 1], arrow.score)
            // Only the last arrow, 6, should be an X
            assertEquals(arrowNumber == 6, arrow.isX)
            arrowNumber++
        }
    }

    @Test
    fun testAddArrowsToDatabaseEditEnd() {
        val oldArrows = listOf(
                DatabaseArrowScore(shootId, 4, 3, false),
                DatabaseArrowScore(shootId, 6, 6, false),
                DatabaseArrowScore(shootId, 7, 7, false),
                DatabaseArrowScore(shootId, 8, 10, true)
        )
        val arrowScores = listOf(1, 5, 6, 10)
        val end = End(oldArrows, TestData.ARROW_PLACEHOLDER, TestData.ARROW_DELIMINATOR)
        end.clear()
        for (arrow in arrowScores) {
            end.addArrowToEnd(TestData.ARROWS[arrow])
        }
        assertEquals(arrowScores.sum(), end.getScore())

        val firstArrowNumber = 10
        val output = end.getDatabaseUpdates(shootId, firstArrowNumber)
        assertEquals(UpdateType.UPDATE, output.first)
        for (i in output.second.indices) {
            assertEquals(shootId, output.second[i].shootId)
            assertEquals(
                    if (i < oldArrows.size) oldArrows[i].arrowNumber else i + firstArrowNumber - oldArrows.size,
                    output.second[i].arrowNumber
            )
            assertEquals(arrowScores[i], output.second[i].score)
            assertEquals(false, output.second[i].isX)
        }
    }

    @Test
    fun testClear() {
        assertEquals(".-.-.-.-.-.", end.toString())
        end.clear()
        assertEquals(".-.-.-.-.-.", end.toString())

        end.addArrowToEnd(TestData.ARROWS[1])
        assertEquals("1-.-.-.-.-.", end.toString())
        end.clear()
        assertEquals(".-.-.-.-.-.", end.toString())

        end.addArrowToEnd(TestData.ARROWS[1])
        end.addArrowToEnd(TestData.ARROWS[0])
        end.addArrowToEnd(TestData.ARROWS[3])
        end.addArrowToEnd(TestData.ARROWS[11])
        end.addArrowToEnd(TestData.ARROWS[7])
        end.addArrowToEnd(TestData.ARROWS[7])
        assertEquals("1-m-3-X-7-7", end.toString())
        end.clear()
        assertEquals(".-.-.-.-.-.", end.toString())
    }

    /**
     * Test reduce end size when the new size is valid based on current arrows (do not delete extra arrows)
     */
    @Test
    fun testReduceEndSize() {
        for (i in 0 until 2) {
            end.addArrowToEnd(TestData.ARROWS[i])
        }
        val listener = mock(End.UpdateEndSizeListener::class.java)
        end.updateEndSizeListener = listener

        end.updateEndSize(4, false)
        assertEquals(4, end.endSize)
        assertEquals("m-1-.-.", end.toString())

        verify(listener, times(1)).onEndSizeUpdated()
    }

    /**
     * Test reduce end size when the new size is too small based on current arrows (do not delete extra arrows)
     */
    @Test(expected = IllegalArgumentException::class)
    fun testReduceEndSizeTooSmall() {
        val listener = mock(End.UpdateEndSizeListener::class.java)
        end.updateEndSizeListener = listener
        for (i in 0 until endSize) {
            end.addArrowToEnd(TestData.ARROWS[0])
        }

        end.updateEndSize(4, false)
    }

    /**
     * Test not allowed to change the end size when editing an end
     */
    @Test(expected = UserException::class)
    fun testReduceEndSizeEditEnd() {
        val oldArrows = listOf(
                DatabaseArrowScore(shootId, 1, 3, false),
                DatabaseArrowScore(shootId, 2, 3, false),
                DatabaseArrowScore(shootId, 3, 3, false),
                DatabaseArrowScore(shootId, 4, 6, false),
                DatabaseArrowScore(shootId, 5, 7, false),
                DatabaseArrowScore(shootId, 6, 10, true)
        )
        end = End(oldArrows, TestData.ARROW_PLACEHOLDER, TestData.ARROW_DELIMINATOR)
        val listener = mock(End.UpdateEndSizeListener::class.java)
        end.updateEndSizeListener = listener

        end.updateEndSize(12, false)
    }

    /**
     * Test reduce end size when the new size is valid based on current arrows (empty end - delete extra arrows)
     */
    @Test
    fun testReduceEndSizeDeleteContentsEmptyEnd() {
        val listener = mock(End.UpdateEndSizeListener::class.java)
        end.updateEndSizeListener = listener

        end.updateEndSize(4, true)
        assertEquals(4, end.endSize)
        assertEquals(".-.-.-.", end.toString())

        verify(listener, times(1)).onEndSizeUpdated()
    }

    /**
     * Test reduce end size when the new size is valid based on current arrows (small end - delete extra arrows)
     */
    @Test
    fun testReduceEndSizeDeleteContentsSmallEnd() {
        for (i in 0 until 2) {
            end.addArrowToEnd(TestData.ARROWS[i])
        }
        val listener = mock(End.UpdateEndSizeListener::class.java)
        end.updateEndSizeListener = listener

        end.updateEndSize(4, true)
        assertEquals(4, end.endSize)
        assertEquals("m-1-.-.", end.toString())

        verify(listener, times(1)).onEndSizeUpdated()
    }

    /**
     * Test reduce end size when the new size is too small based on current arrows (delete extra arrows)
     */
    @Test
    fun testReduceEndSizeDeleteContentsLargeEnd() {
        for (i in 0 until endSize) {
            end.addArrowToEnd(TestData.ARROWS[i])
        }
        val listener = mock(End.UpdateEndSizeListener::class.java)
        end.updateEndSizeListener = listener

        end.updateEndSize(4, true)
        assertEquals(4, end.endSize)
        assertEquals("m-1-2-3", end.toString())

        verify(listener, times(1)).onEndSizeUpdated()
    }

    @Test
    fun testUpdateEndSizeRemainingArrows() {
        for (i in 0 until endSize) {
            end.addArrowToEnd(TestData.ARROWS[i])
        }
        val listener = mock(End.UpdateEndSizeListener::class.java)
        end.updateEndSizeListener = listener

        end.distanceRemainingArrows = 4
        assertEquals(4, end.endSize)

        end.distanceRemainingArrows = 24
        assertEquals(endSize, end.endSize)

        verify(listener, times(2)).onEndSizeUpdated()
    }
}
