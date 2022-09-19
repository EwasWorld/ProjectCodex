package eywa.projectcodex.components.viewScores

import eywa.projectcodex.components.viewScores.data.ViewScoresEntry

data class ViewScoresState(
        val isInMultiSelectMode: Boolean = false,
        val openContextMenuEntryIndex: Int? = null,
        val convertDialogSelectedIndex: Int? = null,
        val data: List<ViewScoresEntry> = listOf(),
)