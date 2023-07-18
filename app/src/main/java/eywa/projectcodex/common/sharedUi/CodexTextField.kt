package eywa.projectcodex.common.sharedUi

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asPlaceholderStyle

@Composable
fun CodexTextFieldRoundedSurface(
        modifier: Modifier = Modifier,
        color: Color = CodexTheme.colors.surfaceOnBackground,
        helpState: HelpState? = null,
        content: @Composable () -> Unit
) {
    helpState?.add()
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
        isError: Boolean = false,
        textStyle: TextStyle = TextStyle.Default,
        enabled: Boolean = true,
        colors: TextFieldColors = CodexTextField.transparentOutlinedTextFieldColors(),
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        keyboardActions: KeyboardActions = KeyboardActions.Default,
        helpState: HelpState? = null,
) = CodexTextField(
        text = state.text,
        onValueChange = state.onValueChange,
        placeholderText = placeholderText,
        labelText = labelText,
        modifier = modifier.testTag(state.testTag),
        singleLine = singleLine,
        isError = isError,
        textStyle = textStyle,
        enabled = enabled,
        colors = colors,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
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
        isError: Boolean = false,
        textStyle: TextStyle = TextStyle.Default,
        enabled: Boolean = true,
        colors: TextFieldColors = CodexTextField.transparentOutlinedTextFieldColors(),
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        keyboardActions: KeyboardActions = KeyboardActions.Default,
        helpState: HelpState? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    helpState?.add()

    BasicTextField(
            value = text,
            onValueChange = onValueChange,
            modifier = modifier.updateHelpDialogPosition(helpState),
            interactionSource = interactionSource,
            enabled = enabled,
            singleLine = singleLine,
            textStyle = textStyle,
            visualTransformation = VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
    ) { innerTextField ->
        TextFieldDefaults.OutlinedTextFieldDecorationBox(
                value = text,
                innerTextField = innerTextField,
                contentPadding = PaddingValues(10.dp),
                enabled = enabled,
                interactionSource = interactionSource,
                singleLine = singleLine,
                isError = isError,
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
        )
    }
}

data class CodexTextFieldState(
        val text: String,
        val onValueChange: (String) -> Unit,
        val testTag: String,
)

object CodexTextField {
    @Composable
    fun transparentOutlinedTextFieldColors(
            focussedColor: Color = CodexTheme.colors.textFieldFocussedOutline,
            unfocussedColor: Color = CodexTheme.colors.textFieldUnfocussedOutline
    ) = TextFieldDefaults.outlinedTextFieldColors(
            textColor = CodexTypography.NORMAL.color,
            backgroundColor = Color.Transparent,

            focusedBorderColor = focussedColor,
            focusedLabelColor = focussedColor,
            unfocusedBorderColor = unfocussedColor,
            unfocusedLabelColor = unfocussedColor,

            disabledBorderColor = CodexTheme.colors.disabledOnSurfaceOnBackground,
            disabledLabelColor = CodexTheme.colors.disabledOnSurfaceOnBackground,
            errorBorderColor = CodexTheme.colors.errorOnAppBackground,
            errorLabelColor = CodexTheme.colors.errorOnAppBackground,
    )
}
