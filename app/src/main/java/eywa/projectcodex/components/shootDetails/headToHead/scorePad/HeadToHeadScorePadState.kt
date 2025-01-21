package eywa.projectcodex.components.shootDetails.headToHead.scorePad

import eywa.projectcodex.model.headToHead.FullHeadToHeadMatch

data class HeadToHeadScorePadState(
        val entries: List<FullHeadToHeadMatch>,
        val extras: HeadToHeadScorePadExtras = HeadToHeadScorePadExtras(),
)

data class HeadToHeadScorePadExtras(
        val openAddMatch: Boolean = false,
        val openSightersForMatch: Int? = null,
        val openEditMatchInfo: Int? = null,
        val openEditSetInfo: Pair<Int, Int>? = null,
)
