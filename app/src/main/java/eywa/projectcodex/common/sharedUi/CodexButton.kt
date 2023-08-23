package eywa.projectcodex.common.sharedUi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexThemeColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography

@Composable
fun CodexButton(
        text: String,
        modifier: Modifier = Modifier,
        buttonStyle: CodexButtonStyle = CodexButtonDefaults.DefaultButton(),
        enabled: Boolean = true,
        helpState: HelpState? = null,
        trailingIconInfo: CodexIconInfo? = null,
        onClick: () -> Unit,
) {
    val textColor = buttonStyle.getTextColor(CodexTheme.colors)

    helpState?.add()

    Button(
            modifier = modifier.updateHelpDialogPosition(helpState),
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                    backgroundColor = buttonStyle.getBackgroundColor(CodexTheme.colors),
                    contentColor = textColor,
            ),
            elevation = if (buttonStyle.hasElevation) ButtonDefaults.elevation() else null,
            shape = RoundedCornerShape(100),
            border = buttonStyle.getBorder(CodexTheme.colors),
            enabled = enabled,
    ) {
        Text(
                text = text,
                style = buttonStyle.textStyle.copy(color = buttonStyle.getTextColor(CodexTheme.colors)),
                textAlign = TextAlign.Center,
                modifier = with(buttonStyle) { Modifier.textModifier() }
        )
        trailingIconInfo
                ?.copyIcon(tint = trailingIconInfo.tint ?: textColor)
                ?.ClavaIcon(
                        modifier = Modifier.padding(start = buttonStyle.trailingIconStartPadding)
                )
    }
}

abstract class CodexButtonStyle {
    open val hasElevation: Boolean = true
    open val textStyle: TextStyle = CodexTypography.SMALL
    open val trailingIconStartPadding: Dp = 8.dp

    abstract fun getBackgroundColor(themeColors: CodexThemeColors): Color
    abstract fun getTextColor(themeColors: CodexThemeColors): Color
    abstract fun getBorderColor(themeColors: CodexThemeColors): Color?

    fun getBorder(themeColors: CodexThemeColors): BorderStroke? {
        val borderColor = getBorderColor(themeColors) ?: return null
        return BorderStroke(1.dp, borderColor)
    }

    open fun Modifier.textModifier(): Modifier = this
}

abstract class ColouredButton : CodexButtonStyle() {
    override fun getBorderColor(themeColors: CodexThemeColors): Color? = null
}

abstract class TextButton : CodexButtonStyle() {
    final override val hasElevation = false
    override val textStyle: TextStyle = CodexTypography.TEXT_BUTTON

    override fun getBackgroundColor(themeColors: CodexThemeColors): Color = Color.Transparent

    override fun getBorderColor(themeColors: CodexThemeColors): Color? = null
}

abstract class OutlinedButton : CodexButtonStyle() {
    final override val hasElevation = false
    override val textStyle: TextStyle = CodexTypography.TEXT_BUTTON

    override fun getBackgroundColor(themeColors: CodexThemeColors): Color = Color.Transparent

    override fun getBorderColor(themeColors: CodexThemeColors): Color? = getTextColor(themeColors)
}

sealed class CodexButtonDefaults : CodexButtonStyle() {
    open class DefaultButton : ColouredButton() {
        override val textStyle: TextStyle = CodexTypography.NORMAL
        override val trailingIconStartPadding: Dp = 0.dp

        override fun getBackgroundColor(themeColors: CodexThemeColors): Color = themeColors.filledButton
        override fun getTextColor(themeColors: CodexThemeColors): Color = themeColors.onFilledButton

        override fun Modifier.textModifier() = this.padding(horizontal = 10.dp)

        override val hasElevation: Boolean = false
    }

    object DialogPositiveButton : TextButton() {
        override fun getTextColor(themeColors: CodexThemeColors) = themeColors.dialogPositiveText
    }

    object DialogNegativeButton : TextButton() {
        override fun getTextColor(themeColors: CodexThemeColors) = themeColors.dialogNegativeText
    }

    object DefaultTextButton : TextButton() {
        override fun getTextColor(themeColors: CodexThemeColors) = themeColors.textButtonOnPrimary
    }

    object DefaultOutlinedButton : OutlinedButton() {
        override fun getTextColor(themeColors: CodexThemeColors) = themeColors.filledButton
    }

    object OutlinedButtonOnAppBackground : OutlinedButton() {
        override fun getTextColor(themeColors: CodexThemeColors) = themeColors.onAppBackground
    }
}
