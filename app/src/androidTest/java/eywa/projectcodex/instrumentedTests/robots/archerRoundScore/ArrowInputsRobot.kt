package eywa.projectcodex.instrumentedTests.robots.archerRoundScore

import androidx.compose.ui.test.*
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.archerRoundScore.arrowInputs.ArrowInputsTestTag
import eywa.projectcodex.components.mainActivity.MainActivity

abstract class ArrowInputsRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
        screenTestTag: String? = null,
) : ArcherRoundRobot(composeTestRule, screenTestTag) {
    fun checkScoreButtonNotDisplayed(text: String) {
        composeTestRule.onNode(
                hasTestTag(ArrowInputsTestTag.ARROW_SCORE_BUTTON).and(hasText(text)),
                true
        ).assertDoesNotExist()
    }

    fun clickScoreButton(text: String) {
        composeTestRule.onNode(
                hasTestTag(ArrowInputsTestTag.ARROW_SCORE_BUTTON).and(hasText(text)),
                true
        ).performClick()
    }

    fun clickScoreButton(score: Int) {
        clickScoreButton(score.toString())
    }

    fun checkInputtedArrows(endSize: Int = 6) {
        checkInputtedArrows(emptyList<String>(), endSize)
    }

    @JvmName("checkInputtedArrowsInt")
    fun checkInputtedArrows(expectedArrows: List<Int>, endSize: Int = 6) {
        checkInputtedArrows(expectedArrows.map { it.toString() }, endSize)
    }

    fun checkInputtedArrows(expectedArrows: List<String>, endSize: Int = 6) {
        composeTestRule
                .onNodeWithTag(ArrowInputsTestTag.END_ARROWS_TEXT)
                .assertTextEquals(
                        expectedArrows.plus(List(endSize - expectedArrows.size) { "." }).joinToString("-")
                )
    }

    fun checkEndTotal(total: Int) {
        composeTestRule
                .onNodeWithTag(ArrowInputsTestTag.END_TOTAL_TEXT)
                .assertTextEquals(total.toString())
    }

    fun clickClear() {
        composeTestRule
                .onNodeWithTag(ArrowInputsTestTag.CLEAR_BUTTON, true)
                .performClick()
    }

    fun clickBackspace() {
        composeTestRule
                .onNodeWithTag(ArrowInputsTestTag.BACKSPACE_BUTTON, true)
                .performClick()
    }

    protected fun clickArrowInputsSubmit() {
        composeTestRule
                .onNodeWithTag(ArrowInputsTestTag.SUBMIT_BUTTON, true)
                .performClick()
    }

    protected fun clickArrowInputsCancel() {
        composeTestRule
                .onNodeWithTag(ArrowInputsTestTag.CANCEL_BUTTON, true)
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
        clickArrowInputsSubmit()
    }
}
