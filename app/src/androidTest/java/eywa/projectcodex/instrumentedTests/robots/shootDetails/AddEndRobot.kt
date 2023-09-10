package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.addEnd.AddEndTestTag
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher

class AddEndRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ArrowInputsRobot(composeTestRule, AddEndTestTag.SCREEN) {
    fun waitForLoad() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(ArrowInputsTestTag.CLEAR_BUTTON)
            +CodexNodeInteraction.AssertIsDisplayed.waitFor()
        }
    }

    fun waitForRemainingArrows() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.REMAINING_ARROWS_CURRENT)
            +CodexNodeInteraction.AssertIsDisplayed.waitFor()
        }
    }

    fun checkIndicatorTable(score: Int, arrowCount: Int) {
        perform {
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.ROUND_SCORE)
            +CodexNodeInteraction.AssertTextEquals(score.toString())
        }
        perform {
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.ROUND_ARROWS)
            +CodexNodeInteraction.AssertTextEquals(arrowCount.toString())
        }
    }

    fun checkRemainingArrows(currentDistance: String, laterDistances: String) {
        perform {
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.REMAINING_ARROWS_CURRENT)
            +CodexNodeInteraction.AssertTextEquals(currentDistance)
        }

        perform {
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.REMAINING_ARROWS_LATER)

            if (laterDistances.isNotBlank()) {
                +CodexNodeInteraction.AssertTextEquals(laterDistances)
            }
            else {
                +CodexNodeInteraction.AssertDoesNotExist
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
