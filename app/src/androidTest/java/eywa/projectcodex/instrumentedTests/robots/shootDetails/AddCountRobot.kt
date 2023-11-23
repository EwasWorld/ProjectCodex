package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.addArrowCount.AddArrowCountTestTag
import eywa.projectcodex.components.shootDetails.addEnd.AddEndTestTag
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.checkInputtedText
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.setText
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.BaseRobot
import eywa.projectcodex.instrumentedTests.robots.NewScoreRobot

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
            +CodexNodeMatcher.HasTestTag(AddArrowCountTestTag.SIGHTERS_COUNT)
            if (count == null) +CodexNodeInteraction.AssertDoesNotExist()
            else +CodexNodeInteraction.AssertTextEquals("Sighters: $count")
        }
    }

    fun checkShotCount(count: Int) {
        perform {
            +CodexNodeMatcher.HasTestTag(AddArrowCountTestTag.SHOT_COUNT)
            +CodexNodeInteraction.AssertTextEquals(count.toString())
        }
    }

    fun checkTotalCount(count: Int?) {
        perform {
            +CodexNodeMatcher.HasTestTag(AddArrowCountTestTag.TOTAL_COUNT)
            if (count == null) +CodexNodeInteraction.AssertDoesNotExist()
            else +CodexNodeInteraction.AssertTextEquals("Total: $count")
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
        perform {
            +CodexNodeMatcher.HasTestTag(AddArrowCountTestTag.ADD_COUNT_INPUT_ERROR)
            if (error == null) +CodexNodeInteraction.AssertDoesNotExist()
            else +CodexNodeInteraction.AssertIsDisplayed()
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
}
