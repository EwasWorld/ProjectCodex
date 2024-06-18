package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher

abstract class ArrowInputsRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
        screenTestTag: CodexTestTag,
) : ShootDetailsRobot(composeTestRule, screenTestTag) {
    fun checkScoreButtonNotDisplayed(text: String) {
        performV2Single {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(ArrowInputsTestTag.ARROW_SCORE_BUTTON)
            +CodexNodeMatcher.HasText(text)
            +CodexNodeInteraction.AssertDoesNotExist()
        }
    }

    fun clickScoreButton(text: String) {
        performV2Single {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(ArrowInputsTestTag.ARROW_SCORE_BUTTON)
            +CodexNodeMatcher.HasContentDescription("$text arrow input")
            +CodexNodeInteraction.PerformClick()
        }
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
        performV2Single {
            +CodexNodeMatcher.HasTestTag(ArrowInputsTestTag.END_ARROWS_TEXT)
            +CodexNodeInteraction.AssertTextEquals(
                    expectedArrows.plus(List(endSize - expectedArrows.size) { "." }).joinToString("-")
            ).waitFor()
        }
    }

    fun checkEndTotal(total: Int) {
        checkElementText(ArrowInputsTestTag.END_TOTAL_TEXT, total.toString())
    }

    fun clickClear() {
        clickElement(ArrowInputsTestTag.CLEAR_BUTTON, true)
    }

    fun clickBackspace() {
        clickElement(ArrowInputsTestTag.BACKSPACE_BUTTON, true)
    }

    protected fun clickArrowInputsSubmit() {
        clickElement(ArrowInputsTestTag.SUBMIT_BUTTON, true)
    }

    protected fun clickArrowInputsCancel() {
        clickElement(ArrowInputsTestTag.CANCEL_BUTTON, true)
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
