package eywa.projectcodex.common.sharedUi

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import kotlin.reflect.KClass

/**
 * Groups validators together
 *
 * @throws IllegalArgumentException if there is not exactly one [NumberValidator] with a [NumberValidator.parse] fn
 */
class NumberValidatorGroup<I : Number>(
        private val typeValidator: TypeValidator<I>,
        private vararg val validators: NumberValidator,
) {
    /**
     * @return null if there are no validation errors.
     * Otherwise returns the [NumberValidator.errorMessageId] of the first violation.
     */
    fun getFirstError(value: String?, isDirty: Boolean = true, isRequired: Boolean = true) =
            when {
                isRequired && isDirty && value.isNullOrBlank() -> R.string.err__required_field
                value.isNullOrBlank() -> null
                typeValidator.regex.matchEntire(value) == null -> typeValidator.regexFailedErrorMessageId
                else -> validators.firstOrNull { !it.isValid(value) }?.errorMessageId
            }

    fun parse(value: String) = typeValidator.transform(value)
}

/**
 * Check that the format of a [String] conforms to a particular number type e.g. [Float].
 * Provide a transformation function to convert from [String] to the given type.
 */
sealed class TypeValidator<I : Number>(
        val regex: Regex,
        val regexFailedErrorMessageId: Int,
) {
    object FloatValidator : TypeValidator<Float>(
            Regex("-?[0-9]*\\.?[0-9]*"),
            R.string.err__invalid_float_digit,
    ) {
        override fun transform(value: String) = value.toFloatOrNull()
    }

    object IntValidator : TypeValidator<Int>(
            Regex("-?[0-9]*"),
            R.string.err__invalid_int_digit,
    ) {
        override fun transform(value: String) = value.toIntOrNull()
    }

    abstract fun transform(value: String): I?
}

enum class NumberValidator(@StringRes val errorMessageId: Int) {
    POSITIVE(R.string.err__negative_is_invalid) {
        override fun isValid(value: String): Boolean = Regex("-.*").matchEntire(value) == null
    },
    ;

    abstract fun isValid(value: String): Boolean
}

// TODO_CURRENT Fix deleting whole thing if not matching
private sealed class NumberSettingHelper<I> {
    abstract val allowedDigits: Regex
    abstract fun fromString(value: String): I?

    @Deprecated("Don't use")
    object IntHelper : NumberSettingHelper<Int>() {
        override val allowedDigits: Regex
            get() = Regex("-?[0-9]*")

        override fun fromString(value: String) = value
                .takeIf { value.isNotBlank() }
                ?.toIntOrNull()
    }

    object StringHelper : NumberSettingHelper<String>() {
        override val allowedDigits: Regex
            get() = Regex(".*")

        override fun fromString(value: String) = value
    }

    @Deprecated("Don't use")
    object FloatHelper : NumberSettingHelper<Float>() {
        override val allowedDigits: Regex
            get() = Regex("-?[0-9]*\\.?[0-9]*")

        override fun fromString(value: String) = value
                .takeIf { value.isNotBlank() }
                ?.toFloatOrNull()
    }

    companion object {
        // Getting AbstractMethodError when using sealedSubclasses, possibly to do with having a generic?
        private val classMap = mapOf(
                Int::class to IntHelper,
                String::class to StringHelper,
        )

        fun <I : Any> getHelper(clazz: KClass<I>): NumberSettingHelper<I>? =
                classMap[clazz] as? NumberSettingHelper<I>
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <I : Any> NumberSetting(
        clazz: KClass<I>,
        @StringRes title: Int,
        currentValue: I?,
        isError: Boolean = false,
        testTag: String,
        helpListener: ((HelpShowcaseIntent) -> Unit)? = null,
        @StringRes helpTitle: Int? = null,
        @StringRes helpBody: Int? = null,
        placeholder: I,
        onValueChanged: (I?) -> Unit,
        modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val helper = NumberSettingHelper.getHelper(clazz)!!

    DataRow(
            title = title,
            helpListener = helpListener,
            helpTitle = helpTitle,
            helpBody = helpBody,
            modifier = modifier,
    ) {
        Surface(
                color = CodexTheme.colors.surfaceOnBackground,
                shape = RoundedCornerShape(5.dp),
        ) {
            CodexTextField(
                    state = CodexTextFieldState(
                            text = currentValue?.toString() ?: "",
                            onValueChange = { onValueChanged(helper.fromString(it)) },
                            testTag = "",
                    ),
                    isError = isError,
                    placeholderText = placeholder.toString(),
                    textStyle = CodexTypography.NORMAL.copy(
                            color = CodexTheme.colors.onSurfaceOnBackground,
                            textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() },
                    ),
                    modifier = Modifier
                            .testTag(testTag)
                            .widthIn(min = 40.dp)
                            .width(IntrinsicSize.Min)
            )
        }
    }
}
