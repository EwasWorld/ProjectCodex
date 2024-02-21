package eywa.projectcodex.common.helpShowcase.ui

import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset

class HelpShowcaseNoShapeState(
        title: String,
        message: String,
        hasNextItem: Boolean,
        screenSize: Size,
        nextItemListener: () -> Unit,
        closeListener: () -> Unit,
        overlayClickedListener: () -> Unit,
) : HelpShowcaseOvalState(
        ovalTopLeft = Offset(x = screenSize.width, y = screenSize.height).div(2f),
        ovalHeight = 1f,
        ovalWidth = 1f,
        title = title,
        message = message,
        hasNextItem = hasNextItem,
        nextItemListener = nextItemListener,
        closeListener = closeListener,
        overlayClickedListener = overlayClickedListener,
        screenSize = screenSize,
) {
    override val textAreaHeight: Float = screenSize.height
    override val textAreaTopLeft: IntOffset = IntOffset.Zero
    override val textAreaVerticalArrangement: Alignment.Vertical = Alignment.Top

    override fun drawCutout(scope: DrawScope, animationState: Float) {
        // If the showcase is fully visible, there should be no cutout
        if (animationState == 1f) return

        // Otherwise, animate an oval coming in
        super.drawCutout(scope, animationState)
    }
}
