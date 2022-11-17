package eywa.projectcodex.common.sharedUi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography


/**
 * Text should be no more than 20 characters
 */
@Composable
fun CodexChip(
        text: String,
        state: CodexChipState,
        modifier: Modifier = Modifier,
) = CodexChip(
        text = text,
        selected = state.selected,
        enabled = state.enabled,
        modifier = modifier,
        onToggle = state.onToggle,
        testTag = state.testTag,
)

/**
 * Text should be no more than 20 characters
 */
@Composable
fun CodexChip(
        text: String,
        selected: Boolean,
        testTag: String,
        enabled: Boolean = true,
        modifier: Modifier = Modifier,
        onToggle: () -> Unit
) {
    val surfaceColor = when {
        !selected -> Color.Transparent
        enabled -> CodexTheme.colors.chipOnPrimarySelected
        else -> CodexTheme.colors.disabledButton
    }
    val onColor = when {
        !enabled -> CodexTheme.colors.onDisabledButton
        selected -> CodexTheme.colors.chipOnPrimarySelectedText
        else -> CodexTheme.colors.chipOnPrimaryUnselected
    }
    val clickModifier = if (!enabled) Modifier else Modifier.selectable(selected = selected, onClick = onToggle)

    // TODO Animate
    Surface(
            border = if (selected) null else BorderStroke(Dp.Hairline, onColor),
            shape = RoundedCornerShape(8.dp),
            color = surfaceColor,
            modifier = modifier
    ) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = clickModifier
                        .height(32.dp)
                        .padding(start = 8.dp, end = 16.dp)
                        .testTag(testTag)
        ) {
            if (selected) {
                Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = onColor,
                        modifier = Modifier.size(18.dp)
                )
            }
            else {
                Spacer(modifier = Modifier.size(0.dp, 0.dp))
            }
            Text(
                    text = text,
                    style = CodexTypography.SMALL.copy(color = onColor),
            )
        }
    }
}

data class CodexChipState(
        val selected: Boolean,
        val enabled: Boolean = true,
        val onToggle: () -> Unit,
        val testTag: String,
)

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY
)
@Composable
fun CodexChip_Preview() {
    CodexTheme {
        Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(10.dp)
        ) {
            Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CodexChip(text = "First chip", selected = true, enabled = true, onToggle = {}, testTag = "")
                CodexChip(text = "Chip 2", selected = false, enabled = true, onToggle = {}, testTag = "")
            }
            Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CodexChip(text = "Chip 3", selected = true, enabled = false, onToggle = {}, testTag = "")
                CodexChip(text = "Another chip", selected = false, enabled = false, onToggle = {}, testTag = "")
            }
        }
    }
}