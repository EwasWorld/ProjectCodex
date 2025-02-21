package eywa.projectcodex.common.sharedUi.grid

import androidx.annotation.IntRange
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

data class CodexGridItem(
        val modifier: Modifier = Modifier,
        val backgroundColor: @Composable (() -> Color)? = null,
        val backgroundShape: Shape = RectangleShape,
        val padding: PaddingValues = PaddingValues(),
        @IntRange(from = 1) val horizontalSpan: Int = 1,
        @IntRange(from = 1) val verticalSpan: Int = 1,
        val itemCount: Int = 1,
        val content: @Composable () -> Unit,
)

class CodexGridConfig {
    internal val items = mutableListOf<CodexGridItem>()

    fun item(
            modifier: Modifier = Modifier,
            backgroundColor: @Composable (() -> Color)? = null,
            backgroundShape: Shape = RectangleShape,
            padding: PaddingValues = PaddingValues(),
            horizontalSpan: Int = 1,
            verticalSpan: Int = 1,
            itemCount: Int = 1,
            content: @Composable () -> Unit,
    ) {
        items.add(
                CodexGridItem(
                        modifier = modifier,
                        backgroundColor = backgroundColor,
                        backgroundShape = backgroundShape,
                        padding = padding,
                        horizontalSpan = horizontalSpan,
                        verticalSpan = verticalSpan,
                        itemCount = itemCount,
                        content = content,
                ),
        )
    }
}

sealed class CodexGridColumn {
    data object WrapContent : CodexGridColumn()
    data class Match(val group: Int) : CodexGridColumn()
}

@Composable
fun CodexGrid(
        columns: Int,
        modifier: Modifier = Modifier,
        alignment: Alignment = Alignment.Center,
        verticalSpacing: Dp = 0.dp,
        horizontalSpacing: Dp = 0.dp,
        config: CodexGridConfig.() -> Unit,
) = CodexGrid(
        columns = List(columns) { CodexGridColumn.WrapContent },
        alignment = alignment,
        verticalSpacing = verticalSpacing,
        horizontalSpacing = horizontalSpacing,
        config = config,
        modifier = modifier
)

class IntPadding(
        val start: Int,
        val top: Int,
        val end: Int,
        val bottom: Int,
) {
    val horizontal
        get() = start + end

    val vertical
        get() = top + bottom
}

/**
 * Create a grid where each column is the width of the item with the largest width
 */
