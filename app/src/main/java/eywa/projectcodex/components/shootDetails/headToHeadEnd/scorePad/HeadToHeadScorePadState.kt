package eywa.projectcodex.components.shootDetails.headToHeadEnd.scorePad

import eywa.projectcodex.model.FullHeadToHeadHeat

sealed class HeadToHeadScorePadState {
    data object Loading : HeadToHeadScorePadState()
    data object Error : HeadToHeadScorePadState()
    data class Loaded(
            val entries: List<FullHeadToHeadHeat>,
    ) : HeadToHeadScorePadState()
}
