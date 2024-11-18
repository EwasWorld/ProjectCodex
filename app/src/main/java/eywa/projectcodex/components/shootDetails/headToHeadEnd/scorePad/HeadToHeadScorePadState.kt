package eywa.projectcodex.components.shootDetails.headToHeadEnd.scorePad

import eywa.projectcodex.model.headToHead.FullHeadToHeadHeat

data class HeadToHeadScorePadState(
        val entries: List<FullHeadToHeadHeat>,
        val extras: HeadToHeadScorePadExtras = HeadToHeadScorePadExtras(),
)

data class HeadToHeadScorePadExtras(
        val openAddHeat: Boolean = false,
        val openSightersForHeat: Int? = null,
        val openEditHeatInfo: Int? = null,
)
