package eywa.projectcodex.common.sharedUi

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import eywa.projectcodex.common.utils.CodexTestTag

fun Modifier.testTag(testTag: CodexTestTag) = testTag(testTag.getTestTag())

object ComposeUtils {
    fun <K : Any> Modifier.modifierIfNotNull(value: K?, modifier: (K) -> Modifier) =
            if (value != null) this.then(modifier(value)) else this

    fun Modifier.modifierIf(predicate: Boolean, modifier: Modifier) = if (predicate) this.then(modifier) else this

    fun Modifier.reduceParentPadding(
            padding: PaddingValues,
    ) = composed {
        val layoutDirection: LayoutDirection = LocalLayoutDirection.current
        layout { measurable, constraints ->
            val horizontal =
                    padding.calculateLeftPadding(layoutDirection) + padding.calculateRightPadding(layoutDirection)
            val vertical = padding.calculateTopPadding() + padding.calculateBottomPadding()
            val placeable = measurable.measure(
                    constraints.copy(
                            maxWidth = constraints.maxWidth + horizontal.roundToPx(),
                            maxHeight = constraints.maxHeight + vertical.roundToPx(),
                    )
            )
            layout(placeable.width, placeable.height) {
                placeable.place(0, 0)
            }
        }
    }

    /**
     * true if in a compose preview
     */
    @Composable
    fun isInEditMode() = LocalInspectionMode.current

    fun <T> List<T>.orderPreviews(): List<T> {
        val finalPos = indices.sortedBy { it.toString() }
        val final = MutableList(size) { first() }
        for ((i, pos) in finalPos.withIndex()) {
            final[pos] = get(i)
        }
        return final
    }
}
