package eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowData
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowDataPreviewHelper
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridState
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeat
import eywa.projectcodex.model.SightMark

data class HeadToHeadAddEndState(
        val round: Round? = RoundPreviewHelper.wa70RoundData.round,
        val distance: Int? = 70,
        val isMetric: Boolean? = true,
        val sightMark: SightMark? = null,
        val face: RoundFace? = RoundFace.FULL,
        val teamSize: Int = 2,
        val enteredArrows: List<HeadToHeadGridRowData> = HeadToHeadGridRowDataPreviewHelper.selfAndOneTeamMateWithOpponent,
        val endSize: Int = 3,
        val selected: HeadToHeadArcherType? = enteredArrows.map { it.type }.minByOrNull { it.ordinal },
        val teamScore: Int = 0,
        val opponentScore: Int = 2,
        val isSetPoints: Boolean = true,
        val heat: DatabaseHeadToHeadHeat? = null,
        val setNumber: Int = 0,
) {
    val result: HeadToHeadResult
        get() {
            val isComplete = enteredArrows.all { it.isComplete(endSize, teamSize) }
            if (!isComplete) return HeadToHeadResult.INCOMPLETE
            val teamEndTotal = enteredArrows.filter { it.type.isTeam }.sumOf { it.totalScore() }
            val opponentEndTotal =
                    enteredArrows.filter { it.type == HeadToHeadArcherType.OPPONENT_ARROW }.sumOf { it.totalScore() }
            return when {
                teamEndTotal == opponentEndTotal -> HeadToHeadResult.TIE
                teamEndTotal > opponentEndTotal -> HeadToHeadResult.WIN
                else -> HeadToHeadResult.LOSS
            }
        }

    fun toGridState() = heat?.let {
        HeadToHeadGridState(
                enteredArrows = listOf(enteredArrows),
                endSize = endSize,
                teamSize = teamSize,
                selected = selected,
                isSingleEditableSet = true,
                hasShootOff = it.hasShootOff,
                isShootOffWin = it.isShootOffWin,
        )
    }
}

