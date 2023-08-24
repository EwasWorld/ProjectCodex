package eywa.projectcodex.common.sharedUi.numberField

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
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.sharedUi.CodexTextField
import eywa.projectcodex.common.sharedUi.CodexTextFieldState
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.CodexTestTag

@Composable
fun CodexLabelledNumberField(
        title: String,
        currentValue: String?,
        testTag: String,
        placeholder: String,
        modifier: Modifier = Modifier,
        errorMessage: DisplayableError? = null,
        helpState: HelpState? = null,
        onValueChanged: (String?) -> Unit,
) {
    DataRow(
            title = title,
            helpState = helpState,
            titleModifier = Modifier.clearAndSetSemantics { },
            modifier = modifier,
    ) {
        CodexNumberField(
                contentDescription = title,
                currentValue = currentValue,
                errorMessage = errorMessage,
                testTag = testTag,
                placeholder = placeholder,
                onValueChanged = onValueChanged,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CodexNumberField(
        contentDescription: String,
        currentValue: String?,
        testTag: String,
        placeholder: String,
        modifier: Modifier = Modifier,
        errorMessage: DisplayableError? = null,
        onValueChanged: (String?) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val displayValue = currentValue ?: ""
    val error = errorMessage?.toErrorString(LocalContext.current.resources)

    Surface(
            color = CodexTheme.colors.surfaceOnBackground,
            shape = RoundedCornerShape(5.dp),
            modifier = modifier,
    ) {
        CodexTextField(
                state = CodexTextFieldState(
                        text = displayValue,
                        onValueChange = { onValueChanged(it) },
                        testTag = "",
                ),
                isError = error != null,
                placeholderText = placeholder,
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
fun CodexNumberFieldErrorText(
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
