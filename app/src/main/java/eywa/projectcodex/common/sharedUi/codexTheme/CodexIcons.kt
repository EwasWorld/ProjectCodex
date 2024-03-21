package eywa.projectcodex.common.sharedUi.codexTheme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector

val LocalCodexThemeIcons = staticCompositionLocalOf { CodexIcons() }

@Immutable
data class CodexIcons(
        val helpInfo: ImageVector = Icons.Default.HelpOutline,
)
