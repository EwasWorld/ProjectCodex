package eywa.projectcodex.components.archerRoundScore.arrowInputs

import eywa.projectcodex.components.archerRoundScore.state.HasEndSize
import eywa.projectcodex.components.archerRoundScore.state.HasEnteredArrows
import eywa.projectcodex.components.archerRoundScore.state.HasRound

interface ArrowInputsState : HasRound, HasEndSize, HasEnteredArrows

interface HasSelectedEndNumber {
    fun getSelectedEndNumber(): Int
}