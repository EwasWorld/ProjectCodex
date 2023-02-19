package eywa.projectcodex.components.viewScores

import eywa.projectcodex.components.viewScores.data.ViewScoresEntry

data class ViewScoresState(
        val isInMultiSelectMode: Boolean = false,
        val data: List<ViewScoresEntry> = listOf(),
        val multiSelectEmailClicked: Boolean = false,
        val multiSelectEmailNoSelection: Boolean = false,
)
