package eywa.projectcodex.common.sharedUi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography

@Composable
fun CodexIconButton(
        icon: CodexIconInfo,
        modifier: Modifier = Modifier,
        captionBelow: String? = null,
        captionStyle: TextStyle = CodexTypography.X_SMALL,
        enabled: Boolean = true,
        helpState: HelpState? = null,
        onClick: () -> Unit,
) {
    val color = when {
        icon.tint != null -> icon.tint!!
        enabled -> CodexTheme.colors.iconButtonOnPrimary
        else -> CodexTheme.colors.disabledButton
    }
    val actualIcon = icon.copyIcon(
            contentDescription = icon.contentDescription.takeIf { captionBelow.isNullOrBlank() },
            tint = color,
    )

    IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                    .padding(3.dp)
                    .updateHelpDialogPosition(helpState)
    ) {
        Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            actualIcon.CodexIcon(
                    modifier = Modifier.scale(1.2f),
            )
            if (!captionBelow.isNullOrBlank()) {
                Text(
                        text = captionBelow,
                        style = captionStyle,
                        color = color,
                )
            }
        }
    }
}

@Composable
fun CodexIconButton(
        icon: CodexIconInfo,
        modifier: Modifier = Modifier,
        caption: String? = null,
        captionStyle: TextStyle = CodexTypography.X_SMALL,
        captionAlignment: Alignment = Alignment.BottomEnd,
        captionModifier: Modifier = Modifier,
        enabled: Boolean = true,
        helpState: HelpState? = null,
        onClick: () -> Unit,
) {
    val color = when {
        icon.tint != null -> icon.tint!!
        enabled -> CodexTheme.colors.iconButtonOnPrimary
        else -> CodexTheme.colors.disabledButton
    }
    val actualIcon = icon.copyIcon(
            contentDescription = icon.contentDescription.takeIf { caption.isNullOrBlank() },
            tint = color,
    )

    IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                    .padding(3.dp)
                    .updateHelpDialogPosition(helpState)
    ) {
        Box(
                contentAlignment = captionAlignment
        ) {
            actualIcon.CodexIcon(
                    modifier = Modifier
                            .scale(1.2f)
                            .padding(3.dp),
            )
            if (!caption.isNullOrBlank()) {
                Text(
                        text = caption,
                        style = captionStyle,
                        color = color,
                        modifier = captionModifier
                )
            }
        }
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun CodexIconButton_Preview() {
    CodexIconButton(
            icon = CodexIconInfo.VectorIcon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "",
            ),
            captionBelow = "Delete",
            onClick = {},
    )
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Overlap_CodexIconButton_Preview() {
    CodexIconButton(
            icon = CodexIconInfo.VectorIcon(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = "",
                    modifier = Modifier.padding(bottom = 5.dp)
            ),
            caption = "0",
            onClick = {},
    )
}
