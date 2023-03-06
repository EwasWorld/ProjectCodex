package eywa.projectcodex.components.archerRoundScore

import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.addRound
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState
import eywa.projectcodex.database.arrowValue.ArrowValue

object ArcherRoundsPreviewHelper {
    val round = RoundPreviewHelper.outdoorImperialRoundData

    val inputArrows = listOf(
            Arrow(10, true),
            Arrow(10, false),
            Arrow(3, false),
            Arrow(0, false),
    )

    val SIMPLE = ArcherRoundState.Loaded(
            currentScreen = eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen.INPUT_END,
            fullArcherRoundInfo = ArcherRoundPreviewHelper.fullArcherRoundInfo,
            goldsType = GoldsType.NINES,
            inputArrows = inputArrows,
    )

    val WITH_SHOT_ARROWS = SIMPLE.copy(
            fullArcherRoundInfo = SIMPLE.fullArcherRoundInfo.copy(
                    arrows = List(20) { ArrowValue(1, it, 7, false) },
            ).addRound(RoundPreviewHelper.indoorMetricRoundData)
    )
}
