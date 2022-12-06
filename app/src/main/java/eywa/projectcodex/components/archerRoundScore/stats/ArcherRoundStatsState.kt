package eywa.projectcodex.components.archerRoundScore.stats

import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.components.archerRoundScore.state.HasFullArcherRoundInfo

interface ArcherRoundStatsState : HasFullArcherRoundInfo {
    val goldsType: GoldsType
}