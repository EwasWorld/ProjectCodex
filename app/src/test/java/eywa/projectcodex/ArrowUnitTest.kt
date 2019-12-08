package eywa.projectcodex

import org.junit.Assert
import org.junit.Test
import java.lang.NumberFormatException


class ArrowUnitTest {
    @Test
    fun testGetArrowScore() {
        Assert.assertEquals(1, getArrowScore("1"))
        Assert.assertEquals(5, getArrowScore("5"))
        Assert.assertEquals(0, getArrowScore("m"))
        Assert.assertEquals(10, getArrowScore("10"))
        Assert.assertEquals(10, getArrowScore("X"))

        try {
            getArrowScore("-1")
            Assert.fail("Negative number")
        }
        catch (e: NumberFormatException) {
        }

        try {
            getArrowScore("sdgsgh")
            Assert.fail("Not a number")
        }
        catch (e: NumberFormatException) {
        }
    }

    @Test
    fun testToString() {
        Assert.assertEquals("m", Arrow("m").toString())
        Assert.assertEquals("1", Arrow("1").toString())
        Assert.assertEquals("4", Arrow("4").toString())
        Assert.assertEquals("10", Arrow("10").toString())
        Assert.assertEquals("X", Arrow("X").toString())
    }

    @Test
    fun testIsX() {
        Assert.assertEquals(true, isX("X"))
        Assert.assertEquals(true, isX("x"))
        Assert.assertEquals(false, isX("2"))
        Assert.assertEquals(false, isX("1"))
        Assert.assertEquals(false, isX("sdgsg"))
        Assert.assertEquals(false, isX("20"))
    }
}