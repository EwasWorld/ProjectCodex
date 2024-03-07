package eywa.projectcodex.common.helpShowcase

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
                boundaries: Map<Int, Pair<Offset, Size>>,
                item: HelpShowcaseItem,
                hasNextItem: Boolean,
                goToNextItemListener: () -> Unit,
                endShowcaseListener: () -> Unit,
                screenSize: Size,
        ): HelpShowcaseState? {
            val coordinates = item.firstVisible(
                    currentScreenSize = screenSize,
                    boundaries = boundaries,
            ) ?: return null

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
                boundaries: Map<Int, Pair<Offset, Size>>,
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
            boundaries: Map<Int, Pair<Offset, Size>>,
            item: HelpShowcaseItem,
            hasNextItem: Boolean,
            goToNextItemListener: () -> Unit,
            endShowcaseListener: () -> Unit,
            screenSize: Size,
    ): HelpShowcaseState?
}
