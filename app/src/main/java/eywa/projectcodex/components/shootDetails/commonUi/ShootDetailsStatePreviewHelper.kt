package eywa.projectcodex.components.shootDetails.commonUi

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addIdenticalArrows
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addRound
import eywa.projectcodex.components.shootDetails.ShootDetailsState

object ShootDetailsStatePreviewHelper {
    val SIMPLE = ShootDetailsState(fullShootInfo = ShootPreviewHelper.newFullShootInfo())

    val WITH_SHOT_ARROWS = ShootDetailsState(
            fullShootInfo = ShootPreviewHelper
                    .newFullShootInfo()
                    .addIdenticalArrows(20, 7)
                    .addRound(RoundPreviewHelper.outdoorImperialRoundData)
    )
}
