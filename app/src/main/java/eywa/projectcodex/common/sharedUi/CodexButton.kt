package eywa.projectcodex.common.sharedUi

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import java.util.*

@Composable
fun CodexButton(
        text: String,
        buttonStyle: CodexButtonStyle,
        modifier: Modifier = Modifier,
        onClick: () -> Unit,
) {
    Button(
            modifier = modifier,
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                    backgroundColor = buttonStyle.getBackgroundColor(),
                    contentColor = buttonStyle.getTextColor()
            ),
            elevation = if (buttonStyle.hasElevation) ButtonDefaults.elevation() else null
    ) {
        Text(
                text = text.uppercase(Locale.getDefault()),
                style = buttonStyle.textStyle.copy(color = buttonStyle.getTextColor())
        )
    }
}

abstract class CodexButtonStyle {
    open val hasElevation: Boolean = true
    open val textStyle: TextStyle = TextStyle.Default

    @Composable
    abstract fun getBackgroundColor(): Color

    @Composable
    abstract fun getTextColor(): Color
}

abstract class ColouredButton : CodexButtonStyle()

abstract class TextButton : CodexButtonStyle() {
    final override val hasElevation = false

    @Composable
    override fun getBackgroundColor() = Color.Transparent
}

sealed class CodexButtonDefaults : CodexButtonStyle() {
    object ButtonOnAppBackground : ColouredButton() {
        @Composable
        override fun getBackgroundColor() = CodexTheme.colors.filledButton

        @Composable
        override fun getTextColor() = CodexTheme.colors.onFilledButton
    }

    object DialogPositiveButton : TextButton() {
        override val textStyle: TextStyle = CodexTypography.DIALOG_BUTTON

        @Composable
        override fun getTextColor() = CodexTheme.colors.dialogPositiveText
    }

    object DialogNegativeButton : TextButton() {
        override val textStyle: TextStyle = CodexTypography.DIALOG_BUTTON

        @Composable
        override fun getTextColor() = CodexTheme.colors.dialogNegativeText
    }
}
