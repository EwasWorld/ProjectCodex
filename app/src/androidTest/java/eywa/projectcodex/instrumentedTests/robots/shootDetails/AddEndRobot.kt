package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.addEnd.AddEndTestTag
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.SightMarkDetailRobot
import eywa.projectcodex.instrumentedTests.robots.SightMarksRobot

class AddEndRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ArrowInputsRobot(composeTestRule, AddEndTestTag.SCREEN) {
    fun waitForLoad() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(ArrowInputsTestTag.CLEAR_BUTTON)
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
    }

    fun waitForRemainingArrows() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.REMAINING_ARROWS_CURRENT)
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
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
                +CodexNodeInteraction.AssertDoesNotExist()
            }
        }
    }

    fun clickNextEnd() = clickArrowInputsSubmit()


    fun clickRoundCompleteOk(block: ShootDetailsStatsRobot.() -> Unit = {}) {
        clickDialogOk(ROUND_COMPLETE_DIALOG_TITLE)
        createRobot(ShootDetailsStatsRobot::class, block)
    }

    fun checkSightMarkIndicator(distance: String, sightMark: String?) {
        checkElementText(
                AddEndTestTag.SIGHT_MARK_DESCRIPTION,
                if (sightMark == null) "No sight mark for $distance"
                else "$distance sight mark:",
        )
        performV2Single {
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.SIGHT_MARK)
            if (sightMark == null) {
                +CodexNodeInteraction.AssertDoesNotExist()
            }
            else {
                +CodexNodeInteraction.AssertTextEquals(sightMark)
            }
        }
    }

    fun clickAllSightMarks(block: SightMarksRobot.() -> Unit) {
        clickElement(AddEndTestTag.EXPAND_SIGHT_MARK)
        createRobot(SightMarksRobot::class, block)
    }

    fun clickEditSightMark(block: SightMarkDetailRobot.() -> Unit) {
        clickElement(AddEndTestTag.EDIT_SIGHT_MARK)
        createRobot(SightMarkDetailRobot::class, block)
    }

    companion object {
        private const val ROUND_COMPLETE_DIALOG_TITLE = "Round Complete"
    }
}
