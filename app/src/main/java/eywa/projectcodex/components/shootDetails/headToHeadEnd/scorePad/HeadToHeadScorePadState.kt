package eywa.projectcodex.components.shootDetails.headToHeadEnd.scorePad

import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridState
import eywa.projectcodex.model.FullHeadToHeadSet

data class HeadToHeadScorePadState(
        val endSize: Int,
        val teamSize: Int,
        val entries: List<HeadToHeadScorePadHeatState>,
)

data class HeadToHeadScorePadHeatState(
        val enteredArrows: List<FullHeadToHeadSet>,
        val heat: Int,
        val opponent: String,
        val opponentRank: Int,
) {
    fun toGridState() = HeadToHeadGridState(
            enteredArrows = enteredArrows,
            selected = null,
            isSingleEditableSet = false,
    )
}
