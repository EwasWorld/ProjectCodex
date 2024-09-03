package eywa.projectcodex.common.sharedUi.numberField

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.ResOrActual.StringResource

/**
 * Groups validators together
 */
class NumberValidatorGroup<I : Number>(
        private val typeValidator: TypeValidator<I>,
        private vararg val validators: NumberValidator<in I>,
) {
    /**
     * @return null if there are no validation errors.
     * Otherwise returns the [NumberValidator.message] of the first violation.
     */
    fun getFirstError(value: String?, isDirty: Boolean = true): ResOrActual<String>? {
        val isRequired = !validators.contains(NumberValidator.NotRequired)
        if (isRequired && isDirty && value.isNullOrBlank()) return StringResource(R.string.err__required_field)
        if (value.isNullOrBlank()) return null

        val parsed = typeValidator.transform(value)
                ?: return StringResource(typeValidator.regexFailedErrorMessageId)

        return validators.firstOrNull { !it.isValid(value, parsed) }?.message
    }

    fun parse(value: String) = if (getFirstError(value) != null) null else typeValidator.transform(value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NumberValidatorGroup<*>) return false

        if (typeValidator != other.typeValidator) return false
        if (!validators.contentEquals(other.validators)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = typeValidator.hashCode()
        result = 31 * result + validators.contentHashCode()
        return result
    }
}
