package eywa.projectcodex.common.sharedUi

import androidx.compose.material.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme

@Composable
fun CodexFloatingActionButton(
        icon: CodexIconInfo,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
) = FloatingActionButton(
        backgroundColor = CodexTheme.colors.floatingActions,
        contentColor = CodexTheme.colors.onFloatingActions,
        onClick = onClick,
        content = { icon.CodexIcon() },
        modifier = modifier
)
