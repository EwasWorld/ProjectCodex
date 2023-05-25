package eywa.projectcodex.common.helpShowcase

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import eywa.projectcodex.common.helpShowcase.ui.HelpShowcaseNoShapeState
import eywa.projectcodex.common.helpShowcase.ui.HelpShowcaseOvalState
import eywa.projectcodex.common.helpShowcase.ui.HelpShowcaseState
import eywa.projectcodex.common.utils.get

/**
 * The shape the showcase will use to highlight the given view
 */
enum class HelpShowcaseShape {
    OVAL {
        @Composable
        override fun asState(
                item: HelpShowcaseItem,
                hasNextItem: Boolean,
                goToNextItemListener: () -> Unit,
                endShowcaseListener: () -> Unit,
                screenHeight: Float,
                screenWidth: Float,
        ): HelpShowcaseState? {
            if (item.layoutCoordinates == null || !item.layoutCoordinates.isAttached) return null

            return HelpShowcaseOvalState.from(
                    title = item.helpTitle.get(),
                    message = item.helpBody.get(),
                    hasNextItem = hasNextItem,
                    viewInfo = item.layoutCoordinates,
                    screenHeight = screenHeight,
                    screenWidth = screenWidth,
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
                item: HelpShowcaseItem,
                hasNextItem: Boolean,
                goToNextItemListener: () -> Unit,
                endShowcaseListener: () -> Unit,
                screenHeight: Float,
                screenWidth: Float
        ): HelpShowcaseState {
            return HelpShowcaseNoShapeState(
                    title = item.helpTitle.get(),
                    message = item.helpBody.get(),
                    hasNextItem = hasNextItem,
                    screenHeight = screenHeight,
                    screenWidth = screenWidth,
                    closeListener = endShowcaseListener,
                    nextItemListener = goToNextItemListener,
                    overlayClickedListener = goToNextItemListener,
            )
        }
    },
    ;

    @Composable
    abstract fun asState(
            item: HelpShowcaseItem,
            hasNextItem: Boolean,
            goToNextItemListener: () -> Unit,
            endShowcaseListener: () -> Unit,
            screenHeight: Float,
            screenWidth: Float,
    ): HelpShowcaseState?
}
