package eywa.projectcodex.common.sharedUi.codexTheme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object CodexTypography {
    val NORMAL = TextStyle.Default.copy(
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight(400),
    )

    val DIALOG_TITLE = NORMAL.copy(
            fontSize = 24.sp,
    )

    val DIALOG_TEXT = NORMAL.copy(
            fontSize = 14.sp,
    )

    val DIALOG_BUTTON = NORMAL.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight(500),
    )

    val SMALL = NORMAL.copy(
            fontSize = 14.sp,
    )

    val SMALL_DIMMED = SMALL.copy(
            color = Color.Black.copy(alpha = 0.55f),
    )
}