package eywa.projectcodex.common.sharedUi.codexTheme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import javax.annotation.concurrent.Immutable

val LocalCodexThemeDimens = staticCompositionLocalOf { CodexDimens() }

@Immutable
data class CodexDimens(
        val screenPadding: Dp = 20.dp,
        val smallCornerRounding: Dp = 15.dp,
        val cornerRounding: Dp = 30.dp,
)
