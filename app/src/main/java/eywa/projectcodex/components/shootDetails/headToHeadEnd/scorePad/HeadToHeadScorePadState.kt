package eywa.projectcodex.components.shootDetails.headToHeadEnd.scorePad

import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowData
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridState

data class HeadToHeadScorePadState(
        val endSize: Int,
        val teamSize: Int,
        val entries: List<HeadToHeadScorePadHeatState>,
)

data class HeadToHeadScorePadHeatState(
        val enteredArrows: List<List<HeadToHeadGridRowData>>,
        val hasShootOff: Boolean,
        val isShootOffWin: Boolean,
        val heat: Int,
        val opponent: String,
        val opponentRank: Int,
) {
    fun toGridState(endSize: Int, teamSize: Int) = HeadToHeadGridState(
            enteredArrows = enteredArrows,
            endSize = endSize,
            teamSize = teamSize,
            selected = null,
            isSingleEditableSet = false,
            hasShootOff = hasShootOff,
            isShootOffWin = isShootOffWin,
    )
}
