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
        isChecked: Boolean,
        isDisabled: Boolean = false,
        onClick: () -> Unit
) {
    val surfaceColor = when {
        !isChecked -> Color.Transparent
        !isDisabled -> CodexTheme.colors.chipOnPrimarySelected
        else -> CodexTheme.colors.disabledButton
    }
    val onColor = when {
        isDisabled -> CodexTheme.colors.onDisabledButton
        isChecked -> CodexTheme.colors.chipOnPrimarySelectedText
        else -> CodexTheme.colors.chipOnPrimaryUnselected
    }

    Surface(
            border = if (isChecked) null else BorderStroke(Dp.Hairline, onColor),
            shape = RoundedCornerShape(8.dp),
            color = surfaceColor,
    ) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                        .selectable(selected = isChecked, onClick = onClick)
                        .height(32.dp)
                        .padding(start = 8.dp, end = 16.dp)
        ) {
            if (isChecked) {
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
                CodexChip(text = "First chip", isChecked = true, isDisabled = false, onClick = {})
                CodexChip(text = "Chip 2", isChecked = false, isDisabled = false, onClick = {})
            }
            Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CodexChip(text = "Chip 3", isChecked = true, isDisabled = true, onClick = {})
                CodexChip(text = "Another chip", isChecked = false, isDisabled = true, onClick = {})
            }
        }
    }
}