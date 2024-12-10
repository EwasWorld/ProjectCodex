package eywa.projectcodex.components.shootDetails.headToHeadEnd.grid

import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult

data class HeadToHeadSetData(
        val teamEndTotal: Int,
        val opponentEndTotal: Int,
        val hasSelfAndTeamRows: Boolean,
        val teamTotalColumnSpan: Int,
        val resultColumnSpan: Int,
        val result: HeadToHeadResult,
        val isShootOff: Boolean,
)
