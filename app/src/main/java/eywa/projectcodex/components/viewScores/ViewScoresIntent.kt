package eywa.projectcodex.components.viewScores

sealed class ViewScoresIntent {
    data class OpenContextMenu(val entryIndex: Int) : ViewScoresIntent()
    object CloseContextMenu : ViewScoresIntent()

    data class SetMultiSelectMode(val isInMultiSelectMode: Boolean) : ViewScoresIntent()

    data class ToggleEntrySelected(val entryIndex: Int) : ViewScoresIntent()

    /**
     * @param forceIsSelectedTo If non-null, forces all item's isSelected to be this value.
     *      Otherwise, if all items are selected, deselect all items.
     *      Otherwise, select all items
     */
    data class SelectAllOrNone(val forceIsSelectedTo: Boolean? = null) : ViewScoresIntent()
    object OpenConvertMenu : ViewScoresIntent()
    object CloseConvertAndContextMenu : ViewScoresIntent()
    data class UpdateConvertMenuSelectedIndex(val selectedIndex: Int) : ViewScoresIntent()
}