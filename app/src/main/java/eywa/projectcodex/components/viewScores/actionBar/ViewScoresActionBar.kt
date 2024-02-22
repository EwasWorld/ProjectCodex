package eywa.projectcodex.components.viewScores.actionBar

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme

@Composable
fun ViewScoresActionBar(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
) = Surface(
        shape = RoundedCornerShape(CornerSize(35.dp)),
        color = CodexTheme.colors.floatingActions,
        content = content,
        modifier = modifier
)
