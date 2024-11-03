package eywa.projectcodex.components.shootDetails.headToHeadEnd.grid

import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult

data class HeadToHeadSetData(
        val endSize: Int,
        val teamSize: Int,
        val teamEndTotal: Int,
        val opponentEndTotal: Int,
        val hasSelfAndTeamRows: Boolean,
        val result: HeadToHeadResult,
        val isShootOff: Boolean,
)
