package eywa.projectcodex.components.archerRoundScore

import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.components.newScore.NewScoreStatePreviewProvider
import eywa.projectcodex.database.archerRound.FullArcherRoundInfo
import eywa.projectcodex.database.arrowValue.ArrowValue

object ArcherRoundPreviewHelper {
    val provider = NewScoreStatePreviewProvider()
    val round = provider.outdoorImperialRoundData

    val SIMPLE = ArcherRoundState(
            fullArcherRoundInfo = FullArcherRoundInfo(
                    archerRound = provider.editingArcherRound,
                    arrows = List(50) { ArrowValue(1, it, 7, false) },
                    round = round.round,
                    roundArrowCounts = round.roundArrowCounts,
                    allRoundSubTypes = round.roundSubTypes,
                    allRoundDistances = round.roundDistances,
            ),
            goldsType = GoldsType.NINES,
            inputArrows = listOf(
                    Arrow(10, true),
                    Arrow(10, false),
                    Arrow(3, false),
                    Arrow(0, false),
            ),
    )

    val FEW_ARROWS = SIMPLE.copy(
            fullArcherRoundInfo = SIMPLE.fullArcherRoundInfo.copy(
                    arrows = List(6) { ArrowValue(1, it, 7, false) },
            )
    )
}