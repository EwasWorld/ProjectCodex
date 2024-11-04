package eywa.projectcodex.common.sharedUi.numberField

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.clearAndSetSemantics
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
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual

@Composable
fun CodexLabelledNumberFieldWithErrorMessage(
        title: String,
        currentValue: String?,
        fieldTestTag: CodexTestTag,
        errorMessageTestTag: CodexTestTag,
        placeholder: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        errorMessage: ResOrActual<String>? = null,
        selectAllOnFocus: Boolean = true,
        colors: TextFieldColors = CodexTextField.transparentOutlinedTextFieldColors(),
        helpState: HelpState? = null,
        onValueChanged: (String?) -> Unit,
) {
    Column(
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
    ) {
        CodexLabelledNumberField(
                title = title,
                currentValue = currentValue,
                testTag = fieldTestTag,
                placeholder = placeholder,
                enabled = enabled,
                errorMessage = errorMessage,
                selectAllOnFocus = selectAllOnFocus,
                colors = colors,
                helpState = helpState,
                onValueChanged = onValueChanged,
        )
        CodexNumberFieldErrorText(
                errorText = errorMessage,
                testTag = errorMessageTestTag,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CodexLabelledNumberField(
        title: String,
        currentValue: String?,
        testTag: CodexTestTag,
        placeholder: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        errorMessage: ResOrActual<String>? = null,
        selectAllOnFocus: Boolean = true,
        colors: TextFieldColors = CodexTextField.transparentOutlinedTextFieldColors(),
        helpState: HelpState? = null,
        onValueChanged: (String?) -> Unit,
) {
    DataRow(
            title = title,
            helpState = helpState,
            titleModifier = Modifier.clearAndSetSemantics { },
            modifier = modifier
    ) {
        CodexNumberField(
                contentDescription = title,
                currentValue = currentValue,
                errorMessage = errorMessage,
                enabled = enabled,
                selectAllOnFocus = selectAllOnFocus,
                testTag = testTag,
                placeholder = placeholder,
                colors = colors,
                onValueChanged = onValueChanged,
                modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
fun CodexNumberField(
        contentDescription: String,
        currentValue: String?,
        testTag: CodexTestTag,
        placeholder: String,
        modifier: Modifier = Modifier,
        errorMessage: ResOrActual<String>?,
        enabled: Boolean = true,
        selectAllOnFocus: Boolean = true,
        trailingIcon: (@Composable () -> Unit)? = null,
        colors: TextFieldColors = CodexTextField.transparentOutlinedTextFieldColors(),
        onValueChanged: (String?) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val displayValue = currentValue ?: ""
    val error = errorMessage?.get()

    CodexTextField(
            state = CodexTextFieldState(
                    text = displayValue,
                    onValueChange = { onValueChanged(it) },
                    testTag = testTag,
            ),
            error = error,
            trailingIcon = trailingIcon,
            placeholderText = placeholder,
            enabled = enabled,
            textStyle = CodexTypography.NORMAL.copy(
                    color = CodexTheme.colors.onSurfaceOnBackground,
                    textAlign = TextAlign.Center,
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() },
            ),
            selectAllOnFocus = selectAllOnFocus,
            colors = colors,
            contentDescription = contentDescription,
            modifier = modifier
                    .widthIn(min = 40.dp)
                    .width(IntrinsicSize.Min)
    )
}

@Composable
fun CodexNumberFieldErrorText(
        errorText: ResOrActual<String>?,
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
                text = errorText?.get() ?: "",
                style = CodexTypography.SMALL,
                color = CodexTheme.colors.errorOnAppBackground,
                textAlign = textAlign,
                modifier = modifier
                        .testTag(testTag)
                        .clearAndSetSemantics { }
        )
    }
}
