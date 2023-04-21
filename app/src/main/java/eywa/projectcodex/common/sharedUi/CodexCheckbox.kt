package eywa.projectcodex.common.sharedUi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CodexCheckbox(
        text: String,
        checked: Boolean,
        modifier: Modifier = Modifier,
        displayAsSwitch: Boolean = false,
        onToggle: () -> Unit,
) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = modifier.clickable(onClick = onToggle)
    ) {
        Text(text = text)

        if (displayAsSwitch) {
            Switch(checked = checked, onCheckedChange = { onToggle() })
        }
        else {
            Checkbox(checked = checked, onCheckedChange = { onToggle() })
        }
    }
}
