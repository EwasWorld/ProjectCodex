package eywa.projectcodex.common.sharedUi.numberField

import eywa.projectcodex.R

/**
 * Groups validators together
 */
class NumberValidatorGroup<I : Number>(
        private val typeValidator: TypeValidator<I>,
        private vararg val validators: NumberValidator<in I>,
) {
    /**
     * @return null if there are no validation errors.
     * Otherwise returns the [NumberValidator.toErrorString] of the first violation.
     */
    fun getFirstError(value: String?, isDirty: Boolean = true, isRequired: Boolean = true): DisplayableError? {
        if (isRequired && isDirty && value.isNullOrBlank()) return StringResError(R.string.err__required_field)
        if (value.isNullOrBlank()) return null

        val parsed = typeValidator.transform(value)
                ?: return StringResError(typeValidator.regexFailedErrorMessageId)

        return validators.firstOrNull { !it.isValid(value, parsed) }
    }

    fun parse(value: String) = if (getFirstError(value) != null) null else typeValidator.transform(value)
}
