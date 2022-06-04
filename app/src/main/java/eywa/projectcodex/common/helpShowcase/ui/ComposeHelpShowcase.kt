package eywa.projectcodex.common.helpShowcase.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun ComposeHelpShowcase(
        ovalTopLeft: Offset,
        ovalHeight: Float,
        ovalWidth: Float,
        screenHeight: Float,
        screenWidth: Float,
        shown: Boolean,
        color: Color = colorResource(id = R.color.colorPrimaryDarkTransparent),
        alpha: Float = 0.8f,
        onDismissListener: () -> Unit
) {
    val targetValue = if (shown) {
        1f
    }
    else {
        getRequiredScale(
                ovalTopLeft.x, ovalTopLeft.y,
                ovalHeight, ovalWidth,
                screenWidth, screenHeight
        )
    }
    val state: Float by animateFloatAsState(
            targetValue = targetValue,
            animationSpec = tween(500)
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
        val extraWidth = (ovalWidth * state - ovalWidth) / 2
        val extraHeight = (ovalHeight * state - ovalHeight) / 2
        drawOval(
                color = Color.Transparent,
                blendMode = BlendMode.Clear,
                topLeft = Offset(ovalTopLeft.x - extraWidth, ovalTopLeft.y - extraHeight),
                size = Size(ovalWidth * state, ovalHeight * state),
        )
    }
}

@Composable
fun ComposeHelpShowcase(
        viewInfo: LayoutCoordinates,
        screenHeight: Float,
        screenWidth: Float,
        shown: Boolean,
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
    val ovalWidth = sqrt((viewX - ovalCentreX).pow(2) + (viewY - ovalCentreY).pow(2) / ratio)
    val ovalHeight = ovalWidth * ratio

    ComposeHelpShowcase(
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
        heightDp = 200,
        widthDp = 100
)
@Composable
fun ComposeHelpShowcasePreview() {
    with(LocalDensity.current) {
        ComposeHelpShowcase(
                ovalTopLeft = Offset(10f, 50f),
                ovalHeight = 100f,
                ovalWidth = 150f,
                screenHeight = 200.dp.toPx(),
                screenWidth = 100.dp.toPx(),
                shown = true,
                onDismissListener = {}
        )
    }
}

@Preview(
        showBackground = true,
        heightDp = 200,
        widthDp = 100
)
@Composable
fun ComposeHelpShowcaseHiddenPreview() {
    with(LocalDensity.current) {
        ComposeHelpShowcase(
                ovalTopLeft = Offset(10f, 50f),
                ovalHeight = 100f,
                ovalWidth = 150f,
                screenHeight = 200.dp.toPx(),
                screenWidth = 100.dp.toPx(),
                shown = false,
                onDismissListener = {}
        )
    }
}
