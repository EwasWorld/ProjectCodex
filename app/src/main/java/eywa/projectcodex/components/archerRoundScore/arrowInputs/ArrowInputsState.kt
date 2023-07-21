package eywa.projectcodex.components.archerRoundScore.arrowInputs

import eywa.projectcodex.components.archerRoundScore.state.HasEnteredArrows
import eywa.projectcodex.components.archerRoundScore.state.HasFullArcherRoundInfo
import eywa.projectcodex.components.archerRoundScore.state.HasInputEndSize

interface ArrowInputsState : HasFullArcherRoundInfo, HasInputEndSize, HasEnteredArrows

interface HasSelectedEndNumber {
    val selectedEndNumber: Int
}
