package eywa.projectcodex.common.sharedUi.numberField

import android.content.res.Resources
import eywa.projectcodex.R

sealed class NumberValidator<T : Number> : DisplayableError {
    /**
     * Dummy value which can bypass the isRequired check which is on by default
     */
    object NotRequired : NumberValidator<Number>() {
        override fun isValid(value: String, parsed: Number): Boolean = true

        override fun toErrorString(resources: Resources): String = ""
    }

    data class InRange<T>(val range: ClosedRange<T>) : NumberValidator<T>() where T : Number, T : Comparable<T> {
        override fun isValid(value: String, parsed: T): Boolean = parsed in range

        override fun toErrorString(resources: Resources): String =
                resources.getString(R.string.err__out_of_range, range.start, range.endInclusive)
    }

    object IsPositive : NumberValidator<Number>() {
        override fun isValid(value: String, parsed: Number): Boolean =
                Regex("-.*").matchEntire(value) == null

        override fun toErrorString(resources: Resources): String =
                resources.getString(R.string.err__negative_is_invalid)
    }

    data class AtLeast<T>(val minInclusive: T) : NumberValidator<T>() where T : Number, T : Comparable<T> {
        override fun isValid(value: String, parsed: T): Boolean = parsed >= minInclusive

        override fun toErrorString(resources: Resources): String =
                resources.getString(R.string.err__invalid_must_be_at_least, minInclusive)
    }

    data class AtMost<T>(val maxInclusive: T) : NumberValidator<T>() where T : Number, T : Comparable<T> {
        override fun isValid(value: String, parsed: T): Boolean = parsed <= maxInclusive

        override fun toErrorString(resources: Resources): String =
                resources.getString(R.string.err__invalid_must_be_at_most, maxInclusive)
    }

    abstract fun isValid(value: String, parsed: T): Boolean
}
