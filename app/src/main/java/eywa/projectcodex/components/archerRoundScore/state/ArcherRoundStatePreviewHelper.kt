package eywa.projectcodex.components.archerRoundScore.state

import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.addIdenticalArrows
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.addRound
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.GoldsType

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
                    .addIdenticalArrows(20, 7)
                    .addRound(RoundPreviewHelper.outdoorImperialRoundData)
    )
}
