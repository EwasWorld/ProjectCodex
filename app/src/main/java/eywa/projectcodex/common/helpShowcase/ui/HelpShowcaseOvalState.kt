package eywa.projectcodex.common.helpShowcase.ui

import androidx.annotation.FloatRange
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

open class HelpShowcaseOvalState(
        private val ovalTopLeft: Offset,
        private val ovalHeight: Float,
        private val ovalWidth: Float,
        final override val title: String,
        final override val message: String,
        override val hasNextItem: Boolean,
        override val nextItemListener: () -> Unit,
        override val closeListener: () -> Unit,
        override val overlayClickedListener: () -> Unit,
        screenSize: Size,
) : HelpShowcaseState {
    init {
        require(title.isNotBlank()) { "Showcase title cannot be blank" }
        require(message.isNotBlank()) { "Showcase message cannot be blank" }
    }

    private val ovalBottomOffset = ovalTopLeft.y + ovalHeight
    private val isTextAboveOval = ovalTopLeft.y > screenSize.height - ovalBottomOffset

    override val textAreaHeight = if (isTextAboveOval) ovalTopLeft.y else screenSize.height - ovalBottomOffset
    override val textAreaTopLeft = IntOffset(
            x = 0,
            y = if (isTextAboveOval) 0 else ovalBottomOffset.roundToInt()
    )
    override val textAreaVerticalArrangement = if (isTextAboveOval) Alignment.Bottom else Alignment.Top

    /**
     * The minimum scale factor that the oval needs to be multiplied by so that none of it is visible on the screen
     * assuming the centre of the oval and its width to height ratio remains the same
     */
    private val maximisedOvalScale: Float

    init {
        val ovalCentreX = ovalTopLeft.x + ovalWidth / 2
        val ovalCentreY = ovalTopLeft.y + ovalHeight / 2
        val xDenominator = (ovalWidth / 2).pow(2)
        val yDenominator = (ovalHeight / 2).pow(2)

        maximisedOvalScale = listOf(
                0f to 0f,
                0f to screenSize.height,
                screenSize.width to 0f,
                screenSize.width to screenSize.height,
        )
                .maxOf { (x, y) ->
                    /*
                     * Find what to multiply the width and height by
                     * so that the coordinates (x, y) are on the ellipse's perimeter
                     *
                     * Done using the equation of an ellipse:
                     *      (x - ovalCentreX)^2 / ovalHorizontalRadius^2
                     *      + (y - ovalCentreY)^2 / ovalVerticalRadius^2
                     *      = 1
                     * If you multiply ovalHorizontalRadius and ovalVerticalRadius by a scale factor, z,
                     *      when you solve for z, the = 1 is replaces with z^2
                     */
                    sqrt(
                            (x - ovalCentreX).pow(2) / xDenominator
                                    + (y - ovalCentreY).pow(2) / yDenominator
                    )
                }
    }

    private fun scaledOvalOffset(scale: Float): Offset {
        val extraWidth = (ovalWidth * scale - ovalWidth) / 2
        val extraHeight = (ovalHeight * scale - ovalHeight) / 2
        return Offset(ovalTopLeft.x - extraWidth, ovalTopLeft.y - extraHeight)
    }

    private fun scaledOvalSize(scale: Float) = Size(ovalWidth * scale, ovalHeight * scale)

    override fun drawCutout(
            scope: DrawScope,
            @FloatRange(from = 0.0, to = 1.0) animationState: Float,
    ) {
        val scale = (maximisedOvalScale - 1f) * (1f - animationState) + 1f

        with(scope) {
            drawOval(
                    color = Color.Transparent,
                    blendMode = BlendMode.Clear,
                    topLeft = scaledOvalOffset(scale),
                    size = scaledOvalSize(scale),
            )
        }
    }

    companion object {
        val DEFAULT_PADDING = 6.dp

        fun from(
                title: String,
                message: String,
                hasNextItem: Boolean,
                nextButtonListener: () -> Unit,
                closeButtonListener: () -> Unit,
                overlayClickedListener: () -> Unit,
                viewInfo: LayoutCoordinates,
                screenSize: Size,
                padding: Dp = DEFAULT_PADDING,
                density: Density,
        ): HelpShowcaseOvalState {
            val paddingPx = with(density) { padding.toPx() }
            val (viewX, viewY) = viewInfo.positionInRoot().minus(Offset(paddingPx, paddingPx))
            val viewWidth = viewInfo.size.width + 2 * paddingPx
            val viewHeight = viewInfo.size.height + 2 * paddingPx

            val ovalCentreX = viewX + viewWidth / 2
            val ovalCentreY = viewY + viewHeight / 2

            val ratio = viewHeight / viewWidth
            val ovalWidth = sqrt(
                    (viewX - viewWidth / 2 - ovalCentreX).pow(2)
                            + (viewY - viewHeight / 2 - ovalCentreY).pow(2) / ratio
            )
            val ovalHeight = ovalWidth * ratio

            return HelpShowcaseOvalState(
                    title = title,
                    message = message,
                    hasNextItem = hasNextItem,
                    nextItemListener = nextButtonListener,
                    closeListener = closeButtonListener,
                    overlayClickedListener = overlayClickedListener,
                    ovalTopLeft = Offset(ovalCentreX - ovalWidth / 2, ovalCentreY - ovalHeight / 2),
                    ovalHeight = ovalHeight,
                    ovalWidth = ovalWidth,
                    screenSize = screenSize,
            )
        }
    }
}
