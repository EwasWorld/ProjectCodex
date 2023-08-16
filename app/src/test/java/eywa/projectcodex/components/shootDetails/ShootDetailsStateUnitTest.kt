package eywa.projectcodex.components.shootDetails

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addIdenticalArrows
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addRound
import org.junit.Assert
import org.junit.Test

class ShootDetailsStateUnitTest {
    @Test
    fun testSelectedEndFirstArrowNumberAndEndSize() {
        fun test(state: ShootDetailsState, expectedFirstArrow: Int?, expectedSize: Int?) {
            Assert.assertEquals(expectedFirstArrow, state.firstArrowNumberInSelectedEnd)
            Assert.assertEquals(expectedSize, state.selectedEndSize)
        }

        val baseState = ShootDetailsState(
                fullShootInfo = ShootPreviewHelper.newFullShootInfo()
                        .addIdenticalArrows(60, 7)
                        .addRound(RoundPreviewHelper.outdoorImperialRoundData),
        )

        // No selected end
        test(
                baseState,
                null,
                null,
        )

        // Full ends
        test(
                baseState.copy(
                        scorePadSelectedEnd = 3,
                        scorePadEndSize = 3,
                ),
                7,
                3,
        )

        // Partial ends
        test(
                baseState.copy(
                        scorePadSelectedEnd = 8,
                        scorePadEndSize = 5,
                ),
                36,
                1,
        )

        test(
                baseState.copy(
                        scorePadSelectedEnd = 13,
                        scorePadEndSize = 5,
                ),
                expectedFirstArrow = 57,
                4,
        )
    }
}
