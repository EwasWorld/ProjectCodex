package eywa.projectcodex.common.sharedUi

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import eywa.projectcodex.common.utils.CodexTestTag

fun Modifier.testTag(testTag: CodexTestTag) = testTag("${testTag.screenName}_${testTag.getElement()}")

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

    fun Modifier.semanticsWithContext(
            mergeDescendants: Boolean = false,
            properties: (SemanticsPropertyReceiver.(Context) -> Unit)
    ): Modifier = composed {
        val context = LocalContext.current
        this.then(Modifier.semantics(mergeDescendants) {
            properties(context)
        })
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
