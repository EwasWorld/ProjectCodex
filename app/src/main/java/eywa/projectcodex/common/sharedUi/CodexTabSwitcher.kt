package eywa.projectcodex.common.sharedUi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.helperInterfaces.NamedItem
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual

@Composable
fun <T : NamedItem> CodexTabSwitcher(
        items: Iterable<T>,
        selectedItem: T,
        itemClickedListener: (T) -> Unit,
        modifier: Modifier = Modifier,
        paddingValues: PaddingValues = PaddingValues(start = 12.dp, end = 12.dp, top = 20.dp),
) {
    require(items.count() >= 2) { "Must have at least two items" }

    val cornerRoundPercent = 30
    val borderStroke = 2.dp

    Box(
            modifier = modifier
    ) {
        Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(cornerRoundPercent, cornerRoundPercent, 0, 0),
                border = BorderStroke(borderStroke, CodexTheme.colors.tabSwitcherBorder),
                modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .padding(paddingValues)
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
                        backgroundColour = CodexTheme.colors.tabSwitcherSelected
                        textColour = CodexTheme.colors.onTabSwitcherSelected
                    }
                    else {
                        backgroundColour = CodexTheme.colors.tabSwitcherNotSelected
                        textColour = CodexTheme.colors.onTabSwitcherNotSelected
                    }

                    Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                    .selectable(isSelected, role = Role.Tab) { itemClickedListener(item) }
                                    .weight(1f)
                                    .background(backgroundColour)
                                    .fillMaxHeight()
                                    .padding(10.dp)
                                    .testTag(TabSwitcherTestTag.ITEM.getTestTag())
                    ) {
                        Text(
                                text = item.label.get(),
                                color = textColour,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (index != items.count() - 1) {
                        Divider(
                                color = CodexTheme.colors.tabSwitcherBorder,
                                modifier = Modifier
                                        .fillMaxHeight()
                                        .width(borderStroke)
                        )
                    }
                }
            }
        }

        Divider(
                color = CodexTheme.colors.tabSwitcherBorder,
                modifier = Modifier
                        .fillMaxWidth()
                        .width(borderStroke)
                        .align(Alignment.BottomCenter)
        )
    }
}

enum class TabSwitcherTestTag : CodexTestTag {
    ITEM,
    ;

    override val screenName: String
        get() = "TAB_SWITCHER"

    override fun getElement(): String = name
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun TabSwitcher_Preview() {
    val items = listOf("Item 1", "Item 2")
            .map {
                object : NamedItem {
                    override val label = ResOrActual.Actual(it)
                }
            }
    CodexTheme {
        CodexTabSwitcher(
                items = items,
                selectedItem = items[0],
                itemClickedListener = {},
        )
    }
}
