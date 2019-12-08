package eywa.projectcodex

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.lang.NumberFormatException

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 * https://medium.com/@daptronic/unit-testing-android-resources-with-kotlin-and-resourceprovider-65874997aa
 * https://github.com/Comcast/resourceprovider
 */
@RunWith(MockitoJUnitRunner.Silent::class)
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
    fun testAddArrowStringToEnd() {
        end.addArrowToEnd("1")
        assertEquals(1, end.getEndScore())

        end.addArrowToEnd("m")
        assertEquals(1, end.getEndScore())

        end.addArrowToEnd("X")
        assertEquals(11, end.getEndScore())

        end.addArrowToEnd("3")
        end.addArrowToEnd("3")
        end.addArrowToEnd("3")
        assertEquals(20, end.getEndScore())

        try {
            end.addArrowToEnd("3")
            fail("Too many arrows")
        }
        catch (e: NullPointerException) {
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

        assertEquals(20, end.getEndScore())
    }

    @Test
    fun testAddArrowValueToEnd() {
        end.addArrowToEnd(arrows[1])
        assertEquals(1, end.getEndScore())

        end.addArrowToEnd(arrows[0])
        assertEquals(1, end.getEndScore())

        end.addArrowToEnd(arrows[11])
        assertEquals(11, end.getEndScore())

        end.addArrowToEnd(arrows[3])
        end.addArrowToEnd(arrows[3])
        end.addArrowToEnd(arrows[3])
        assertEquals(20, end.getEndScore())

        assertEquals(20, end.getEndScore())
    }

    @Test
    fun testRemoveLastArrowFromEnd() {
        try {
            end.removeLastArrowFromEnd()
            fail("No arrows in end")
        }
        catch (e: NullPointerException) {
        }

        end.addArrowToEnd(arrows[7])
        end.addArrowToEnd(arrows[3])
        end.addArrowToEnd(arrows[5])
        assertEquals(15, end.getEndScore())

        end.removeLastArrowFromEnd()
        assertEquals(10, end.getEndScore())

        end.removeLastArrowFromEnd()
        assertEquals(7, end.getEndScore())

        end.removeLastArrowFromEnd()
        assertEquals(0, end.getEndScore())
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
        assertEquals("m-1-3-10-X-.", end.toString())
    }

    @Test
    fun testAddArrowsToDatabase() {
        // TODO Work out how to mock scoresViewModel
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
