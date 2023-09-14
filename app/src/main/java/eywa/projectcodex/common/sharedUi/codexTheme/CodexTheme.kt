package eywa.projectcodex.common.sharedUi.codexTheme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

var currentAppTheme by mutableStateOf(AppTheme.LIGHT)

@Composable
fun CodexTheme(
        theme: AppTheme = currentAppTheme,
        content: @Composable () -> Unit
) {
    CompositionLocalProvider(
            LocalCodexThemeColors provides theme.colors,
            LocalCodexThemeDimens provides theme.dimens,
    ) {
        MaterialTheme(
                content = content
        )
    }
}

object CodexTheme {
    val colors: CodexThemeColors
        @Composable
        get() = LocalCodexThemeColors.current

    val dimens: CodexDimens
        @Composable
        get() = LocalCodexThemeDimens.current
}
