package eywa.projectcodex.common.sharedUi

import androidx.compose.ui.Modifier

object ComposeUtils {
    fun Modifier.modifierIf(predicate: Boolean, modifier: Modifier) = if (predicate) this.then(modifier) else Modifier
}
