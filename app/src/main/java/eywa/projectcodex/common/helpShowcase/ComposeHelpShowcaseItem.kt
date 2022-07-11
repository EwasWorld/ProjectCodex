package eywa.projectcodex.common.helpShowcase

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import eywa.projectcodex.common.helpShowcase.ui.ComposeHelpShowcaseState

class ComposeHelpShowcaseItem(
        @StringRes var helpTitle: Int,
        @StringRes private var helpBody: Int,
        private var shapePadding: Dp? = null,
        override var priority: Int? = HelpShowcaseItem.DEFAULT_HELP_PRIORITY,
) : HelpShowcaseItem {
    private var layoutCoordinates: LayoutCoordinates? = null

    @Composable
    fun asState(
            hasNextItem: Boolean,
            goToNextItemListener: () -> Unit,
            endShowcaseListener: () -> Unit,
            screenHeight: Float,
            screenWidth: Float,
    ) = ComposeHelpShowcaseState.from(
            title = stringResource(id = helpTitle),
            message = stringResource(id = helpBody),
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