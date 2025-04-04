package eywa.projectcodex.components.shootDetails

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.model.user.CodexUserPreviewHelper
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
                shootId = 1,
                fullShootInfo = ShootPreviewHelperDsl.create {
                    addIdenticalArrows(60, 7)
                    round = RoundPreviewHelper.outdoorImperialRoundData
                },
                user = CodexUserPreviewHelper.allCapabilities,
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
