package eywa.projectcodex.common.sharedUi.numberField

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual

sealed class NumberValidator<T : Number> {
    abstract val message: ResOrActual<String>?

    /**
     * Dummy value which can bypass the isRequired check which is on by default
     */
    data object NotRequired : NumberValidator<Number>() {
        override val message: ResOrActual<String>
            get() = throw UnsupportedOperationException()

        override fun isValid(value: String, parsed: Number): Boolean = true
    }

    data class InRange<T>(
            val range: ClosedRange<T>,
            override val message: ResOrActual<String> =
                    ResOrActual.StringResource(R.string.err__out_of_range, listOf(range.start, range.endInclusive)),
    ) : NumberValidator<T>() where T : Number, T : Comparable<T> {

        override fun isValid(value: String, parsed: T): Boolean = parsed in range
    }

    data class IsPositive(
            override val message: ResOrActual<String> = ResOrActual.StringResource(R.string.err__negative_is_invalid),
    ) : NumberValidator<Number>() {
        override fun isValid(value: String, parsed: Number): Boolean =
                Regex("-.*").matchEntire(value) == null
    }

    data class AtLeast<T>(
            val minInclusive: T,
            override val message: ResOrActual<String> =
                    ResOrActual.StringResource(R.string.err__invalid_must_be_at_least, listOf(minInclusive)),
    ) : NumberValidator<T>() where T : Number, T : Comparable<T> {
        override fun isValid(value: String, parsed: T): Boolean = parsed >= minInclusive
    }

    data class AtMost<T>(
            val maxInclusive: T,
            override val message: ResOrActual<String> =
                    ResOrActual.StringResource(R.string.err__invalid_must_be_at_most, listOf(maxInclusive)),
    ) : NumberValidator<T>() where T : Number, T : Comparable<T> {
        override fun isValid(value: String, parsed: T): Boolean = parsed <= maxInclusive
    }

    abstract fun isValid(value: String, parsed: T): Boolean
}
