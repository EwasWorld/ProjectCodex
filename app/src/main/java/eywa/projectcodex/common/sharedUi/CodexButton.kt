package eywa.projectcodex.common.sharedUi

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
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
                    backgroundColor = buttonStyle.backgroundColor,
                    contentColor = buttonStyle.textColor
            ),
            elevation = if (buttonStyle.hasElevation) ButtonDefaults.elevation() else null
    ) {
        Text(text = text.uppercase(Locale.getDefault()))
    }
}

abstract class CodexButtonStyle {
    abstract val backgroundColor: Color
    abstract val textColor: Color
    open val hasElevation: Boolean = true
}

abstract class ColouredButton : CodexButtonStyle()

abstract class TextButton : CodexButtonStyle() {
    final override val backgroundColor: Color = Color.Transparent
    final override val hasElevation = false
}

sealed class CodexButtonDefaults : CodexButtonStyle() {
    object ButtonOnPrimary : ColouredButton() {
        override val backgroundColor: Color = Color.LightGray
        override val textColor: Color = Color.Black
    }

    object AlertDialogPositiveButton : TextButton() {
        override val textColor: Color = CodexColors.COLOR_ACCENT_DARK
    }

    object AlertDialogNegativeButton : TextButton() {
        override val textColor: Color = Color.DarkGray
    }
}
