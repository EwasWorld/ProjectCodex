package eywa.projectcodex.instrumentedTests.robots.shootDetails

import androidx.test.espresso.Espresso
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.addArrowCount.AddArrowCountTestTag
import eywa.projectcodex.components.shootDetails.addEnd.AddEndTestTag
import eywa.projectcodex.components.shootDetails.stats.ui.StatsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.checkInputtedText
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.setText
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.BaseRobot
import eywa.projectcodex.instrumentedTests.robots.NewScoreRobot
import eywa.projectcodex.instrumentedTests.robots.SightMarkDetailRobot
import eywa.projectcodex.instrumentedTests.robots.SightMarksRobot

class AddCountRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, AddArrowCountTestTag.SCREEN) {
    fun checkDate(date: String) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.DATE_TEXT)
            +CodexNodeInteraction.AssertTextEquals(date)
        }
    }

    fun checkRound(round: String?) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.ROUND_TEXT)
            +CodexNodeInteraction.AssertTextEquals(round ?: "N/A")
        }
    }

    fun clickEditRoundData(block: NewScoreRobot.() -> Unit) {
        perform {
            +CodexNodeMatcher.HasTestTag(StatsTestTag.EDIT_SHOOT_INFO)
            +CodexNodeInteraction.PerformClick()
        }

        createRobot(NewScoreRobot::class, block)
    }

    fun checkRemainingArrowsNotShown() {
        perform {
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.REMAINING_ARROWS_CURRENT)
            +CodexNodeInteraction.AssertDoesNotExist()
        }
    }

    fun checkRemainingArrows(currentDistance: String, laterDistances: String?) {
        perform {
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.REMAINING_ARROWS_CURRENT)
            +CodexNodeInteraction.AssertTextEquals(currentDistance).waitFor()
        }

        perform {
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.REMAINING_ARROWS_LATER)

            if (!laterDistances.isNullOrBlank()) {
                +CodexNodeInteraction.AssertTextEquals(laterDistances)
            }
            else {
                +CodexNodeInteraction.AssertDoesNotExist()
            }
        }
    }

    fun checkSightersCount(count: Int?) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(AddArrowCountTestTag.SIGHTERS_COUNT)
            if (count == null) +CodexNodeInteraction.AssertDoesNotExist()
            else +CodexNodeInteraction.AssertTextEquals(count.toString())
        }
    }

    fun checkShotCount(count: Int) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(AddArrowCountTestTag.SHOT_COUNT)
            +CodexNodeInteraction.AssertTextEquals(count.toString())
        }
    }

    fun checkTotalCount(count: Int?) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(AddArrowCountTestTag.TOTAL_COUNT)
            if (count == null) +CodexNodeInteraction.AssertDoesNotExist()
            else +CodexNodeInteraction.AssertTextEquals(count.toString())
        }
    }

    fun checkInput(amount: Int, error: String? = null) {
        perform {
            checkInputtedText(AddArrowCountTestTag.ADD_COUNT_INPUT, amount.toString())
            +CodexNodeInteraction.AssertHasError(error)
        }
        perform {
            +CodexNodeMatcher.HasTestTag(AddArrowCountTestTag.ADD_COUNT_INPUT_ERROR)
            if (error == null) +CodexNodeInteraction.AssertDoesNotExist()
            else +CodexNodeInteraction.AssertIsDisplayed()
        }
    }

    fun setInputAmount(amount: Int, error: String? = null) {
        perform {
            setText(AddArrowCountTestTag.ADD_COUNT_INPUT, amount.toString())
            +CodexNodeInteraction.AssertHasError(error)
        }
        Espresso.closeSoftKeyboard()
        perform {
            +CodexNodeMatcher.HasTestTag(AddArrowCountTestTag.ADD_COUNT_INPUT_ERROR)
            if (error == null) +CodexNodeInteraction.AssertDoesNotExist()
            else +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
    }

    fun clickIncreaseInput() {
        perform {
            +CodexNodeMatcher.HasTestTag(AddArrowCountTestTag.INPUT_PLUS_BUTTON)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun clickDecreaseInput() {
        perform {
            +CodexNodeMatcher.HasTestTag(AddArrowCountTestTag.INPUT_MINUS_BUTTON)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun checkAddNotExist() {
        perform {
            +CodexNodeMatcher.HasTestTag(AddArrowCountTestTag.SUBMIT)
            +CodexNodeInteraction.AssertDoesNotExist()
        }
    }

    fun clickAdd() {
        perform {
            +CodexNodeMatcher.HasTestTag(AddArrowCountTestTag.SUBMIT)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun checkRoundComplete() {
        perform {
            +CodexNodeMatcher.HasTestTag(AddArrowCountTestTag.ROUND_COMPLETE)
            +CodexNodeInteraction.AssertIsDisplayed()
        }
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
}
