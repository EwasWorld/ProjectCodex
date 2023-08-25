package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.addEnd.AddEndTestTag
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeAction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher

class AddEndRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ArrowInputsRobot(composeTestRule, AddEndTestTag.SCREEN) {
    fun waitForLoad() {
        perform {
            waitForDisplay = true
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(ArrowInputsTestTag.CLEAR_BUTTON)
            +CodexNodeAction.AssertIsDisplayed
        }
    }

    fun waitForRemainingArrows() {
        perform {
            waitForDisplay = true
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.REMAINING_ARROWS_CURRENT)
            +CodexNodeAction.AssertIsDisplayed
        }
    }

    fun checkIndicatorTable(score: Int, arrowCount: Int) {
        perform {
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.ROUND_SCORE)
            +CodexNodeAction.AssertTextEquals(score.toString())
        }
        perform {
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.ROUND_ARROWS)
            +CodexNodeAction.AssertTextEquals(arrowCount.toString())
        }
    }

    fun checkRemainingArrows(currentDistance: String, laterDistances: String) {
        perform {
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.REMAINING_ARROWS_CURRENT)
            +CodexNodeAction.AssertTextEquals(currentDistance)
        }

        perform {
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.REMAINING_ARROWS_LATER)

            if (laterDistances.isNotBlank()) {
                +CodexNodeAction.AssertTextEquals(laterDistances)
            }
            else {
                +CodexNodeAction.AssertDoesNotExist
            }
        }
    }

    fun clickNextEnd() = clickArrowInputsSubmit()


    fun clickRoundCompleteOk(block: ShootDetailsStatsRobot.() -> Unit = {}) {
        clickDialogOk(ROUND_COMPLETE_DIALOG_TITLE)
        ShootDetailsStatsRobot(composeTestRule).apply(block)
    }

    companion object {
        private const val ROUND_COMPLETE_DIALOG_TITLE = "Round Complete"
    }
}
