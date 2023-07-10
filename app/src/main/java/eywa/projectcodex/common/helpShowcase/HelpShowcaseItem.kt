package eywa.projectcodex.common.helpShowcase

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Dp
import eywa.projectcodex.common.utils.ResOrActual

/**
 * @param priority Lower number = higher priority
 */
data class HelpShowcaseItem(
        val helpTitle: ResOrActual<String>,
        internal val helpBody: ResOrActual<String>,
        internal val shapePadding: Dp? = null,
        val priority: Int? = DEFAULT_HELP_PRIORITY,
        private val shape: HelpShowcaseShape = HelpShowcaseShape.OVAL,
        val layoutCoordinates: LayoutCoordinates? = null,
) {
    @Deprecated("Use string-based constructor")
    constructor(
            helpTitle: Int,
            helpBody: Int,
            shapePadding: Dp? = null,
            shape: HelpShowcaseShape = HelpShowcaseShape.OVAL,
            priority: Int? = DEFAULT_HELP_PRIORITY,
    ) : this(
            ResOrActual.fromRes(helpTitle),
            ResOrActual.fromRes(helpBody),
            shapePadding,
            priority,
            shape,
    )

    constructor(
            helpTitle: String,
            helpBody: String,
            shapePadding: Dp? = null,
            shape: HelpShowcaseShape = HelpShowcaseShape.OVAL,
            priority: Int? = DEFAULT_HELP_PRIORITY,
    ) : this(
            ResOrActual.fromActual(helpTitle),
            ResOrActual.fromActual(helpBody),
            shapePadding,
            priority,
            shape,
    )

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
