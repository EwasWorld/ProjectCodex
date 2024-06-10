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
        @IntRange(from = 1) val columnSpan: Int = 1,
        val content: @Composable () -> Unit,
)

class CodexGridConfig {
    internal val items = mutableListOf<CodexGridItem>()

    fun item(
            fillBox: Boolean = false,
            columnSpan: Int = 1,
            content: @Composable () -> Unit,
    ) {
        items.add(CodexGridItem(fillBox, columnSpan, content))
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
        val columnSpanners = mutableListOf<ColumnSpanner>()

        var columnIndex = 0
        var rowIndex = 0

        val originalPlaceables = subcompose(SlotsEnum.MAIN, content).mapIndexed { index, measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            val span = items[index].columnSpan

            if (columnIndex == 0) {
                rowHeights.add(0)
            }
            rowHeights[rowIndex] = maxOf(placeable.height, rowHeights[rowIndex])

            if (span == 1) {
                columnWidths[columnIndex] = maxOf(placeable.width, columnWidths[columnIndex])
            }
            else {
                columnSpanners.add(ColumnSpanner(columnIndex, rowIndex, span, placeable))
            }

            columnIndex += span
            if (columnIndex > columns.size) throw IllegalStateException("Too many columns")
            else if (columnIndex == columns.size) {
                columnIndex = 0
                rowIndex++
            }

            placeable
        }

        columnSpanners.forEach { spanner ->
            val span = spanner.span
            val columnWidth = columnWidths.drop(spanner.x).take(span).sum() + horizontalSpace * (span - 1)

            val excess = (spanner.placeable.width - columnWidth) / span
            if (excess > 0) {
                for (i in spanner.x until (spanner.x + span)) {
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
        val placeables = subcompose(SlotsEnum.DEPENDANT, content).mapIndexed { index, measurable ->
            val span = items[index].columnSpan

            val placeable = if (items[index].fillBox) {
                val columnWidth =
                        if (span == 1) {
                            columnWidths[columnIndex]
                        }
                        else {
                            columnWidths.drop(columnIndex).take(span).sum() + horizontalSpace * (span - 1)
                        }
                val rowHeight = rowHeights[rowIndex]
                measurable.measure(constraints.copy(minWidth = columnWidth, minHeight = rowHeight))
            }
            else {
                originalPlaceables[index]
            }

            columnIndex += span
            if (columnIndex == columns.size) {
                columnIndex = 0
                rowIndex++
            }

            placeable
        }

        columnIndex = 0
        rowIndex = 0
        layout(
                columnWidths.sum() + horizontalSpace * (columnWidths.size - 1),
                rowHeights.sum() + verticalSpace * (rowHeights.size - 1),
        ) {
            var x = 0
            var y = 0

            placeables.forEachIndexed { index, placeable ->
                val span = items[index].columnSpan
                val columnWidth = columnWidths.drop(columnIndex).take(span).sum() + horizontalSpace * (span - 1)
                val rowHeight = rowHeights[rowIndex]

                val offset = alignment.align(
                        IntSize(placeable.width, placeable.height),
                        IntSize(columnWidth, rowHeight),
                        layoutDirection,
                )

                placeable.place(
                        x = x + offset.x,
                        y = y + offset.y,
                )
                if (columnIndex < columns.size - span) {
                    x += columnWidth + horizontalSpace
                }
                else {
                    x = 0
                    y += rowHeight + verticalSpace
                }

                columnIndex += items[index].columnSpan
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
