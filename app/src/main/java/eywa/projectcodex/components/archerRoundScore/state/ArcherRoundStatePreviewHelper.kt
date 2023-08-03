package eywa.projectcodex.components.archerRoundScore.state

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addIdenticalArrows
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addRound
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
            fullShootInfo = ShootPreviewHelper.newFullShootInfo(),
            goldsType = GoldsType.NINES,
            inputArrows = inputArrows,
    )

    val WITH_SHOT_ARROWS = SIMPLE.copy(
            fullShootInfo = SIMPLE.fullShootInfo
                    .addIdenticalArrows(20, 7)
                    .addRound(RoundPreviewHelper.outdoorImperialRoundData)
    )
}
