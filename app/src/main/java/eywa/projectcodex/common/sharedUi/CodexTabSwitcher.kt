package eywa.projectcodex.common.sharedUi

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.helperInterfaces.NamedItem
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : NamedItem> CodexTabSwitcher(
        items: Iterable<T>,
        selectedItem: T,
        itemClickedListener: (T) -> Unit,
        modifier: Modifier = Modifier,
        itemColor: Color = CodexTheme.colors.tabSwitcherSelected,
        dividerColor: Color = CodexTheme.colors.tabSwitcherDivider,
) {
    require(items.count() >= 2) { "Must have at least two items" }
    val selectedTabIndex = items.indexOfFirst { selectedItem == it }

    if (items.count() > 2) {
        PrimaryScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                divider = { Divider(color = dividerColor) },
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                            color = itemColor,
                            modifier = Modifier.tabIndicatorOffset(it[selectedTabIndex])
                    )
                },
                tabs = { Tabs(items, selectedItem, itemClickedListener, itemColor) },
                modifier = modifier
        )
    }
    else {
        PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                divider = { Divider(color = dividerColor) },
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                            color = itemColor,
                            modifier = Modifier.tabIndicatorOffset(selectedTabIndex)
                    )
                },
                tabs = { Tabs(items, selectedItem, itemClickedListener, itemColor) },
                modifier = modifier
        )
    }
}

@Composable
private fun <T : NamedItem> Tabs(
        items: Iterable<T>,
        selectedItem: T,
        itemClickedListener: (T) -> Unit,
        itemColor: Color = CodexTheme.colors.tabSwitcherSelected,
) {
    val selectedTabIndex = items.indexOfFirst { selectedItem == it }

    items.forEachIndexed { index, item ->
        Tab(
                selected = index == selectedTabIndex,
                onClick = { itemClickedListener(item) },
                text = {
                    Text(
                            text = item.label.get(),
                            color = itemColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                    )
                },
                modifier = Modifier.testTag(TabSwitcherTestTag.ITEM)
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
    val items = List(2) { "Item $it" }
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
                modifier = Modifier.padding(bottom = 50.dp)
        )
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Scrollable_TabSwitcher_Preview() {
    val items = List(10) { "Item $it" }
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
                modifier = Modifier.padding(bottom = 50.dp)
        )
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Overflow_TabSwitcher_Preview() {
    val items = List(2) { "Item $it which is very long" }
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
                modifier = Modifier.padding(bottom = 50.dp)
        )
    }
}
