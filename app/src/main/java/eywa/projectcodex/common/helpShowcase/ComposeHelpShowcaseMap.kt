package eywa.projectcodex.common.helpShowcase

import androidx.annotation.StringRes
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned

fun Modifier.updateHelpDialogPosition(helpItemsMap: ComposeHelpShowcaseMap, @StringRes key: Int) =
        onGloballyPositioned { helpItemsMap.updateItem(key, it) }

fun Modifier.updateHelpDialogPosition(listener: HelpShowcaseListener, @StringRes title: Int) =
        onGloballyPositioned { listener.updateHelpDialogPosition(title, it) }

class ComposeHelpShowcaseMap {
    /**
     * Map<titleStringId, ShowcaseItem>
     */
    private val helpInfoMap = mutableMapOf<Int, ComposeHelpShowcaseItem>()

    fun add(item: ComposeHelpShowcaseItem) {
        helpInfoMap[item.helpTitle] = item
    }

    fun updateItem(@StringRes key: Int, layoutCoordinates: LayoutCoordinates) =
            helpInfoMap[key]!!.updateLayoutCoordinates(layoutCoordinates)

    fun getItems() = helpInfoMap.values.toList()

    fun clear() = helpInfoMap.clear()
}

interface HelpShowcaseListener {
    fun addHelpShowcase(item: ComposeHelpShowcaseItem)
    fun updateHelpDialogPosition(@StringRes helpTitle: Int, layoutCoordinates: LayoutCoordinates)
}
