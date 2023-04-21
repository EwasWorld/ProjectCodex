package eywa.projectcodex.common.sharedUi

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme

@Composable
fun CodexIconButton(
        contentDescription: String,
        icon: ImageVector,
        modifier: Modifier = Modifier,
        onClick: () -> Unit,
) {
    IconButton(
            onClick = onClick,
            modifier = modifier.scale(1.2f),
    ) {
        Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = CodexTheme.colors.iconButtonOnPrimary,
        )
    }
}
