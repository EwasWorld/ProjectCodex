package eywa.projectcodex.components.archerRoundScore.arrowInputs

import eywa.projectcodex.components.archerRoundScore.state.HasEnteredArrows
import eywa.projectcodex.components.archerRoundScore.state.HasInputEndSize
import eywa.projectcodex.components.archerRoundScore.state.HasRound

interface ArrowInputsState : HasRound, HasInputEndSize, HasEnteredArrows

interface HasSelectedEndNumber {
    val selectedEndNumber: Int
}
