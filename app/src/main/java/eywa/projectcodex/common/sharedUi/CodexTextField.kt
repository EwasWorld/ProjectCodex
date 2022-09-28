package eywa.projectcodex.common.sharedUi

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography

object CodexTextField {
    @Composable
    fun transparentOutlinedTextFieldColors(
            focussedColor: Color = CodexTheme.colors.textFieldFocussedOutline,
            unfocussedColor: Color = CodexTheme.colors.textFieldUnfocussedOutline
    ) = TextFieldDefaults.outlinedTextFieldColors(
            textColor = CodexTypography.NORMAL.color,
            backgroundColor = Color.Transparent,
            focusedBorderColor = focussedColor,
            unfocusedBorderColor = unfocussedColor,
            disabledBorderColor = CodexTheme.colors.disabledOnSurfaceOnBackground,
            focusedLabelColor = focussedColor,
            unfocusedLabelColor = unfocussedColor,
            disabledLabelColor = CodexTheme.colors.disabledOnSurfaceOnBackground,
    )
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CodexTextField(
        text: String,
        placeholderText: String?,
        labelText: String? = null,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier,
        singleLine: Boolean = false,
        enabled: Boolean = true,
        colors: TextFieldColors = CodexTextField.transparentOutlinedTextFieldColors()
) {
    val interactionSource = remember { MutableInteractionSource() }

    BasicTextField(
            value = text,
            onValueChange = onValueChange,
            modifier = modifier,
            interactionSource = interactionSource,
            enabled = enabled,
            singleLine = singleLine,
            visualTransformation = VisualTransformation.None,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                    onDone = {
//                                        keyboardController?.hide()
                        // do something here
                    }
            ),
    ) { innerTextField ->
        TextFieldDefaults.OutlinedTextFieldDecorationBox(
                value = text,
                innerTextField = innerTextField,
                contentPadding = PaddingValues(10.dp),
                enabled = enabled,
                interactionSource = interactionSource,
                singleLine = singleLine,
                visualTransformation = VisualTransformation.None,
                placeholder = placeholderText?.let {
                    {
                        Text(
                                text = placeholderText,
                                style = CodexTypography.SMALL,
                        )
                    }

                },
                label = labelText?.let {
                    {
                        Text(
                                text = labelText,
                                style = CodexTypography.SMALL,
                        )
                    }
                },
                colors = colors,
        )
    }
}
