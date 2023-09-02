package eywa.projectcodex.common.sharedUi

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize

data class CodexGridItem(
//        val spanColumns: Int = 1,
        val fillBox: Boolean = false,
        val content: @Composable () -> Unit,
)

class CodexGridConfig {
    val items = mutableListOf<CodexGridItem>()

    fun item(
            fillBox: Boolean = false,
            content: @Composable () -> Unit,
    ) {
        items.add(CodexGridItem(fillBox, content))
    }
}

/**
 * Create a grid where each column is the width of the item with the largest width
 */
@Composable
fun CodexGrid(
        columns: Int,
        alignment: Alignment,
        modifier: Modifier = Modifier,
        config: CodexGridConfig.() -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    val items = CodexGridConfig().apply { config() }.items
    val content = @Composable { items.forEach { it.content() } }

    SubcomposeLayout(
            modifier = modifier,
    ) { constraints ->
        val columnWidths = MutableList(columns) { 0 }
        val rowHeights = mutableListOf<Int>()
        val originalPlaceables = subcompose(SlotsEnum.MAIN, content).mapIndexed { index, measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))

            val columnIndex = index % columns
            columnWidths[columnIndex] = maxOf(placeable.width, columnWidths[columnIndex])

            if (columnIndex == 0) {
                rowHeights.add(0)
            }
            rowHeights[rowHeights.lastIndex] = maxOf(placeable.height, rowHeights[rowHeights.lastIndex])

            placeable
        }

        val placeables = subcompose(SlotsEnum.DEPENDANT, content).mapIndexed { index, measurable ->
            if (items[index].fillBox) {
                val columnWidth = columnWidths[index % columns]
                val rowHeight = rowHeights[index.floorDiv(columns)]
                measurable.measure(constraints.copy(minWidth = columnWidth, minHeight = rowHeight))
            }
            else {
                originalPlaceables[index]
            }
        }

        layout(columnWidths.sum(), rowHeights.sum()) {
            var x = 0
            var y = 0

            placeables.forEachIndexed { index, placeable ->
                val columnIndex = index % columns
                val columnWidth = columnWidths[columnIndex]
                val rowHeight = rowHeights.first()

                val offset = alignment.align(
                        IntSize(placeable.width, placeable.height),
                        IntSize(columnWidth, rowHeight),
                        layoutDirection,
                )

                placeable.place(
                        x = x + offset.x,
                        y = y + offset.y,
                )
                if (columnIndex < columns - 1) {
                    x += columnWidth
                }
                else {
                    x = 0
                    y += rowHeight
                    rowHeights.removeFirst()
                }
            }
        }
    }
}

private enum class SlotsEnum { MAIN, DEPENDANT }
