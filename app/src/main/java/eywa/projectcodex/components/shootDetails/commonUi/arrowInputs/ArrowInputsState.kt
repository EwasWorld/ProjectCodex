package eywa.projectcodex.components.shootDetails.commonUi.arrowInputs

import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.model.Arrow

interface ArrowInputsState {
    val enteredArrows: List<Arrow>
    val round: Round?
    val face: RoundFace?
    val endSize: Int
}
