package eywa.projectcodex.components.archerRoundScore.state

import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.addArrows
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.addRound
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper

object ArcherRoundStatePreviewHelper {
    val inputArrows = listOf(
            Arrow(10, true),
            Arrow(10, false),
            Arrow(3, false),
            Arrow(0, false),
    )

    val SIMPLE = ArcherRoundState.Loaded(
            currentScreen = ArcherRoundScreen.INPUT_END,
            fullArcherRoundInfo = ArcherRoundPreviewHelper.newFullArcherRoundInfo(),
            goldsType = GoldsType.NINES,
            inputArrows = inputArrows,
    )

    val WITH_SHOT_ARROWS = SIMPLE.copy(
            fullArcherRoundInfo = SIMPLE.fullArcherRoundInfo
                    .addArrows(20, 7)
                    .addRound(RoundPreviewHelper.indoorMetricRoundData)
    )
}
