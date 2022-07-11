package eywa.projectcodex.common.helpShowcase.ui

import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.*
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.CodexButton
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * @param animationState 1 for fully visible, 0 for fully invisible (expanded off screen)
 */
@Composable
fun ComposeHelpShowcase(
        state: ComposeHelpShowcaseState,
        @FloatRange(from = 0.0, to = 1.0) animationState: Float = 1f,
) {
    require(animationState in 0f..1f) { "Invalid animation state" }

    val scale = (state.maximisedOvalScale - 1f) * (1f - animationState) + 1f
    val overlayColor = colorResource(id = R.color.colorPrimaryDark)
    Canvas(
            modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.85f)
                    .clickable(onClick = state.overlayClickedListener)
    ) {
        drawRect(
                color = overlayColor,
                topLeft = Offset(0f, 0f),
                size = this.size
        )
        drawOval(
                color = Color.Transparent,
                blendMode = BlendMode.Clear,
                topLeft = state.scaledOvalOffset(scale),
                size = state.scaledOvalSize(scale),
        )
    }

    Column(
            modifier = Modifier
                    .offset { state.textAreaTopLeft }
                    .height(with(LocalDensity.current) { state.textAreaHeight.toDp() })
                    .padding(16.dp),
            verticalArrangement = state.textAreaVerticalArrangement
    ) {
        Text(
                text = state.title,
                fontSize = 30.sp,
                modifier = Modifier
                        .padding(
                                start = 5.dp,
                                bottom = 15.dp,
                        )
                        .alpha(animationState),
                color = colorResource(id = R.color.colorLightAccent)
        )
        val messageAlpha = if (state.message.isNotBlank() && state.title.isNotBlank()) 0.5f else 1f
        Text(
                text = state.message,
                fontSize = 20.sp,
                modifier = Modifier
                        .padding(start = 5.dp)
                        .alpha(animationState * messageAlpha),
                color = Color.White
        )
        if (state.hasNextItem) {
            ClickableText(
                    text = AnnotatedString("Next"),
                    onClick = { state.nextItemListener() },
                    style = TextStyle.Default.copy(
                            fontSize = 22.sp,
                            color = Color.White
                    ),
                    modifier = Modifier
                            .padding(
                                    horizontal = 5.dp,
                                    vertical = 10.dp,
                            )
                            .alpha(animationState),
            )
        }
        ClickableText(
                text = AnnotatedString("Close help"),
                onClick = { state.closeListener() },
                style = TextStyle.Default.copy(
                        fontSize = 18.sp,
                        color = Color.White
                ),
                modifier = Modifier
                        .padding(
                                horizontal = 5.dp,
                                vertical = 10.dp,
                        )
                        .alpha(animationState),
        )
    }
}

class ComposeHelpShowcaseState(
        val title: String,
        val message: String,
        val hasNextItem: Boolean,
        val nextItemListener: () -> Unit,
        val closeListener: () -> Unit,
        val overlayClickedListener: () -> Unit,
        private val ovalTopLeft: Offset,
        private val ovalHeight: Float,
        private val ovalWidth: Float,
        private val screenHeight: Float,
        private val screenWidth: Float,
) {
    private val ovalBottomOffset = ovalTopLeft.y + ovalHeight
    private val isTextAboveOval = ovalTopLeft.y > screenHeight - ovalBottomOffset

    val textAreaHeight = if (isTextAboveOval) ovalTopLeft.y else screenHeight - ovalBottomOffset
    val textAreaTopLeft = IntOffset(
            x = 0,
            y = if (isTextAboveOval) 0 else ovalBottomOffset.roundToInt()
    )
    val textAreaVerticalArrangement = if (isTextAboveOval) Arrangement.Bottom else Arrangement.Top

    /**
     * The minimum scale factor that the oval needs to be multiplied by so that none of it is visible on the screen
     * assuming the centre of the oval and its width to height ratio remains the same
     */
    val maximisedOvalScale: Float

    init {
        val ovalCentreX = ovalTopLeft.x + ovalWidth / 2
        val ovalCentreY = ovalTopLeft.y + ovalHeight / 2
        val xDenominator = (ovalWidth / 2).pow(2)
        val yDenominator = (ovalHeight / 2).pow(2)

        maximisedOvalScale = listOf(
                0f to 0f,
                0f to screenHeight,
                screenWidth to 0f,
                screenWidth to screenHeight
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

    fun scaledOvalOffset(scale: Float): Offset {
        val extraWidth = (ovalWidth * scale - ovalWidth) / 2
        val extraHeight = (ovalHeight * scale - ovalHeight) / 2
        return Offset(ovalTopLeft.x - extraWidth, ovalTopLeft.y - extraHeight)
    }

    fun scaledOvalSize(scale: Float) = Size(ovalWidth * scale, ovalHeight * scale)

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
                screenHeight: Float,
                screenWidth: Float,
                padding: Dp = DEFAULT_PADDING,
                density: Density,
        ): ComposeHelpShowcaseState {
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

            return ComposeHelpShowcaseState(
                    title = title,
                    message = message,
                    hasNextItem = hasNextItem,
                    nextItemListener = nextButtonListener,
                    closeListener = closeButtonListener,
                    overlayClickedListener = overlayClickedListener,
                    ovalTopLeft = Offset(ovalCentreX - ovalWidth / 2, ovalCentreY - ovalHeight / 2),
                    ovalHeight = ovalHeight,
                    ovalWidth = ovalWidth,
                    screenHeight = screenHeight,
                    screenWidth = screenWidth,
            )
        }
    }
}

@Preview(
        showBackground = true,
        device = Devices.PIXEL_2,
        backgroundColor = 0xFF69BEFF
)
@Composable
private fun ComposeHelpShowcasePreview(
        @PreviewParameter(ComposeHelpShowcasePreviewProvider::class) param: ComposeHelpShowcasePreviewParams,
) {
    Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CodexButton(text = "Hello") {}
        CodexButton(text = "Hello") {}
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        ComposeHelpShowcase(
                ComposeHelpShowcaseState(
                        title = "Title",
                        message = "Message",
                        hasNextItem = param.hasNextItem,
                        nextItemListener = {},
                        closeListener = {},
                        overlayClickedListener = {},
                        ovalTopLeft = param.ovalTopLeft,
                        ovalHeight = param.ovalHeight,
                        ovalWidth = param.ovalWidth,
                        screenHeight = this.constraints.minHeight.toFloat(),
                        screenWidth = this.constraints.minWidth.toFloat(),
                )
        )
    }
}

data class ComposeHelpShowcasePreviewParams(
        val name: String,
        val ovalTopLeft: Offset,
        val ovalHeight: Float = 100f,
        val ovalWidth: Float = 150f,
        val hasNextItem: Boolean = true,
)

class ComposeHelpShowcasePreviewProvider : PreviewParameterProvider<ComposeHelpShowcasePreviewParams> {
    override val values = sequenceOf(
            ComposeHelpShowcasePreviewParams("Text below", Offset(10f, 100f)),
            ComposeHelpShowcasePreviewParams("Text above", Offset(10f, 1300f)),
            ComposeHelpShowcasePreviewParams("Roughly on button", Offset(430f, 850f), ovalWidth = 220f),
    )
}