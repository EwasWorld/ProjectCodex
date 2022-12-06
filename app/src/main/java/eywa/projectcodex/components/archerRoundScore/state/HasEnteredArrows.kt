package eywa.projectcodex.components.archerRoundScore.state

import eywa.projectcodex.common.archeryObjects.Arrow

interface HasEnteredArrows {
    fun getEnteredArrows(): List<Arrow>
}