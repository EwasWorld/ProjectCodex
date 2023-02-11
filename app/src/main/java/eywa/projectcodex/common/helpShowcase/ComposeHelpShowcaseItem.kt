package eywa.projectcodex.common.helpShowcase

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import eywa.projectcodex.common.helpShowcase.ui.ComposeHelpShowcaseState
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.get

/**
 * @param priority Lower number = higher priority
 */
class ComposeHelpShowcaseItem(
        var helpTitle: ResOrActual<String>,
        private var helpBody: ResOrActual<String>,
        private var shapePadding: Dp? = null,
        var priority: Int? = DEFAULT_HELP_PRIORITY,
) {
    private var layoutCoordinates: LayoutCoordinates? = null

    constructor(
            helpTitle: Int,
            helpBody: Int,
            shapePadding: Dp? = null,
            priority: Int? = DEFAULT_HELP_PRIORITY,
    ) : this(
            ResOrActual.fromRes(helpTitle),
            ResOrActual.fromRes(helpBody),
            shapePadding,
            priority,
    )

    constructor(
            helpTitle: String,
            helpBody: String,
            shapePadding: Dp? = null,
            priority: Int? = DEFAULT_HELP_PRIORITY,
    ) : this(
            ResOrActual.fromActual(helpTitle),
            ResOrActual.fromActual(helpBody),
            shapePadding,
            priority,
    )

    @Composable
    fun asState(
            hasNextItem: Boolean,
            goToNextItemListener: () -> Unit,
            endShowcaseListener: () -> Unit,
            screenHeight: Float,
            screenWidth: Float,
    ) = ComposeHelpShowcaseState.from(
            title = helpTitle.get(),
            message = helpBody.get(),
            hasNextItem = hasNextItem,
            viewInfo = layoutCoordinates!!,
            screenHeight = screenHeight,
            screenWidth = screenWidth,
            padding = shapePadding ?: ComposeHelpShowcaseState.DEFAULT_PADDING,
            density = LocalDensity.current,
            closeButtonListener = endShowcaseListener,
            nextButtonListener = goToNextItemListener,
            overlayClickedListener = goToNextItemListener
    )

    fun updateLayoutCoordinates(layoutCoordinates: LayoutCoordinates) {
        this.layoutCoordinates = layoutCoordinates
    }

    /**
     * The shape the showcase will use to highlight the given view
     * TODO Implement more help showcase shapes?
     */
    enum class Shape { CIRCLE, OVAL, RECTANGLE, NO_SHAPE }
}
