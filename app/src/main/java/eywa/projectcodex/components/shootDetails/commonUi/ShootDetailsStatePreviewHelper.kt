package eywa.projectcodex.components.shootDetails.commonUi

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.components.shootDetails.ShootDetailsState

object ShootDetailsStatePreviewHelper {
    val SIMPLE = ShootDetailsState(
            fullShootInfo = ShootPreviewHelperDsl.create {},
            shootId = 1,
    )

    val WITH_SHOT_ARROWS = ShootDetailsState(
            fullShootInfo = ShootPreviewHelperDsl.create {
                round = RoundPreviewHelper.outdoorImperialRoundData
                addIdenticalArrows(20, 7)
            },
            shootId = 1,
    )
}
