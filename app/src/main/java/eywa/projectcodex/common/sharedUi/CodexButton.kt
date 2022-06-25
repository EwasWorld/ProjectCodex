package eywa.projectcodex.common.sharedUi

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import java.util.*

@Composable
fun CodexButton(
        text: String,
        modifier: Modifier = Modifier,
        onClick: () -> Unit,
) {
    Button(
            modifier = modifier,
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.LightGray,
                    contentColor = Color.Black
            )
    ) {
        Text(text = text.uppercase(Locale.getDefault()))
    }
}