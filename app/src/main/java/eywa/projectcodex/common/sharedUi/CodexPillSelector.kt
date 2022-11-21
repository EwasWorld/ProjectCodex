package eywa.projectcodex.common.sharedUi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.helperInterfaces.NamedItem

// TODO Remove this if you're not using it?
@Composable
fun <T : NamedItem> CodexPillSelector(
        items: Iterable<T>,
        selectedItem: T,
        itemClickedListener: (T) -> Unit,
        modifier: Modifier = Modifier,
) {
    val borderStroke = 1.dp

    Surface(
            shape = RoundedCornerShape(100),
            border = BorderStroke(borderStroke, Color.LightGray),
            modifier = modifier.height(IntrinsicSize.Min)
    ) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedItem == item
                val textColour: Color
                val backgroundColour: Color
                if (isSelected) {
                    backgroundColour = CodexTheme.colors.pillSelectorSelected
                    textColour = CodexTheme.colors.onPillSelectorSelected
                }
                else {
                    backgroundColour = CodexTheme.colors.pillSelectorNotSelected
                    textColour = CodexTheme.colors.onPillSelectorNotSelected
                }

                Text(
                        text = item.label,
                        style = CodexTypography.NORMAL.copy(
                                color = textColour,
                                fontWeight = FontWeight.Bold,
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                                .selectable(isSelected, role = Role.Tab) { itemClickedListener(item) }
                                .weight(1f)
                                .background(backgroundColour)
                                .fillMaxHeight()
                                .padding(10.dp)
                )
                if (index != items.count() - 1) {
                    Divider(
                            color = CodexTheme.colors.pillSelectorBorder,
                            modifier = Modifier
                                    .fillMaxHeight()
                                    .width(borderStroke)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CodexPillSelector_Preview() {
    val items = listOf("Item 1", "Item 2")
            .map {
                object : NamedItem {
                    override val label = it
                }
            }
    CodexTheme {
        CodexPillSelector(
                items = items,
                selectedItem = items[0],
                itemClickedListener = {},
        )
    }
}
