package eywa.projectcodex.common.sharedUi

import androidx.annotation.IntRange
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

data class CodexGridItem(
        val fillBox: Boolean = false,
        @IntRange(from = 1) val horizontalSpan: Int = 1,
        @IntRange(from = 1) val verticalSpan: Int = 1,
        val content: @Composable () -> Unit,
)

class CodexGridConfig {
    internal val items = mutableListOf<CodexGridItem>()

    fun item(
            fillBox: Boolean = false,
            horizontalSpan: Int = 1,
            verticalSpan: Int = 1,
            content: @Composable () -> Unit,
    ) {
        items.add(CodexGridItem(fillBox, horizontalSpan, verticalSpan, content))
    }
}

sealed class CodexGridColumn {
    object WrapContent : CodexGridColumn()
    data class Match(val group: Int) : CodexGridColumn()
}

@Composable
fun CodexGrid(
        columns: Int,
        alignment: Alignment,
        modifier: Modifier = Modifier,
        verticalSpacing: Dp = 0.dp,
        horizontalSpacing: Dp = 0.dp,
        config: CodexGridConfig.() -> Unit,
) = CodexGrid(
        columns = List(columns) { CodexGridColumn.WrapContent },
        alignment = alignment,
        modifier = modifier,
        verticalSpacing = verticalSpacing,
        horizontalSpacing = horizontalSpacing,
        config = config
)

/**
 * Create a grid where each column is the width of the item with the largest width
 */