@Composable
fun CodexGrid(
        columns: List<CodexGridColumn>,
        modifier: Modifier = Modifier,
        alignment: Alignment = Alignment.Center,
        verticalSpacing: Dp = 0.dp,
        horizontalSpacing: Dp = 0.dp,
        config: CodexGridConfig.() -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    val items = CodexGridConfig().apply { config() }.items

    val verticalSpace = with(LocalDensity.current) { verticalSpacing.toPx().roundToInt() }
    val horizontalSpace = with(LocalDensity.current) { horizontalSpacing.toPx().roundToInt() }

    val columnGroups = columns.filterIsInstance<CodexGridColumn.Match>()
            .withIndex()
            .groupBy { it.value.group }
            .values
            .map { list -> list.map { it.index } }

    val density = LocalDensity.current
    val padding = items.map {
        with(density) {
            val padding = it.padding
            IntPadding(
                    start = padding.calculateLeftPadding(layoutDirection).toPx().roundToInt(),
                    top = padding.calculateTopPadding().toPx().roundToInt(),
                    end = padding.calculateRightPadding(layoutDirection).toPx().roundToInt(),
                    bottom = padding.calculateBottomPadding().toPx().roundToInt(),
            )
        }
    }

    SubcomposeLayout(modifier = modifier) { constraints ->
        val columnWidths = MutableList(columns.size) { 0 }
        val rowHeights = mutableListOf<Int>()

        /**
         * Track items that span multiple columns.
         * After measuring all items, if they don't fit within the width of their columns, expand all relevant columns
         */
        val horizontalSpanners = mutableListOf<ColumnSpanner>()

        /**
         * Track items that span multiple rows.
         * After measuring all items, if they don't fit within the width of their rows, expand all relevant rows
         */
        val verticalSpanners = mutableListOf<ColumnSpanner>()

        /**
         * Cell locations which are skipped due to the vertical span of another cell (column to row).
         * E.g. if the cell at (5,5) has a vertical span of 2, then the cell at (6,5) is in this dummy list.
         * Doesn't track horizontal spans as the cells are parsed in rows, so horizontal spans can be accounted for by
         * increasing the row index before placing the next cell
         */
        val masterSkipCells = mutableListOf<Pair<Int, Int>>()

        var columnIndex = 0
        var rowIndex = 0
        var index = 0
        var itemInternalIndex: Int? = null
        var tempHeight = 0
        var tempWidth = 0

        val itemPlaceables = subcompose(SlotsEnum.MAIN) {
            items.forEach { it.content() }
        }.map { measurable ->
            // Correct the row/column index if any cells need to be skipped
            while (masterSkipCells.contains(columnIndex to rowIndex)) {
                columnIndex++
                if (columnIndex == columns.size) {
                    columnIndex = 0
                    rowIndex++
                }
            }

            val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            val item = items[index]
            val itemPadding = padding[index]
            val horizontalSpan = item.horizontalSpan
            val verticalSpan = item.verticalSpan

            // Store any skip cells
            if (verticalSpan > 1) {
                for (j in rowIndex until (rowIndex + verticalSpan - 1)) {
                    for (i in columnIndex until (columnIndex + horizontalSpan)) {
                        masterSkipCells.add(i to j + 1)
                    }
                }
            }

            if (item.itemCount > 1) {
                itemInternalIndex = (itemInternalIndex ?: 0) + 1

                tempHeight = maxOf(tempHeight, placeable.height)
                tempWidth += placeable.width

                if (itemInternalIndex != item.itemCount) {
                    return@map placeable
                }
            }
            itemInternalIndex = null

            val height = (if (item.itemCount == 1) placeable.height else tempHeight) + itemPadding.vertical
            val width = (if (item.itemCount == 1) placeable.width else tempWidth) + itemPadding.horizontal
            tempHeight = 0
            tempWidth = 0

            fun spanner(span: Int) =
                    ColumnSpanner(col = columnIndex, row = rowIndex, span = span, height = height, width = width)

            // Update row's max height
            while (rowHeights.size <= rowIndex) {
                rowHeights.add(0)
            }
            if (verticalSpan == 1) rowHeights[rowIndex] = maxOf(height, rowHeights[rowIndex])
            else verticalSpanners.add(spanner(verticalSpan))

            // Update column's max width
            if (horizontalSpan == 1) columnWidths[columnIndex] = maxOf(width, columnWidths[columnIndex])
            else horizontalSpanners.add(spanner(horizontalSpan))

            // Go to next cell location
            columnIndex += horizontalSpan
            if (columnIndex > columns.size) throw IllegalStateException("Too many columns")
            else if (columnIndex == columns.size) {
                columnIndex = 0
                rowIndex++
            }
            index++

            placeable
        }

        // Increase row heights or column widths if needed by spanners
        verticalSpanners.forEach { spanner ->
            val verticalSpan = spanner.span
            val rowHeight =
                    rowHeights.drop(spanner.row).take(verticalSpan).sum() + verticalSpace * (verticalSpan - 1)

            val excess = (spanner.height - rowHeight) / verticalSpan
            if (excess > 0) {
                for (i in spanner.row until (spanner.row + verticalSpan)) {
                    rowHeights[i] += excess
                }
            }
        }
        horizontalSpanners.forEach { spanner ->
            val horizontalSpan = spanner.span
            val columnWidth =
                    columnWidths.drop(spanner.col).take(horizontalSpan).sum() + horizontalSpace * (horizontalSpan - 1)

            val excess = (spanner.width - columnWidth) / horizontalSpan
            if (excess > 0) {
                for (i in spanner.col until (spanner.col + horizontalSpan)) {
                    columnWidths[i] += excess
                }
            }
        }

        // Force all columns that are grouped to have the same width as the max width column
        columnGroups.forEach { group ->
            val width = columnWidths.slice(group).max()
            group.forEach { columnWidths[it] = width }
        }

        /*
         * Measure boxes
         */
        columnIndex = 0
        rowIndex = 0
        var skipCells = masterSkipCells.toMutableSet()

        val boxPlaceables = subcompose(SlotsEnum.BOXES) {
            items.forEach {
                if (it.backgroundColor != null) {
                    Surface(
                            color = it.backgroundColor.invoke(),
                            shape = it.backgroundShape,
                            modifier = it.modifier
                    ) {}
                }
                else {
                    Surface {}
                }
            }
        }.mapIndexed { i, measurable ->
            // Correct the row/column index if any cells need to be skipped
            while (skipCells.remove(columnIndex to rowIndex)) {
                columnIndex++
                if (columnIndex == columns.size) {
                    columnIndex = 0
                    rowIndex++
                }
            }

            val item = items[i]
            val horizontalSpan = item.horizontalSpan
            val verticalSpan = item.verticalSpan

            val width: Int?
            val height: Int?

            if (item.backgroundColor == null) {
                width = null
                height = null
            }
            else {
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
                val itemPadding = padding[i]
                width = columnWidth - itemPadding.horizontal
                height = rowHeight - itemPadding.vertical
            }

            // Go to next cell location
            columnIndex += horizontalSpan
            if (columnIndex == columns.size) {
                columnIndex = 0
                rowIndex++
            }

            if (width == null || height == null) {
                null
            }
            else {
                measurable.measure(
                        constraints.copy(
                                minWidth = width,
                                maxWidth = width,
                                minHeight = height,
                                maxHeight = height,
                        ),
                )
            }
        }

        /*
         * Place all items
         */
        layout(
                maxOf(constraints.minWidth, columnWidths.sum() + horizontalSpace * (columnWidths.size - 1)),
                maxOf(constraints.minHeight, rowHeights.sum() + verticalSpace * (rowHeights.size - 1)),
        ) {
            columnIndex = 0
            rowIndex = 0
            index = 0
            itemInternalIndex = null
            skipCells = masterSkipCells.toMutableSet()

            var x = 0
            var y = 0
            var tempX = 0
            var itemGap = 0

            itemPlaceables.forEachIndexed { i, itemPlaceable ->
                // Correct the row/column index if any cells need to be skipped
                while (skipCells.remove(columnIndex to rowIndex)) {
                    x += columnWidths[columnIndex] + horizontalSpace
                    columnIndex++
                    if (columnIndex == columns.size) {
                        columnIndex = 0
                        rowIndex++
                        x = 0
                        y += rowHeights[rowIndex] + verticalSpace
                    }
                }

                val item = items[index]
                val horizontalSpan = item.horizontalSpan
                val verticalSpan = item.verticalSpan
                val columnWidth = columnWidths.drop(columnIndex).take(horizontalSpan)
                        .sum() + horizontalSpace * (horizontalSpan - 1)
                val rowHeight =
                        rowHeights.drop(rowIndex).take(verticalSpan).sum() + verticalSpace * (verticalSpan - 1)
                val itemPadding = padding[index]
                val cellSize = IntSize(
                        width = columnWidth - itemPadding.horizontal,
                        height = rowHeight - itemPadding.vertical,
                )

                // Place box
                val boxPlaceable = boxPlaceables[index]
                if (boxPlaceable != null && itemInternalIndex == null) {
                    val boxOffset = alignment.align(
                            size = IntSize(width = boxPlaceable.width, height = boxPlaceable.height),
                            space = cellSize,
                            layoutDirection = layoutDirection,
                    )
                    boxPlaceable.place(
                            x = x + boxOffset.x + itemPadding.start,
                            y = y + boxOffset.y + itemPadding.top,
                    )
                }

                // Place item
                val itemOffset = alignment.align(
                        size = IntSize(width = itemPlaceable.width, height = itemPlaceable.height),
                        space = cellSize,
                        layoutDirection = layoutDirection,
                )
                if (item.itemCount == 1) {
                    itemPlaceable.place(
                            x = x + itemOffset.x + itemPadding.start,
                            y = y + itemOffset.y + itemPadding.top,
                    )
                }
                else {
                    // Handle multiple items in one cell
                    if (itemInternalIndex == null) {
                        val itemWidthsSum = itemPlaceables
                                .drop(i)
                                .take(item.itemCount)
                                .sumOf { it.width }
                        itemGap = (boxPlaceable!!.width - itemWidthsSum) / (item.itemCount - 1)
                        tempX = x + itemPadding.start
                    }
                    itemInternalIndex = (itemInternalIndex ?: 0) + 1

                    itemPlaceable.place(
                            x = tempX,
                            y = y + itemOffset.y + itemPadding.top,
                    )
                    tempX += itemPlaceable.width + itemGap

                    if (itemInternalIndex!! < item.itemCount) {
                        return@forEachIndexed
                    }
                }
                tempX = 0
                itemInternalIndex = null

                // Go to next cell location
                x += columnWidth + horizontalSpace
                columnIndex += horizontalSpan
                if (columnIndex == columns.size) {
                    x = 0
                    y += rowHeights[rowIndex] + verticalSpace
                    columnIndex = 0
                    rowIndex++
                }
                index++
            }
        }
    }
}

private data class ColumnSpanner(
        val col: Int,
        val row: Int,
        val span: Int,
        val height: Int,
        val width: Int,
)

private enum class SlotsEnum { MAIN, BOXES }
