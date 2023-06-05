package eywa.projectcodex.components.archerRoundScore

import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.addIdenticalArrows
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.addRound
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.newFullArcherRoundInfo
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundStatePreviewHelper
import junit.framework.TestCase.fail
import org.junit.Assert.assertEquals
import org.junit.Test

class ArcherRoundStateUnitTest {
    @Test
    fun testStartingScreen() {
        ArcherRoundState.Loading(ArcherRoundScreen.INPUT_END)

        try {
            ArcherRoundState.Loading(ArcherRoundScreen.INSERT_END)
            fail("Should not be able to start on a sub-screen")
        }
        catch (e: IllegalArgumentException) {
            // Correctly rejected
        }
    }

    @Test
    fun testScorePadSelectedEndFirstArrowNumber() {
        try {
            ArcherRoundStatePreviewHelper.SIMPLE.scorePadSelectedEndFirstArrowNumber
            fail("No selected end")
        }
        catch (e: NullPointerException) {
            // Correctly rejected
        }

        fun test(state: ArcherRoundState.Loaded, expectedFirstArrow: Int, expectedSize: Int) {
            assertEquals(expectedFirstArrow, state.scorePadSelectedEndFirstArrowNumber)
            assertEquals(expectedSize, state.scorePadSelectedEndSize)
        }

        // Full ends
        test(
                ArcherRoundStatePreviewHelper.SIMPLE
                        .copy(
                                scorePadSelectedEnd = 3,
                                scorePadEndSizePartial = 3,
                                fullArcherRoundInfo = newFullArcherRoundInfo()
                                        .addIdenticalArrows(60, 7)
                                        .addRound(RoundPreviewHelper.indoorMetricRoundData),
                        ),
                7,
                3,
        )

        // Partial ends
        test(
                ArcherRoundStatePreviewHelper.SIMPLE
                        .copy(
                                scorePadSelectedEnd = 8,
                                scorePadEndSizePartial = 5,
                                fullArcherRoundInfo = newFullArcherRoundInfo()
                                        .addIdenticalArrows(60, 7)
                                        .addRound(RoundPreviewHelper.outdoorImperialRoundData),
                        ),
                36,
                1,
        )

        test(
                ArcherRoundStatePreviewHelper.SIMPLE
                        .copy(
                                scorePadSelectedEnd = 13,
                                scorePadEndSizePartial = 5,
                                fullArcherRoundInfo = newFullArcherRoundInfo()
                                        .addIdenticalArrows(60, 7)
                                        .addRound(RoundPreviewHelper.outdoorImperialRoundData),
                        ),
                57,
                4,
        )
    }

    @Test
    fun testCurrentEndSize() {
        fun test(state: ArcherRoundState.Loaded, expectedEndSizes: Map<ArcherRoundScreen, Int>) {
            expectedEndSizes.forEach { (screen, expectedEndSize) ->
                assertEquals(
                        "$screen",
                        expectedEndSize,
                        state.copy(currentScreen = screen).currentScreenEndSize,
                )
            }
        }

        // No round
        test(
                ArcherRoundStatePreviewHelper.SIMPLE.copy(
                        scorePadSelectedEnd = 2,
                        scorePadEndSizePartial = 10,
                        inputEndSizePartial = 20,
                        fullArcherRoundInfo = ArcherRoundStatePreviewHelper.SIMPLE.fullArcherRoundInfo
                                .addIdenticalArrows(30, 7)
                ),
                mapOf(
                        ArcherRoundScreen.INPUT_END to 20,
                        ArcherRoundScreen.EDIT_END to 10,
                        ArcherRoundScreen.INSERT_END to 10,
                ),
        )

        // Single distance, many arrows left
        test(
                ArcherRoundStatePreviewHelper.SIMPLE.copy(
                        scorePadSelectedEnd = 2,
                        scorePadEndSizePartial = 10,
                        inputEndSizePartial = 20,
                        fullArcherRoundInfo = ArcherRoundStatePreviewHelper.SIMPLE.fullArcherRoundInfo
                                .addIdenticalArrows(30, 7)
                                .addRound(RoundPreviewHelper.indoorMetricRoundData),
                ),
                mapOf(
                        ArcherRoundScreen.INPUT_END to 20,
                        ArcherRoundScreen.EDIT_END to 10,
                        ArcherRoundScreen.INSERT_END to 10,
                ),
        )

        // Single distance, restricted arrows left
        test(
                ArcherRoundStatePreviewHelper.SIMPLE.copy(
                        scorePadSelectedEnd = 6,
                        scorePadEndSizePartial = 10,
                        inputEndSizePartial = 20,
                        fullArcherRoundInfo = ArcherRoundStatePreviewHelper.SIMPLE.fullArcherRoundInfo
                                .addIdenticalArrows(55, 7)
                                .addRound(RoundPreviewHelper.indoorMetricRoundData),
                ),
                mapOf(
                        ArcherRoundScreen.INPUT_END to 5,
                        ArcherRoundScreen.EDIT_END to 5,
                        ArcherRoundScreen.INSERT_END to 5,
                ),
        )

        // Two distances, restricted arrows left
        test(
                ArcherRoundStatePreviewHelper.SIMPLE.copy(
                        scorePadSelectedEnd = 4,
                        scorePadEndSizePartial = 10,
                        inputEndSizePartial = 20,
                        fullArcherRoundInfo = ArcherRoundStatePreviewHelper.SIMPLE.fullArcherRoundInfo
                                .addIdenticalArrows(51, 7)
                                .addRound(RoundPreviewHelper.outdoorImperialRoundData),
                ),
                mapOf(
                        ArcherRoundScreen.INPUT_END to 9,
                        ArcherRoundScreen.EDIT_END to 6,
                        ArcherRoundScreen.INSERT_END to 9,
                ),
        )

        // Two distances, restricted distance arrows left
        test(
                ArcherRoundStatePreviewHelper.SIMPLE.copy(
                        scorePadSelectedEnd = 3,
                        scorePadEndSizePartial = 10,
                        inputEndSizePartial = 20,
                        fullArcherRoundInfo = ArcherRoundStatePreviewHelper.SIMPLE.fullArcherRoundInfo
                                .addIdenticalArrows(30, 7)
                                .addRound(RoundPreviewHelper.outdoorImperialRoundData),
                ),
                mapOf(
                        ArcherRoundScreen.INPUT_END to 6,
                        ArcherRoundScreen.EDIT_END to 10,
                        ArcherRoundScreen.INSERT_END to 10,
                ),
        )
    }
}
