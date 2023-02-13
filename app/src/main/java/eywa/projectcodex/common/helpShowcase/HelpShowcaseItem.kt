package eywa.projectcodex.common.helpShowcase

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import eywa.projectcodex.common.helpShowcase.ui.ComposeHelpShowcase
import eywa.projectcodex.common.helpShowcase.ui.ComposeHelpShowcaseState
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.get

/**
 * @param priority Lower number = higher priority
 */
data class HelpShowcaseItem(
        val helpTitle: ResOrActual<String>,
        private val helpBody: ResOrActual<String>,
        private val shapePadding: Dp? = null,
        val priority: Int? = DEFAULT_HELP_PRIORITY,
        val layoutCoordinates: LayoutCoordinates? = null
) {

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
    fun Showcase(
            hasNextItem: Boolean,
            goToNextItemListener: () -> Unit,
            endShowcaseListener: () -> Unit,
            screenHeight: Float,
            screenWidth: Float,
            @FloatRange(from = 0.0, to = 1.0) animationState: Float = 1f,
    ) {
        if (layoutCoordinates == null || !layoutCoordinates.isAttached) return
        ComposeHelpShowcase(
                ComposeHelpShowcaseState.from(
                        title = helpTitle.get(),
                        message = helpBody.get(),
                        hasNextItem = hasNextItem,
                        viewInfo = layoutCoordinates,
                        screenHeight = screenHeight,
                        screenWidth = screenWidth,
                        padding = shapePadding ?: ComposeHelpShowcaseState.DEFAULT_PADDING,
                        density = LocalDensity.current,
                        closeButtonListener = endShowcaseListener,
                        nextButtonListener = goToNextItemListener,
                        overlayClickedListener = goToNextItemListener
                ),
                animationState = animationState
        )
    }

    /**
     * The shape the showcase will use to highlight the given view
     * TODO Implement more help showcase shapes?
     */
    enum class Shape { CIRCLE, OVAL, RECTANGLE, NO_SHAPE }
}
