package eywa.projectcodex.common.sharedUi

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode

object ComposeUtils {
    fun Modifier.modifierIf(predicate: Boolean, modifier: Modifier) = if (predicate) this.then(modifier) else this


    /**
     * true if in a compose preview
     */
    @Composable
    fun isInEditMode() = LocalInspectionMode.current
}
