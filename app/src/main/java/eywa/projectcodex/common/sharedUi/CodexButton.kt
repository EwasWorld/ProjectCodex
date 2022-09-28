package eywa.projectcodex.common.sharedUi

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography

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
            elevation = if (buttonStyle.hasElevation) ButtonDefaults.elevation() else null,
            shape = RoundedCornerShape(100)
    ) {
        Text(
                text = text,
                style = buttonStyle.textStyle.copy(color = buttonStyle.getTextColor()),
                modifier = with(buttonStyle) { Modifier.textModifier() }
        )
    }
}

abstract class CodexButtonStyle {
    open val hasElevation: Boolean = true
    open val textStyle: TextStyle = CodexTypography.SMALL

    @Composable
    abstract fun getBackgroundColor(): Color

    @Composable
    abstract fun getTextColor(): Color

    open fun Modifier.textModifier(): Modifier = this
}

abstract class ColouredButton : CodexButtonStyle()

abstract class TextButton : CodexButtonStyle() {
    final override val hasElevation = false

    @Composable
    override fun getBackgroundColor() = Color.Transparent
}

sealed class CodexButtonDefaults : CodexButtonStyle() {
    open class DefaultButton : ColouredButton() {
        @Composable
        override fun getBackgroundColor() = CodexTheme.colors.filledButton

        @Composable
        override fun getTextColor() = CodexTheme.colors.onFilledButton

        override fun Modifier.textModifier() = this.padding(horizontal = 10.dp)

        override val hasElevation: Boolean = false
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
