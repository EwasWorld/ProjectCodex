package eywa.projectcodex.common.sharedUi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import kotlin.math.max
import kotlin.math.roundToInt


@Composable
fun WrappingRow(
        verticalAlignment: Alignment.Vertical = Alignment.Top,
        modifier: Modifier = Modifier,
        spacing: Dp = 5.dp,
        content: @Composable () -> Unit
) {
    val spacingPx = with(LocalDensity.current) { spacing.toPx().roundToInt() }

    Layout(
            modifier = modifier,
            content = content,
    ) { measurables, constraints ->
        // Measure each child
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }

        // Calculate the height and width
        val info = place(placeables, spacingPx, constraints, verticalAlignment)

        check(info.totalWidth <= constraints.maxWidth && info.totalHeight <= constraints.maxHeight) { "Doesn't fit!" }

        layout(info.totalWidth, info.totalHeight) {
            place(
                    placeables,
                    spacingPx,
                    constraints,
                    verticalAlignment,
                    info.rowHeights,
            ) { placeable, x, y -> placeable.placeRelative(x = x, y = y) }
        }
    }
}

private fun place(
        placeables: List<Placeable>,
        spacing: Int,
        constraints: Constraints,
        verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
        inputRowHeights: List<Int>? = null,
        onEach: ((Placeable, x: Int, y: Int) -> Unit)? = null
): WrappingRowInfo {
    var currentRowMaxHeight = 0
    var totalMaxWidth = 0
    var yPosition = 0
    var xPosition = 0
    val rowHeights = mutableListOf<Int>()

    for (placeable in placeables) {
        if (xPosition + placeable.width > constraints.maxWidth) {
            totalMaxWidth = max(totalMaxWidth, xPosition - spacing)
            xPosition = 0
            yPosition += currentRowMaxHeight
            rowHeights.add(currentRowMaxHeight)
            currentRowMaxHeight = 0
        }

        val alignment = inputRowHeights?.let {
            verticalAlignment.align(placeable.height, inputRowHeights[rowHeights.size])
        } ?: 0

        // TODO_CURRENT stop when you get to the max height
//        if (yPosition + placeable.height > constraints.maxHeight) break
        onEach?.invoke(placeable, xPosition, yPosition + alignment)

        xPosition += placeable.width + spacing
        currentRowMaxHeight = max(currentRowMaxHeight, placeable.height)
    }

    rowHeights.add(currentRowMaxHeight)
    return WrappingRowInfo(
            totalWidth = max(totalMaxWidth, xPosition - spacing),
            totalHeight = yPosition + currentRowMaxHeight,
            rowHeights = rowHeights,
    )
}


private data class WrappingRowInfo(
        val totalWidth: Int,
        val totalHeight: Int,
        val rowHeights: List<Int>,
)

@Preview(showBackground = true)
@Composable
fun WrappingRow_Preview() {
    CodexTheme {
        WrappingRow(
                modifier = Modifier.width(250.dp)
        ) {
            repeat(10) {
                Text("Hello.")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Centre_WrappingRow_Preview() {
    CodexTheme {
        WrappingRow(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.width(250.dp)
        ) {
            Text("Hello", style = CodexTypography.NORMAL)
            Text("small.", style = CodexTypography.SMALL)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Bottom_WrappingRow_Preview() {
    // TODO Why is this horizontally centred?
    CodexTheme {
        WrappingRow(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.width(250.dp)
        ) {
            Text("Hello", style = CodexTypography.NORMAL)
            Text("small.", style = CodexTypography.SMALL)
        }
    }
}

@Preview(
        showBackground = true,
        heightDp = 40,
)
@Composable
fun HeightConstraint_WrappingRow_Preview() {
    CodexTheme {
        WrappingRow(
                modifier = Modifier
                        .size(250.dp, 30.dp)
                        .background(CodexColors.COLOR_PRIMARY)
        ) {
            Text("Hello. I am here.")
            Text("Hello. I am here.")
            Text("Hello. I am here.")
        }
    }
}