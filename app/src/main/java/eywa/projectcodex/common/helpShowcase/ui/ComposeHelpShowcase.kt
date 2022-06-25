package eywa.projectcodex.common.helpShowcase.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState.Visible
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.CodexButton
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ComposeHelpShowcase(
        title: String = "Title",
        message: String = "message",
        nextButtonListener: (() -> Unit)? = null,
        closeButtonListener: () -> Unit = {},
        ovalTopLeft: Offset,
        ovalHeight: Float,
        ovalWidth: Float,
        screenHeight: Float,
        screenWidth: Float,
        shown: MutableTransitionState<Boolean>,
        color: Color = colorResource(id = R.color.colorPrimaryDarkTransparent),
        alpha: Float = 0.8f,
        onDismissListener: () -> Unit
) {
    val scaleWhenGone by remember {
        mutableStateOf(
                getRequiredScale(
                        ovalTopLeft.x, ovalTopLeft.y,
                        ovalHeight, ovalWidth,
                        screenWidth, screenHeight
                )
        )
    }
    val ovalBottomOffset = ovalTopLeft.y + ovalHeight
    val isTextAbove = ovalTopLeft.y > screenHeight - ovalBottomOffset

    AnimatedVisibility(
            visibleState = shown,
            enter = EnterTransition.None,
            exit = ExitTransition.None
    ) {
        val scale by transition.animateFloat(
                targetValueByState = { if (it == Visible) 1f else scaleWhenGone },
                transitionSpec = {
                    tween(
                            durationMillis = 300,
                            easing = if (targetState == Visible) LinearOutSlowInEasing else FastOutLinearInEasing
                    )
                },
                label = "",
        )

        Canvas(
                modifier = Modifier
                        .fillMaxSize()
                        .alpha(alpha)
                        .clickable(onClick = onDismissListener)
        ) {
            drawRect(
                    color = color,
                    topLeft = Offset(0f, 0f),
                    size = this.size
            )
            val extraWidth = (ovalWidth * scale - ovalWidth) / 2
            val extraHeight = (ovalHeight * scale - ovalHeight) / 2
            drawOval(
                    color = Color.Transparent,
                    blendMode = BlendMode.Clear,
                    topLeft = Offset(ovalTopLeft.x - extraWidth, ovalTopLeft.y - extraHeight),
                    size = Size(ovalWidth * scale, ovalHeight * scale),
            )
        }

        Column(
                modifier = Modifier
                        .offset {
                            IntOffset(
                                    x = 0,
                                    y = if (isTextAbove) 0 else ovalBottomOffset.roundToInt()
                            )
                        }
                        .height(
                                with(LocalDensity.current) {
                                    (if (isTextAbove) ovalTopLeft.y else screenHeight - ovalBottomOffset).toDp()
                                }
                        )
                        .padding(16.dp),
                verticalArrangement = if (isTextAbove) Arrangement.Bottom else Arrangement.Top
        ) {
            Text(
                    text = title,
                    fontSize = 30.sp,
                    modifier = Modifier.padding(
                            start = 5.dp,
                            bottom = 15.dp,
                    ),
                    color = colorResource(id = R.color.colorLightAccent)
            )
            Text(
                    text = message,
                    fontSize = 20.sp,
                    modifier = Modifier
                            .padding(start = 5.dp)
                            .alpha(if (message.isNotBlank() && title.isNotBlank()) 0.5f else 1f),
                    color = Color.White
            )
            if (nextButtonListener != null) {
                ClickableText(
                        text = AnnotatedString("Next"),
                        onClick = { nextButtonListener() },
                        style = TextStyle.Default.copy(
                                fontSize = 22.sp,
                                color = Color.White
                        ),
                        modifier = Modifier.padding(
                                horizontal = 5.dp,
                                vertical = 10.dp,
                        ),
                )
            }
            ClickableText(
                    text = AnnotatedString("Close help"),
                    onClick = { closeButtonListener() },
                    style = TextStyle.Default.copy(
                            fontSize = 18.sp,
                            color = Color.White
                    ),
                    modifier = Modifier.padding(
                            horizontal = 5.dp,
                            vertical = 10.dp,
                    ),
            )
        }
    }
}

@Composable
fun ComposeHelpShowcase(
        title: String = "Title",
        message: String = "message",
        nextButtonListener: (() -> Unit)? = null,
        closeButtonListener: () -> Unit = {},
        viewInfo: LayoutCoordinates,
        screenHeight: Float,
        screenWidth: Float,
        shown: MutableTransitionState<Boolean>,
        padding: Dp = 6.dp,
        color: Color = colorResource(id = R.color.colorPrimaryDarkTransparent),
        alpha: Float = 0.8f,
        onDismissListener: () -> Unit
) {
    val paddingPx = with(LocalDensity.current) { padding.toPx() }
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

    ComposeHelpShowcase(
            title,
            message,
            nextButtonListener,
            closeButtonListener,
            ovalTopLeft = Offset(ovalCentreX - ovalWidth / 2, ovalCentreY - ovalHeight / 2),
            ovalHeight = ovalHeight,
            ovalWidth = ovalWidth,
            screenHeight = screenHeight,
            screenWidth = screenWidth,
            shown = shown,
            color = color,
            alpha = alpha,
            onDismissListener = onDismissListener
    )
}

/**
 * @param ovalTopLeftY 0 is at the top, positive numbers go down the canvas
 * @param ovalTopLeftX 0 is on the left, positive numbers go across the canvas to the right
 */
fun getRequiredScale(
        ovalTopLeftX: Float,
        ovalTopLeftY: Float,
        ovalHeight: Float,
        ovalWidth: Float,
        screenWidth: Float,
        screenHeight: Float
): Float {
    val ovalCentreX = ovalTopLeftX + ovalWidth / 2
    val ovalCentreY = ovalTopLeftY + ovalHeight / 2
    val xDenominator = (ovalWidth / 2).pow(2)
    val yDenominator = (ovalHeight / 2).pow(2)

    return listOf(
            0f to 0f,
            0f to screenHeight,
            screenWidth to 0f,
            screenWidth to screenHeight
    )
            .map { (x, y) ->
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
            .maxOf { it }
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
                nextButtonListener = {},
                ovalTopLeft = param.ovalTopLeft,
                ovalHeight = param.ovalHeight,
                ovalWidth = param.ovalWidth,
                screenHeight = this.constraints.minHeight.toFloat(),
                screenWidth = this.constraints.minWidth.toFloat(),
                shown = MutableTransitionState(true),
                onDismissListener = {}
        )
    }
}

data class ComposeHelpShowcasePreviewParams(
        val name: String,
        val ovalTopLeft: Offset,
        val ovalHeight: Float = 100f,
        val ovalWidth: Float = 150f,
)

class ComposeHelpShowcasePreviewProvider : PreviewParameterProvider<ComposeHelpShowcasePreviewParams> {
    override val values = sequenceOf(
            ComposeHelpShowcasePreviewParams("Text below", Offset(10f, 100f)),
            ComposeHelpShowcasePreviewParams("Text above", Offset(10f, 1300f)),
            ComposeHelpShowcasePreviewParams("Roughly on button", Offset(430f, 850f), ovalWidth = 220f),
    )
}