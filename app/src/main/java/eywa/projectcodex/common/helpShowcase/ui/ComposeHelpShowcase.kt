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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.*
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexButtonDefaults
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

// TODO Better above/below logic - can it default to below unless there isn't enough room rather than taking the larger?
// TODO Look at alignment? Right align if oval is on the right?

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
    val overlayColor = CodexTheme.colors.helpShowcaseScrim

    Box(
            contentAlignment = Alignment.TopStart
    ) {
        Canvas(
                modifier = Modifier
                        .fillMaxSize()
                        // The clear oval doesn't work unless this is <1 - can't remember why, maybe the graphics layer?
                        .alpha(0.998f)
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
                        .padding(16.dp)
                        .padding(horizontal = 5.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp, state.textAreaVerticalArrangement),
        ) {
            Text(
                    text = state.title,
                    style = CodexTypography.NORMAL.copy(
                            fontSize = 30.sp,
                            color = CodexTheme.colors.helpShowcaseTitle,
                    ),
                    modifier = Modifier
                            .padding(bottom = 5.dp)
                            .alpha(animationState)
                            .testTag(ComposeHelpShowcaseTestTag.TITLE)
            )
            Text(
                    text = state.message,
                    style = CodexTypography.NORMAL.copy(
                            fontSize = 20.sp,
                            color = CodexTheme.colors.helpShowcaseMessage,
                    ),
                    modifier = Modifier.alpha(animationState)
            )
            if (state.hasNextItem) {
                ClickableText(
                        text = AnnotatedString(stringResource(id = R.string.general_next)),
                        onClick = { state.nextItemListener() },
                        style = CodexTypography.NORMAL.copy(
                                fontSize = 22.sp,
                                color = CodexTheme.colors.helpShowcaseButton
                        ),
                        modifier = Modifier
                                .padding(bottom = 5.dp)
                                .alpha(animationState)
                                .testTag(ComposeHelpShowcaseTestTag.NEXT_BUTTON)
                )
            }
            ClickableText(
                    text = AnnotatedString(stringResource(id = R.string.action_bar__close_help)),
                    onClick = { state.closeListener() },
                    style = CodexTypography.NORMAL.copy(
                            fontSize = 18.sp,
                            color = CodexTheme.colors.helpShowcaseButton
                    ),
                    modifier = Modifier
                            .alpha(animationState)
                            .testTag(ComposeHelpShowcaseTestTag.CLOSE_BUTTON)
            )
        }
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
    init {
        require(title.isNotBlank()) { "Showcase title cannot be blank" }
        require(message.isNotBlank()) { "Showcase message cannot be blank" }
    }

    private val ovalBottomOffset = ovalTopLeft.y + ovalHeight
    private val isTextAboveOval = ovalTopLeft.y > screenHeight - ovalBottomOffset

    val textAreaHeight = if (isTextAboveOval) ovalTopLeft.y else screenHeight - ovalBottomOffset
    val textAreaTopLeft = IntOffset(
            x = 0,
            y = if (isTextAboveOval) 0 else ovalBottomOffset.roundToInt()
    )
    val textAreaVerticalArrangement = if (isTextAboveOval) Alignment.Bottom else Alignment.Top

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
        CodexButton(text = "Hello", buttonStyle = CodexButtonDefaults.DefaultButton()) {}
        CodexButton(text = "Hello", buttonStyle = CodexButtonDefaults.DefaultButton()) {}
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
        val ovalTopLeft: Offset = Offset(10f, 100f),
        val ovalHeight: Float = 100f,
        val ovalWidth: Float = 150f,
        val hasNextItem: Boolean = true,
)

class ComposeHelpShowcasePreviewProvider : PreviewParameterProvider<ComposeHelpShowcasePreviewParams> {
    override val values = sequenceOf(
            ComposeHelpShowcasePreviewParams("Text below", Offset(10f, 100f)),
            ComposeHelpShowcasePreviewParams("Text above", Offset(10f, 1300f)),
            ComposeHelpShowcasePreviewParams("Roughly on button", Offset(430f, 850f), ovalWidth = 220f),
            ComposeHelpShowcasePreviewParams("No next", hasNextItem = false),
    )
}

object ComposeHelpShowcaseTestTag {
    const val TITLE = "HELP_TITLE_TEXT"
    const val NEXT_BUTTON = "HELP_NEXT_BUTTON"
    const val CLOSE_BUTTON = "HELP_CLOSE_BUTTON"
}
