package eywa.projectcodex.common.helpShowcase

import androidx.appcompat.app.AppCompatActivity
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
        override var priority: Int? = HelpShowcaseItem.DEFAULT_HELP_PRIORITY,
) : HelpShowcaseItem {
    private var layoutCoordinates: LayoutCoordinates? = null

    constructor(
            helpTitle: Int,
            helpBody: Int,
            shapePadding: Dp? = null,
            priority: Int? = HelpShowcaseItem.DEFAULT_HELP_PRIORITY,
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
            priority: Int? = HelpShowcaseItem.DEFAULT_HELP_PRIORITY,
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

    override fun show(
            activity: AppCompatActivity,
            hasNextItem: Boolean,
            goToNextItemListener: () -> Unit,
            endShowcaseListener: () -> Unit
    ) {
        // Should be shown/hidden from MainActivity
        throw NotImplementedError("Compose help showcase show triggered")
    }

    override fun hide(activity: AppCompatActivity) {
        // Should be shown/hidden from MainActivity
        throw NotImplementedError("Compose help showcase show hide")
    }

    fun updateLayoutCoordinates(layoutCoordinates: LayoutCoordinates) {
        this.layoutCoordinates = layoutCoordinates
    }
}
