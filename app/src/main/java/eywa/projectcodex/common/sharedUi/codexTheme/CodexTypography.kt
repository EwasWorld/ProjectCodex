package eywa.projectcodex.common.sharedUi.codexTheme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

@Composable
fun TextStyle.asClickableStyle() = copy(
        color = CodexTheme.colors.linkText,
        textDecoration = TextDecoration.Underline,
)

@Composable
fun TextStyle.asPlaceholderStyle() = copy(
        color = color.copy(color.alpha * 0.3f),
)

object CodexTypography {
    val X_LARGE = TextStyle.Default.copy(
            fontSize = 40.sp,
            fontWeight = FontWeight(400),
    )

    val LARGE = TextStyle.Default.copy(
            fontSize = 30.sp,
            fontWeight = FontWeight(400),
    )

    val NORMAL_PLUS = TextStyle.Default.copy(
            fontSize = 25.sp,
            fontWeight = FontWeight(400),
    )

    val NORMAL = TextStyle.Default.copy(
            fontSize = 20.sp,
            fontWeight = FontWeight(400),
    )

    val SMALL_PLUS = NORMAL.copy(
            fontSize = 17.sp,
    )

    val SMALL = NORMAL.copy(
            fontSize = 14.sp,
    )

    val X_SMALL = NORMAL.copy(
            fontSize = 10.sp,
    )

    val DIALOG_TITLE = NORMAL.copy(
            fontSize = 24.sp,
    )

    val DIALOG_TEXT = NORMAL.copy(
            fontSize = 16.sp,
            fontWeight = FontWeight(500),
    )

    val TEXT_BUTTON = NORMAL
}
