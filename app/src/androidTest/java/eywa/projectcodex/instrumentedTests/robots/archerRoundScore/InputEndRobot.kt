package eywa.projectcodex.instrumentedTests.robots.archerRoundScore

import androidx.compose.ui.test.*
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.components.archerRoundScore.arrowInputs.ArrowInputsTestTag
import eywa.projectcodex.components.archerRoundScore.arrowInputs.inputEnd.InputEndScreen.TestTag
import eywa.projectcodex.components.mainActivity.MainActivity

class InputEndRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ArrowInputsRobot(composeTestRule) {
    fun waitForLoad() {
        CustomConditionWaiter.waitForComposeCondition {
            composeTestRule
                    .onNodeWithTag(ArrowInputsTestTag.CLEAR_BUTTON, true)
                    .assertIsDisplayed()
        }
    }

    fun waitForRemainingArrows() {
        CustomConditionWaiter.waitForComposeCondition {
            composeTestRule
                    .onNodeWithTag(TestTag.REMAINING_ARROWS_CURRENT, true)
                    .assertIsDisplayed()
        }
    }

    fun checkIndicatorTable(score: Int, arrowCount: Int) {
        composeTestRule
                .onNodeWithTag(TestTag.ROUND_SCORE)
                .assertTextEquals(score.toString())
        composeTestRule
                .onNodeWithTag(TestTag.ROUND_ARROWS)
                .assertTextEquals(arrowCount.toString())
    }

    fun checkRemainingArrows(currentDistance: String, laterDistances: String) {
        composeTestRule
                .onNodeWithTag(TestTag.REMAINING_ARROWS_CURRENT)
                .assertTextEquals(currentDistance)

        val laterNode = composeTestRule.onNodeWithTag(TestTag.REMAINING_ARROWS_LATER)
        if (laterDistances.isNotBlank()) {
            laterNode.assertTextEquals(laterDistances)
        }
        else {
            laterNode.assertDoesNotExist()
        }
    }

    fun clickNextEnd() {
        composeTestRule
                .onNodeWithTag(ArrowInputsTestTag.SUBMIT_BUTTON, true)
                .performClick()
    }

    /**
     * Fills the end by pressing one of the score buttons then pressing complete
     */
    fun completeEnd(
            scoreButtonText: String,
            count: Int = 6,
    ) {
        repeat(count) {
            clickScoreButton(scoreButtonText)
        }
        clickNextEnd()
    }

    fun clickRoundCompleteOk(block: ArcherRoundStatsRobot.() -> Unit) {
        CustomConditionWaiter.waitForComposeCondition("Waiting for round complete dialog to display") {
            composeTestRule
                    .onNode(
                            hasTestTag(SimpleDialogTestTag.TITLE).and(hasText(ROUND_COMPLETE_DIALOG_TITLE))
                    )
                    .assertIsDisplayed()
        }
        composeTestRule
                .onNodeWithTag(SimpleDialogTestTag.POSITIVE_BUTTON)
                .performClick()
        ArcherRoundStatsRobot(composeTestRule).apply(block)
    }

    fun clickCannotInputMoreEndsOk() {
        CustomConditionWaiter.waitForComposeCondition("Waiting for cannot input dialog to display") {
            composeTestRule
                    .onNode(
                            hasTestTag(SimpleDialogTestTag.TITLE).and(hasText(CANNOT_INPUT_END_DIALOG_TITLE))
                    )
                    .assertIsDisplayed()
        }
        composeTestRule
                .onNodeWithTag(SimpleDialogTestTag.POSITIVE_BUTTON)
                .performClick()
    }

    companion object {
        private const val ROUND_COMPLETE_DIALOG_TITLE = "Round Complete"
        private const val CANNOT_INPUT_END_DIALOG_TITLE = "Round is complete"
    }
}
