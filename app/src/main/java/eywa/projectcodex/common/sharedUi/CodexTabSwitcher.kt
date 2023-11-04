package eywa.projectcodex.common.sharedUi

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
) {
    require(items.count() >= 2) { "Must have at least two items" }
    val selectedTabIndex = items.indexOfFirst { selectedItem == it }

    // TODO When material3 v1.2.0 becomes stable, this should be changed to PrimaryTabRow
    TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            divider = { Divider(color = CodexTheme.colors.tabSwitcherDivider) },
            indicator = {
                TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(it[selectedTabIndex]),
                        color = CodexTheme.colors.tabSwitcherSelected,
                )
            },
            modifier = modifier
    ) {
        items.forEachIndexed { index, item ->
            Tab(
                    selected = index == selectedTabIndex,
                    onClick = { itemClickedListener(item) },
                    text = {
                        Text(
                                text = item.label.get(),
                                color = CodexTheme.colors.tabSwitcherSelected,
                        )
                    },
                    modifier = Modifier.testTag(TabSwitcherTestTag.ITEM)
            )
        }
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
                modifier = Modifier.padding(bottom = 100.dp)
        )
    }
}
