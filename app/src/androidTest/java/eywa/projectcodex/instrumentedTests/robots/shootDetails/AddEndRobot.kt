package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.addEnd.AddEndTestTag
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.clickDataRow
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.SightMarkDetailRobot
import eywa.projectcodex.instrumentedTests.robots.SightMarksRobot

class AddEndRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ArrowInputsRobot(composeTestRule, AddEndTestTag.SCREEN) {
    fun waitForLoad() {
        performV2Single {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(ArrowInputsTestTag.CLEAR_BUTTON)
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
    }

    fun waitForRemainingArrows() {
        performV2Single {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.REMAINING_ARROWS_CURRENT)
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
    }

    fun checkIndicatorTable(score: Int, arrowCount: Int) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.ROUND_SCORE)
            +CodexNodeInteraction.AssertTextEquals(score.toString())
        }
        performV2Single {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.ROUND_ARROWS)
            +CodexNodeInteraction.AssertTextEquals(arrowCount.toString())
        }
    }

    fun checkRemainingArrows(currentDistance: String, laterDistances: String) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.REMAINING_ARROWS_CURRENT)
            +CodexNodeInteraction.AssertTextEquals(currentDistance).waitFor()
        }

        performV2Single {
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
        clickElement(AddEndTestTag.ROUND_COMPLETE_BUTTON)
        createRobot(ShootDetailsStatsRobot::class, block)
    }

    fun checkSightMarkIndicator(distance: String, sightMark: String?) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.SIGHT_MARK)
            +CodexNodeInteraction.AssertContentDescriptionEquals((sightMark ?: "None") + " $distance:")
        }
    }

    fun checkAllSightMarkOnly() {
        checkElementIsDisplayed(AddEndTestTag.EXPAND_SIGHT_MARK)
        checkElementDoesNotExist(AddEndTestTag.SIGHT_MARK)
    }

    fun clickAllSightMarks(block: SightMarksRobot.() -> Unit) {
        clickElement(AddEndTestTag.EXPAND_SIGHT_MARK)
        createRobot(SightMarksRobot::class, block)
    }

    fun clickEditSightMark(block: SightMarkDetailRobot.() -> Unit) {
        performV2 {
            clickDataRow(AddEndTestTag.SIGHT_MARK)
        }
        createRobot(SightMarkDetailRobot::class, block)
    }

    fun checkSightersCount(count: Int?) {
        checkElementTextOrDoesNotExist(AddEndTestTag.SIGHTERS, count?.toString(), true)
    }

    fun clickSighters(block: AddCountRobot.() -> Unit) {
        clickElement(AddEndTestTag.SIGHTERS, true)
        createRobot(AddCountRobot::class, block)
    }

    companion object {
        private const val ROUND_COMPLETE_DIALOG_TITLE = "Round Complete"
    }
}
