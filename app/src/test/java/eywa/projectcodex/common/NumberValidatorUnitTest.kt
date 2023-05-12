package eywa.projectcodex.common

import eywa.projectcodex.common.sharedUi.NumberValidator
import eywa.projectcodex.common.sharedUi.TypeValidator
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class NumberValidatorPositiveUnitTests(private val param: ValidatorTest<Any>) {
    @Test
    fun test() {
        assertEquals(param.isValid, NumberValidator.POSITIVE.isValid(param.testString))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun generate() = listOf<ValidatorTest<Any>>(
                ValidatorTest("5", true),
                ValidatorTest("0", true),
                ValidatorTest("0.1", true),
                ValidatorTest("", true),
                ValidatorTest("anything", true),

                ValidatorTest("-5", false),
                ValidatorTest("-0.1", false),
                ValidatorTest("-anything", false),
        )
    }
}

@RunWith(Parameterized::class)
class NumberValidatorFloatUnitTests(private val param: ValidatorTest<Float>) {
    @Test
    fun test() {
        assertEquals(param.isValid, validator.isValid(param.testString))
        assertEquals(param.isPartiallyValid, validator.isPartiallyValid(param.testString))
        assertEquals(param.parsedValue, validator.transform(param.testString))
    }

    companion object {
        private val validator = TypeValidator.FloatValidator

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun generate() = listOf(
                ValidatorTest("1.45", true, parsedValue = 1.45f),
                ValidatorTest("1", true, parsedValue = 1f),
                ValidatorTest("1.", true, parsedValue = 1f),
                ValidatorTest(".1", true, parsedValue = 0.1f),
                ValidatorTest("-1", true, parsedValue = -1f),
                ValidatorTest("-.1", true, parsedValue = -0.1f),
                ValidatorTest("", false, true),
                ValidatorTest("-", false, true),
                ValidatorTest(".", false, true),
                ValidatorTest("-.", false, true),
                ValidatorTest("a", false),
                ValidatorTest("50-", false),
        )
    }
}

@RunWith(Parameterized::class)
class NumberValidatorIntUnitTests(private val param: ValidatorTest<Float>) {
    @Test
    fun test() {
        assertEquals(param.isValid, validator.isValid(param.testString))
        assertEquals(param.isPartiallyValid, validator.isPartiallyValid(param.testString))
        assertEquals(param.parsedValue, validator.transform(param.testString))
    }

    companion object {
        private val validator = TypeValidator.IntValidator

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun generate() = listOf(
                ValidatorTest("5", true, parsedValue = 5),
                ValidatorTest("-50", true, parsedValue = -50),
                ValidatorTest("", false, true),
                ValidatorTest("-", false, true),
                ValidatorTest("5.0", false),
                ValidatorTest("a", false),
                ValidatorTest("50-", false),
        )
    }
}

data class ValidatorTest<I>(
        val testString: String,
        val isValid: Boolean,
        val isPartiallyValid: Boolean = isValid,
        val parsedValue: I? = null,
)
