package eywa.projectcodex.components.mainActivity

import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseItem

data class MainActivityState(
        internal val helpItems: List<ComposeHelpShowcaseItem>? = null,
        internal val currentHelpItemIndex: Int? = null,
) {
    val isHelpShowcaseInProgress: Boolean
        get() = !helpItems.isNullOrEmpty() && currentHelpItemIndex in helpItems.indices
    val currentHelpItem: ComposeHelpShowcaseItem?
        get() = currentHelpItemIndex?.let { helpItems?.get(it) }
    val hasNextItem: Boolean
        get() = currentHelpItemIndex != null && currentHelpItemIndex != helpItems?.lastIndex

    fun nextHelpItem(): MainActivityState {
        if (!hasNextItem) return clearHelpItems()
        return copy(currentHelpItemIndex = currentHelpItemIndex?.plus(1))
    }

    fun clearHelpItems(): MainActivityState {
        ActionBarHelp.markShowcaseComplete()
        return copy(helpItems = null, currentHelpItemIndex = null)
    }
}