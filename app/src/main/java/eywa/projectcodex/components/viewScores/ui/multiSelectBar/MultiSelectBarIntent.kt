package eywa.projectcodex.components.viewScores.ui.multiSelectBar

sealed class MultiSelectBarIntent {
    /**
     * If all items are selected, deselect all items.
     * Otherwise, select all items.
     */
    object ClickAllOrNone : MultiSelectBarIntent()
    object ClickEmail : MultiSelectBarIntent()
    object ClickOpen : MultiSelectBarIntent()
    object ClickClose : MultiSelectBarIntent()
}
