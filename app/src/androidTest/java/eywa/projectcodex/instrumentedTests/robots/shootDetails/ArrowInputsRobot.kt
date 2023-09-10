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
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(ArrowInputsTestTag.ARROW_SCORE_BUTTON)
            +CodexNodeMatcher.HasText(text)
            +CodexNodeInteraction.AssertDoesNotExist
        }
    }

    fun clickScoreButton(text: String) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(ArrowInputsTestTag.ARROW_SCORE_BUTTON)
            +CodexNodeMatcher.HasContentDescription("$text arrow input")
            +CodexNodeInteraction.PerformClick
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
        perform {
            +CodexNodeMatcher.HasTestTag(ArrowInputsTestTag.END_ARROWS_TEXT)
            +CodexNodeInteraction.AssertTextEquals(
                    expectedArrows.plus(List(endSize - expectedArrows.size) { "." }).joinToString("-")
            ).waitFor()
        }
    }

    fun checkEndTotal(total: Int) {
        perform {
            +CodexNodeMatcher.HasTestTag(ArrowInputsTestTag.END_TOTAL_TEXT)
            +CodexNodeInteraction.AssertTextEquals(total.toString())
        }
    }

    fun clickClear() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(ArrowInputsTestTag.CLEAR_BUTTON)
            +CodexNodeInteraction.PerformClick
        }
    }

    fun clickBackspace() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(ArrowInputsTestTag.BACKSPACE_BUTTON)
            +CodexNodeInteraction.PerformClick
        }
    }

    protected fun clickArrowInputsSubmit() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(ArrowInputsTestTag.SUBMIT_BUTTON)
            +CodexNodeInteraction.PerformClick
        }
    }

    protected fun clickArrowInputsCancel() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(ArrowInputsTestTag.CANCEL_BUTTON)
            +CodexNodeInteraction.PerformClick
        }
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
