package eywa.projectcodex.common.sharedUi.numberField

data class NumberFieldState<I : Number> internal constructor(
        val text: String,
        val isDirty: Boolean,
        private val validators: NumberValidatorGroup<I>,
) {
    constructor(validators: NumberValidatorGroup<I>, text: String = "") : this(text, false, validators)

    val error = validators.getFirstError(text, isDirty)
    val parsed = validators.parse(text)

    fun onTextChanged(value: String?) = NumberFieldState(text = value ?: "", isDirty = true, validators = validators)
    fun clear() = NumberFieldState(validators = validators)
    fun markDirty() = onTextChanged(text)
}


data class PartialNumberFieldState private constructor(
        private val text: String,
        private val isDirty: Boolean,
) {
    constructor(text: String = "") : this(text, false)

    fun <I : Number> asNumberFieldState(validators: NumberValidatorGroup<I>) =
            NumberFieldState(text = text, isDirty = isDirty, validators = validators)

    fun onTextChanged(value: String?) = PartialNumberFieldState(text = value ?: "", isDirty = true)
    fun markDirty() = onTextChanged(text)
}
