package eywa.projectcodex.components.shootDetails

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addRound
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.completeRound
import eywa.projectcodex.components.shootDetails.stats.DistanceExtra
import eywa.projectcodex.components.shootDetails.stats.GrandTotalExtra
import eywa.projectcodex.components.shootDetails.stats.StatsExtras
import eywa.projectcodex.components.shootDetails.stats.StatsState
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsStateUnitTest {
    @Test
    fun testExtras() {
        val round = RoundPreviewHelper.outdoorImperialRoundData
        val detailsState = ShootDetailsState(
                fullShootInfo = ShootPreviewHelper
                        .newFullShootInfo()
                        .addRound(round)
                        .completeRound(7)
        )
        val state = StatsState(main = detailsState, extras = StatsExtras())

        assertEquals(
                DistanceExtra(
                        round.roundDistances!![0],
                        round.roundArrowCounts!![0],
                        state.fullShootInfo.arrows!!.take(36),
                        6,
                        calculateHandicap = { _, _, _ -> 29.0 },
                ),
                state.extras!![0] as DistanceExtra,
        )
        assertEquals(
                DistanceExtra(
                        round.roundDistances!![1],
                        round.roundArrowCounts!![1],
                        state.fullShootInfo.arrows!!.take(24),
                        6,
                        calculateHandicap = { _, _, _ -> 37.0 },
                ),
                state.extras!![1] as DistanceExtra,
        )
        assertEquals(
                GrandTotalExtra(
                        state.fullShootInfo.arrows!!,
                        6,
                        handicap = 32.0,
                ),
                state.extras!![2] as GrandTotalExtra,
        )
    }
}