@Composable
fun CodexGrid(
        columns: List<CodexGridColumn>,
        alignment: Alignment,
        modifier: Modifier = Modifier,
        verticalSpacing: Dp = 0.dp,
        horizontalSpacing: Dp = 0.dp,
        config: CodexGridConfig.() -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    val items = CodexGridConfig().apply { config() }.items
    val content = @Composable { items.forEach { it.content() } }

    val verticalSpace = with(LocalDensity.current) { verticalSpacing.toPx().roundToInt() }
    val horizontalSpace = with(LocalDensity.current) { horizontalSpacing.toPx().roundToInt() }

    val columnGroups = columns.filterIsInstance<CodexGridColumn.Match>()
            .withIndex()
            .groupBy { it.value.group }
            .values
            .map { list -> list.map { it.index } }

    SubcomposeLayout(
            modifier = modifier,
    ) { constraints ->
        val columnWidths = MutableList(columns.size) { 0 }
        val rowHeights = mutableListOf<Int>()
        val horizontalSpanners = mutableListOf<ColumnSpanner>()

        var columnIndex = 0
        var rowIndex = 0
        val originalDummies = mutableListOf<Pair<Int, Int>>()

        val originalPlaceables = subcompose(SlotsEnum.MAIN, content).mapIndexed { index, measurable ->
            while (originalDummies.contains(columnIndex to rowIndex)) {
                columnIndex++
                if (columnIndex == columns.size) {
                    columnIndex = 0
                    rowIndex++
                }
            }

            val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            val horizontalSpan = items[index].horizontalSpan
            val verticalSpan = items[index].verticalSpan

            if (verticalSpan > 1) {
                for (j in rowIndex until (rowIndex + verticalSpan - 1)) {
                    for (i in columnIndex until (columnIndex + horizontalSpan)) {
                        originalDummies.add(i to j + 1)
                    }
                }
            }

            while (rowHeights.size <= rowIndex) {
                rowHeights.add(0)
            }
            rowHeights[rowIndex] = maxOf(placeable.height, rowHeights[rowIndex])

            if (horizontalSpan == 1) {
                columnWidths[columnIndex] = maxOf(placeable.width, columnWidths[columnIndex])
            }
            else {
                horizontalSpanners.add(ColumnSpanner(columnIndex, rowIndex, horizontalSpan, placeable))
            }

            columnIndex += horizontalSpan
            if (columnIndex > columns.size) throw IllegalStateException("Too many columns")
            else if (columnIndex == columns.size) {
                columnIndex = 0
                rowIndex++
            }

            placeable
        }

        horizontalSpanners.forEach { spanner ->
            val horizontalSpan = spanner.span
            val columnWidth =
                    columnWidths.drop(spanner.x).take(horizontalSpan).sum() + horizontalSpace * (horizontalSpan - 1)

            val excess = (spanner.placeable.width - columnWidth) / horizontalSpan
            if (excess > 0) {
                for (i in spanner.x until (spanner.x + horizontalSpan)) {
                    columnWidths[i] += excess
                }
            }
        }

        columnGroups.forEach { group ->
            val width = columnWidths.slice(group).max()
            group.forEach { columnWidths[it] = width }
        }

        columnIndex = 0
        rowIndex = 0
        var dummies = originalDummies.toMutableSet()
        val placeables = subcompose(SlotsEnum.DEPENDANT, content).mapIndexed { index, measurable ->
            while (dummies.remove(columnIndex to rowIndex)) {
                columnIndex++
                if (columnIndex == columns.size) {
                    columnIndex = 0
                    rowIndex++
                }
            }

            val horizontalSpan = items[index].horizontalSpan
            val verticalSpan = items[index].verticalSpan

            val placeable = if (items[index].fillBox) {
                val columnWidth =
                        if (horizontalSpan == 1) {
                            columnWidths[columnIndex]
                        }
                        else {
                            columnWidths.drop(columnIndex).take(horizontalSpan).sum() +
                                    horizontalSpace * (horizontalSpan - 1)
                        }
                val rowHeight =
                        if (verticalSpan == 1) {
                            rowHeights[rowIndex]
                        }
                        else {
                            rowHeights.drop(rowIndex).take(verticalSpan).sum() +
                                    verticalSpace * (verticalSpan - 1)
                        }
                measurable.measure(
                        constraints.copy(
                                minWidth = columnWidth,
                                maxWidth = columnWidth,
                                minHeight = rowHeight,
                                maxHeight = rowHeight,
                        )
                )
            }
            else {
                originalPlaceables[index]
            }

            columnIndex += horizontalSpan
            if (columnIndex == columns.size) {
                columnIndex = 0
                rowIndex++
            }

            placeable
        }

        layout(
                columnWidths.sum() + horizontalSpace * (columnWidths.size - 1),
                rowHeights.sum() + verticalSpace * (rowHeights.size - 1),
        ) {
            columnIndex = 0
            rowIndex = 0
            dummies = originalDummies.toMutableSet()

            var x = 0
            var y = 0

            placeables.forEachIndexed { index, placeable ->
                while (dummies.remove(columnIndex to rowIndex)) {
                    x += columnWidths[columnIndex] + horizontalSpace
                    columnIndex++
                    if (columnIndex == columns.size) {
                        columnIndex = 0
                        rowIndex++
                        x = 0
                        y += rowHeights[rowIndex] + verticalSpace
                    }
                }

                val horizontalSpan = items[index].horizontalSpan
                val verticalSpan = items[index].verticalSpan
                val columnWidth = columnWidths.drop(columnIndex).take(horizontalSpan)
                        .sum() + horizontalSpace * (horizontalSpan - 1)
                val rowHeight =
                        rowHeights.drop(rowIndex).take(verticalSpan).sum() + verticalSpace * (verticalSpan - 1)

                val offset = alignment.align(
                        IntSize(placeable.width, placeable.height),
                        IntSize(columnWidth, rowHeight),
                        layoutDirection,
                )

                placeable.place(x = x + offset.x, y = y + offset.y)
                if (columnIndex < columns.size - horizontalSpan) {
                    x += columnWidth + horizontalSpace
                }
                else {
                    x = 0
                    y += rowHeights[rowIndex] + verticalSpace
                }

                columnIndex += horizontalSpan
                if (columnIndex == columns.size) {
                    columnIndex = 0
                    rowIndex++
                }
            }
        }
    }
}

data class ColumnSpanner(
        val x: Int,
        val y: Int,
        val span: Int,
        val placeable: Placeable,
)

private enum class SlotsEnum { MAIN, DEPENDANT }
