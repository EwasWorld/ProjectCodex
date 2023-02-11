package eywa.projectcodex.common.helpShowcase

import androidx.annotation.StringRes
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import eywa.projectcodex.common.utils.ResOrActual

fun Modifier.updateHelpDialogPosition(helpItemsMap: ComposeHelpShowcaseMap, @StringRes key: Int) =
        onGloballyPositioned { helpItemsMap.updateItem(key, it) }

fun Modifier.updateHelpDialogPosition(helpItemsMap: ComposeHelpShowcaseMap, key: String) =
        onGloballyPositioned { helpItemsMap.updateItem(key, it) }

fun Modifier.updateHelpDialogPosition(listener: HelpShowcaseListener, @StringRes title: Int) =
        onGloballyPositioned { listener.updateHelpDialogPosition(title, it) }

class ComposeHelpShowcaseMap {
    /**
     * Map<titleStringId, ShowcaseItem>
     */
    private val helpInfoMap = mutableMapOf<ResOrActual<String>, ComposeHelpShowcaseItem>()

    fun add(item: ComposeHelpShowcaseItem) {
        helpInfoMap[item.helpTitle] = item
    }

    fun remove(key: ResOrActual<String>) {
        helpInfoMap.remove(key)
    }

    fun remove(@StringRes key: Int) {
        helpInfoMap.remove(ResOrActual.fromRes(key))
    }

    fun remove(key: String) {
        helpInfoMap.remove(ResOrActual.fromActual(key))
    }

    fun updateItem(@StringRes key: Int, layoutCoordinates: LayoutCoordinates) =
            updateItem(ResOrActual.fromRes(key), layoutCoordinates)

    fun updateItem(key: String, layoutCoordinates: LayoutCoordinates) =
            updateItem(ResOrActual.fromActual(key), layoutCoordinates)

    fun updateItem(key: ResOrActual<String>, layoutCoordinates: LayoutCoordinates) =
            helpInfoMap[key]!!.updateLayoutCoordinates(layoutCoordinates)

    fun getItems() = helpInfoMap.values.toList()

    fun clear() = helpInfoMap.clear()
}

interface HelpShowcaseListener {
    fun addHelpShowcase(item: ComposeHelpShowcaseItem)
    fun updateHelpDialogPosition(@StringRes helpTitle: Int, layoutCoordinates: LayoutCoordinates)
}
