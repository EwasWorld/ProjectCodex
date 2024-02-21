package eywa.projectcodex.common.helpShowcase

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import eywa.projectcodex.common.helpShowcase.ui.HelpShowcaseNoShapeState
import eywa.projectcodex.common.helpShowcase.ui.HelpShowcaseOvalState
import eywa.projectcodex.common.helpShowcase.ui.HelpShowcaseState

operator fun Pair<Offset, Size>.contains(point: Offset): Boolean =
        ((point.x - first.x) in 0f..second.width) && ((point.y - first.y) in 0f..second.height)

/**
 * The shape the showcase will use to highlight the given view
 */
enum class HelpShowcaseShape {
    OVAL {
        @Composable
        override fun asState(
                visibleScreenSize: Pair<Offset, Size>?,
                item: HelpShowcaseItem,
                hasNextItem: Boolean,
                goToNextItemListener: () -> Unit,
                endShowcaseListener: () -> Unit,
                screenSize: Size,
        ): HelpShowcaseState? {
            val coordinates = item.layoutCoordinates.entries
                    .sortedBy { it.key }
                    .map { it.value }
                    .firstOrNull {
                        if (!it.isAttached) return@firstOrNull false
                        val screen = visibleScreenSize ?: (Offset.Zero to screenSize)

                        val topLeft = it.positionInRoot()
                        val (w, h) = it.size
                        val bottomRight = topLeft.plus(Offset(w.toFloat(), h.toFloat()))

                        topLeft in screen && bottomRight in screen
                    }
                    ?: return null

            return HelpShowcaseOvalState.from(
                    title = item.helpTitle,
                    message = item.helpBody,
                    hasNextItem = hasNextItem,
                    viewInfo = coordinates,
                    screenSize = screenSize,
                    padding = item.shapePadding ?: HelpShowcaseOvalState.DEFAULT_PADDING,
                    density = LocalDensity.current,
                    closeButtonListener = endShowcaseListener,
                    nextButtonListener = goToNextItemListener,
                    overlayClickedListener = goToNextItemListener,
            )
        }
    },
    NO_SHAPE {
        @Composable
        override fun asState(
                visibleScreenSize: Pair<Offset, Size>?,
                item: HelpShowcaseItem,
                hasNextItem: Boolean,
                goToNextItemListener: () -> Unit,
                endShowcaseListener: () -> Unit,
                screenSize: Size,
        ): HelpShowcaseState {
            return HelpShowcaseNoShapeState(
                    title = item.helpTitle,
                    message = item.helpBody,
                    hasNextItem = hasNextItem,
                    screenSize = screenSize,
                    closeListener = endShowcaseListener,
                    nextItemListener = goToNextItemListener,
                    overlayClickedListener = goToNextItemListener,
            )
        }
    },
    ;

    @Composable
    abstract fun asState(
            visibleScreenSize: Pair<Offset, Size>?,
            item: HelpShowcaseItem,
            hasNextItem: Boolean,
            goToNextItemListener: () -> Unit,
            endShowcaseListener: () -> Unit,
            screenSize: Size,
    ): HelpShowcaseState?
}
