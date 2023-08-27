package eywa.projectcodex.common.helpShowcase

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Dp

/**
 * @param priority Lower number = higher priority
 */
data class HelpShowcaseItem(
        val helpTitle: String,
        internal val helpBody: String,
        internal val shapePadding: Dp? = null,
        val priority: Int? = DEFAULT_HELP_PRIORITY,
        private val shape: HelpShowcaseShape = HelpShowcaseShape.OVAL,
        val layoutCoordinates: LayoutCoordinates? = null,
) {
    @Composable
    fun asShape(
            hasNextItem: Boolean,
            goToNextItemListener: () -> Unit,
            endShowcaseListener: () -> Unit,
            screenHeight: Float,
            screenWidth: Float,
    ) = shape.asState(
            item = this,
            hasNextItem = hasNextItem,
            goToNextItemListener = goToNextItemListener,
            endShowcaseListener = endShowcaseListener,
            screenHeight = screenHeight,
            screenWidth = screenWidth,
    )
}
