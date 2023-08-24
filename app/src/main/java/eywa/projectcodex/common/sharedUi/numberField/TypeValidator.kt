package eywa.projectcodex.common.sharedUi.numberField

import eywa.projectcodex.R

/**
 * Check that the format of a [String] conforms to a particular number type e.g. [Float].
 * Provide a transformation function to convert from [String] to the given type.
 */
sealed class TypeValidator<I : Number>(
        private val partialRegex: Regex,
        val regexFailedErrorMessageId: Int,
) {
    object FloatValidator : TypeValidator<Float>(
            partialRegex = Regex("-?[0-9]*\\.?[0-9]*"),
            regexFailedErrorMessageId = R.string.err__invalid_float_digit,
    ) {
        override fun transform(value: String) = value.toFloatOrNull()
    }

    object IntValidator : TypeValidator<Int>(
            partialRegex = Regex("-?[0-9]*"),
            regexFailedErrorMessageId = R.string.err__invalid_int_digit,
    ) {
        override fun transform(value: String) = value.toIntOrNull()
    }

    abstract fun transform(value: String): I?

    fun isValid(value: String): Boolean = transform(value) != null
    fun isPartiallyValid(value: String): Boolean = partialRegex.matchEntire(value) != null
}
