package eywa.projectcodex.common.helpShowcase.ui

import androidx.annotation.FloatRange
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset


interface HelpShowcaseState {
    val title: String
    val message: String
    val hasNextItem: Boolean
    val nextItemListener: () -> Unit
    val closeListener: () -> Unit
    val overlayClickedListener: () -> Unit
    val textAreaHeight: Float
    val textAreaTopLeft: IntOffset
    val textAreaVerticalArrangement: Alignment.Vertical

    fun drawCutout(
            scope: DrawScope,
            @FloatRange(from = 0.0, to = 1.0) animationState: Float = 1f,
    )
}
