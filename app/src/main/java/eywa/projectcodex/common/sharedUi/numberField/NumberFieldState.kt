package eywa.projectcodex.common.sharedUi.numberField

class NumberFieldState<I : Number> internal constructor(
        val text: String,
        isDirty: Boolean,
        private val validators: NumberValidatorGroup<I>,
) {
    constructor(text: String = "", validators: NumberValidatorGroup<I>) : this(text, false, validators)

    val error = validators.getFirstError(text, isDirty)
    val parsed = validators.parse(text)

    fun onValueChanged(value: String?) = NumberFieldState(text = value ?: "", isDirty = true, validators = validators)
}


class PartialNumberFieldState private constructor(
        private val text: String,
        private val isDirty: Boolean,
) {
    constructor(text: String = "") : this(text, false)

    fun <I : Number> asNumberFieldState(validators: NumberValidatorGroup<I>) =
            NumberFieldState(text = text, isDirty = isDirty, validators = validators)

    fun onValueChanged(value: String?) = PartialNumberFieldState(text = value ?: "", isDirty = true)
}
