package eywa.projectcodex.common.sharedUi

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.CodexTestTag
import kotlin.reflect.KClass

/**
 * Groups validators together
 *
 * @throws IllegalArgumentException if there is not exactly one [NumberValidator] with a [NumberValidator.parse] fn
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

interface DisplayableError {
    fun toErrorString(resources: Resources): String
}

data class StringResError(@StringRes val id: Int) : DisplayableError {
    override fun toErrorString(resources: Resources): String = resources.getString(id)
}

sealed class NumberValidator<T : Number> : DisplayableError {
    object IsPositive : NumberValidator<Number>() {
        override fun isValid(value: String, parsed: Number): Boolean =
                Regex("-.*").matchEntire(value) == null

        override fun toErrorString(resources: Resources): String =
                resources.getString(R.string.err__negative_is_invalid)
    }

    data class InRange<T>(val range: ClosedRange<T>) : NumberValidator<T>() where T : Number, T : Comparable<T> {
        override fun isValid(value: String, parsed: T): Boolean = parsed in range

        override fun toErrorString(resources: Resources): String =
                resources.getString(R.string.err__out_of_range, range.start, range.endInclusive)
    }

    abstract fun isValid(value: String, parsed: T): Boolean
}

// TODO_CURRENT Do not delete whole thing if not matching
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

@Composable
fun LabelledNumberSetting(
        title: String,
        currentValue: String?,
        testTag: String,
        placeholder: String,
        modifier: Modifier = Modifier,
        errorMessage: DisplayableError? = null,
        helpState: HelpState? = null,
        onValueChanged: (String?) -> Unit,
) = LabelledNumberSetting(
        clazz = String::class,
        title = title,
        currentValue = currentValue,
        testTag = testTag,
        placeholder = placeholder,
        modifier = modifier,
        errorMessage = errorMessage?.toErrorString(LocalContext.current.resources),
        helpState = helpState,
        onValueChanged = onValueChanged,
)

@Composable
fun <I : Any> LabelledNumberSetting(
        clazz: KClass<I>,
        title: String,
        currentValue: I?,
        testTag: String,
        placeholder: I,
        modifier: Modifier = Modifier,
        errorMessage: String? = null,
        helpState: HelpState? = null,
        onValueChanged: (I?) -> Unit,
) {
    DataRow(
            title = title,
            helpState = helpState,
            titleModifier = Modifier.clearAndSetSemantics { },
            modifier = modifier,
    ) {
        NumberSetting(
                clazz = clazz,
                contentDescription = title,
                currentValue = currentValue,
                errorMessage = errorMessage,
                testTag = testTag,
                placeholder = placeholder,
                onValueChanged = onValueChanged,
        )
    }
}

@Composable
fun NumberSetting(
        contentDescription: String,
        currentValue: String?,
        testTag: String,
        placeholder: String,
        modifier: Modifier = Modifier,
        errorMessage: DisplayableError? = null,
        onValueChanged: (String?) -> Unit,
) = NumberSetting(
        clazz = String::class,
        contentDescription = contentDescription,
        currentValue = currentValue,
        testTag = testTag,
        placeholder = placeholder,
        modifier = modifier,
        errorMessage = errorMessage?.toErrorString(LocalContext.current.resources),
        onValueChanged = onValueChanged,
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <I : Any> NumberSetting(
        clazz: KClass<I>,
        contentDescription: String,
        currentValue: I?,
        testTag: String,
        placeholder: I,
        modifier: Modifier = Modifier,
        errorMessage: String? = null,
        onValueChanged: (I?) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val helper = NumberSettingHelper.getHelper(clazz)!!
    val displayValue = currentValue?.toString() ?: ""

    val error = errorMessage?.let {
        it.takeIf { it.isNotBlank() } ?: stringResource(R.string.general_error)
    }

    Surface(
            color = CodexTheme.colors.surfaceOnBackground,
            shape = RoundedCornerShape(5.dp),
            modifier = modifier,
    ) {
        CodexTextField(
                state = CodexTextFieldState(
                        text = displayValue,
                        onValueChange = { onValueChanged(helper.fromString(it)) },
                        testTag = "",
                ),
                isError = error != null,
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
                        .semantics {
                            this.contentDescription = contentDescription
                            editableText = AnnotatedString(displayValue)
                            error?.let { error(it) }
                        }
        )
    }
}

@Composable
fun NumberSettingErrorText(
        errorText: DisplayableError?,
        testTag: CodexTestTag,
        modifier: Modifier = Modifier,
        textAlign: TextAlign? = null,
) {
    AnimatedVisibility(
            visible = errorText != null,
            enter = fadeIn() + expandIn(expandFrom = Alignment.TopCenter),
            exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.TopCenter),
    ) {
        Text(
                text = errorText?.toErrorString(LocalContext.current.resources) ?: "",
                style = CodexTypography.SMALL,
                color = CodexTheme.colors.errorOnAppBackground,
                textAlign = textAlign,
                modifier = modifier
                        .testTag(testTag.getTestTag())
                        .clearAndSetSemantics { }
        )
    }
}
