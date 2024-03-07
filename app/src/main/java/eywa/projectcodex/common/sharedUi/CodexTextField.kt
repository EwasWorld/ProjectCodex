package eywa.projectcodex.common.sharedUi

import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIfNotNull
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asPlaceholderStyle
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.GlobalTouchDetector
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun CodexTextFieldRoundedSurface(
        modifier: Modifier = Modifier,
        color: Color = CodexTheme.colors.surfaceOnBackground,
        helpState: HelpState? = null,
        content: @Composable () -> Unit
) {
    Surface(
            color = color,
            shape = RoundedCornerShape(5.dp),
            content = content,
            modifier = modifier.updateHelpDialogPosition(helpState),
    )
}

@Composable
fun CodexTextField(
        state: CodexTextFieldState,
        placeholderText: String?,
        modifier: Modifier = Modifier,
        labelText: String? = null,
        singleLine: Boolean = false,
        error: String? = null,
        trailingIcon: (@Composable () -> Unit)? = null,
        textStyle: TextStyle = TextStyle.Default,
        enabled: Boolean = true,
        colors: TextFieldColors = CodexTextField.transparentOutlinedTextFieldColors(),
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        keyboardActions: KeyboardActions = KeyboardActions.Default,
        selectAllOnFocus: Boolean = false,
        contentDescription: String? = null,
        helpState: HelpState? = null,
) = CodexTextField(
        text = state.text,
        onValueChange = state.onValueChange,
        placeholderText = placeholderText,
        labelText = labelText,
        modifier = modifier.modifierIfNotNull(state.testTag) { Modifier.testTag(it.getTestTag()) },
        singleLine = singleLine,
        error = error,
        trailingIcon = trailingIcon,
        textStyle = textStyle,
        enabled = enabled,
        colors = colors,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        selectAllTextOnFocus = selectAllOnFocus,
        contentDescription = contentDescription,
        helpState = helpState,
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CodexTextField(
        text: String,
        onValueChange: (String) -> Unit,
        placeholderText: String?,
        modifier: Modifier = Modifier,
        labelText: String? = null,
        singleLine: Boolean = false,
        error: String? = null,
        trailingIcon: (@Composable () -> Unit)? = null,
        textStyle: TextStyle = TextStyle.Default,
        enabled: Boolean = true,
        colors: TextFieldColors = CodexTextField.transparentOutlinedTextFieldColors(),
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        keyboardActions: KeyboardActions = KeyboardActions.Default,
        selectAllTextOnFocus: Boolean = false,
        contentDescription: String? = null,
        helpState: HelpState? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    var selection by remember { mutableStateOf(TextRange.Zero) }

    FocusClearer(interactionSource)

    if (selectAllTextOnFocus) {
        val isFocussed by interactionSource.collectIsFocusedAsState()
        LaunchedEffect(isFocussed) {
            val endRange = if (isFocussed) text.length else 0
            selection = TextRange(start = 0, end = endRange)
        }
    }

    Surface(
            color = colors.backgroundColor(enabled).value,
            shape = RoundedCornerShape(5.dp),
            modifier = modifier,
    ) {
        BasicTextField(
                value = TextFieldValue(text, selection),
                onValueChange = {
                    onValueChange(it.text)
                    selection = it.selection
                },
                interactionSource = interactionSource,
                enabled = enabled,
                singleLine = singleLine,
                textStyle = textStyle,
                visualTransformation = VisualTransformation.None,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                modifier = Modifier
                        .updateHelpDialogPosition(helpState)
                        .semantics {
                            contentDescription?.let { this.contentDescription = it }
                            error?.let { this.error(it) }
                        }
        ) { innerTextField ->
            TextFieldDefaults.OutlinedTextFieldDecorationBox(
                    value = text,
                    innerTextField = innerTextField,
                    contentPadding = PaddingValues(10.dp),
                    enabled = enabled,
                    interactionSource = interactionSource,
                    singleLine = singleLine,
                    isError = error != null,
                    visualTransformation = VisualTransformation.None,
                    placeholder = placeholderText?.let {
                        {
                            Text(
                                    text = placeholderText,
                                    style = textStyle.asPlaceholderStyle().copy(textAlign = textStyle.textAlign),
                            )
                        }

                    },
                    label = labelText?.let {
                        {
                            Text(
                                    text = labelText,
                                    style = textStyle,
                            )
                        }
                    },
                    colors = colors,
                    trailingIcon = trailingIcon,
            )
        }
    }
}

/**
 * Listen to [GlobalTouchDetector] emissions.
 * Clears focus after a short delay if a click that is not on the text field is detected.
 */
@Composable
private fun FocusClearer(interactionSource: InteractionSource) {
    val focusManager: FocusManager = LocalFocusManager.current
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(Unit) {
        launch {
            GlobalTouchDetector.effects.distinctUntilChanged().collectLatest { isPressed ->
                if (isPressed) {
                    delay(100)
                    focusManager.clearFocus()
                }
            }
        }
    }
    LaunchedEffect(isPressed) {
        if (isPressed) {
            launch {
                GlobalTouchDetector.antiPressDetected()
            }
        }
    }
}

data class CodexTextFieldState(
        val text: String,
        val onValueChange: (String) -> Unit,
        val testTag: CodexTestTag?,
)

object CodexTextField {
    @Composable
    fun transparentOutlinedTextFieldColors(
            focussedColor: Color = CodexTheme.colors.textFieldFocussedOutline,
            unfocussedColor: Color = CodexTheme.colors.textFieldUnfocussedOutline,
            backgroundColor: Color = CodexTheme.colors.surfaceOnBackground,
    ) = TextFieldDefaults.outlinedTextFieldColors(
            textColor = CodexTypography.NORMAL.color,
            backgroundColor = backgroundColor,

            focusedBorderColor = focussedColor,
            focusedLabelColor = focussedColor,
            unfocusedBorderColor = unfocussedColor,
            unfocusedLabelColor = unfocussedColor,

            disabledBorderColor = CodexTheme.colors.disabledOnSurfaceOnBackground,
            disabledLabelColor = CodexTheme.colors.disabledOnSurfaceOnBackground,
            errorBorderColor = CodexTheme.colors.errorOnAppBackground,
            errorLabelColor = CodexTheme.colors.errorOnAppBackground,
    )

    @Composable
    fun transparentOutlinedTextFieldColorsOnDialog() = transparentOutlinedTextFieldColors(
            backgroundColor = Color.Transparent,
            unfocussedColor = CodexTheme.colors.appBackground,
    )
}
