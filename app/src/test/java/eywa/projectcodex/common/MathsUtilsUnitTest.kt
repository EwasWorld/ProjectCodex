package eywa.projectcodex.common

import eywa.projectcodex.common.utils.roundToDp
import eywa.projectcodex.common.utils.standardDeviation
import org.junit.Assert.assertEquals
import org.junit.Test

class MathsUtilsUnitTest {
    @Test
    fun testStandardDeviation() {
        val items = listOf(10, 12, 23, 23, 16, 23, 21, 16)

        assertEquals(4.89898f, items.standardDeviation(), 0.00005f)
        assertEquals(5.23722f, items.standardDeviation(true), 0.00005f)
    }

    @Test
    fun testRoundToDp() {
        assertEquals(5.24f, 5.23722f.roundToDp(2))
        assertEquals(5.2f, 5.23722f.roundToDp(1))
        assertEquals(5f, 5.23722f.roundToDp(0))
    }
}
