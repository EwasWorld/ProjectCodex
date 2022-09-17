package eywa.projectcodex.common.sharedUi.codexTheme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun CodexTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        content()
    }
}