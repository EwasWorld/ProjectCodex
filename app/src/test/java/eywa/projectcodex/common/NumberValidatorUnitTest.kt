package eywa.projectcodex.common

import eywa.projectcodex.common.sharedUi.NumberValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberValidatorUnitTest {
    @Test
    fun testPositive() {
        assertTrue(NumberValidator.POSITIVE.isValid("5"))
        assertFalse(NumberValidator.POSITIVE.isValid("-5"))
    }
}
