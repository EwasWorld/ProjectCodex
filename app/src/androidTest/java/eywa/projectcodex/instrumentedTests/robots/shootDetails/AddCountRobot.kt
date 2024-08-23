package eywa.projectcodex.instrumentedTests.robots.shootDetails

import androidx.test.espresso.Espresso
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.addArrowCount.AddArrowCountTestTag
import eywa.projectcodex.components.shootDetails.addEnd.AddEndTestTag
import eywa.projectcodex.components.shootDetails.stats.ui.StatsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.clickDataRow
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.matchTextBox
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
        checkElementText(StatsTestTag.DATE_TEXT, date, true)
    }

    fun checkRound(round: String?) {
        checkElementText(StatsTestTag.ROUND_TEXT, round ?: "N/A", true)
    }

    fun clickEditRoundData(block: NewScoreRobot.() -> Unit) {
        clickElement(StatsTestTag.EDIT_SHOOT_INFO)
        createRobot(NewScoreRobot::class, block)
    }

    fun checkRemainingArrowsNotShown() {
        checkElementDoesNotExist(AddEndTestTag.REMAINING_ARROWS_CURRENT)
    }

    fun checkRemainingArrows(currentDistance: String, laterDistances: String?) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.REMAINING_ARROWS_CURRENT)
            +CodexNodeInteraction.AssertTextEquals(currentDistance).waitFor()
        }

        checkElementTextOrDoesNotExist(AddEndTestTag.REMAINING_ARROWS_LATER, laterDistances?.takeIf { it.isNotBlank() })
    }

    fun checkSightersCount(count: Int?) {
        checkElementTextOrDoesNotExist(AddArrowCountTestTag.SIGHTERS_COUNT, count?.toString(), true)
    }

    fun clickSighters(block: AddCountRobot.() -> Unit) {
        clickElement(AddArrowCountTestTag.SIGHTERS_COUNT, true)
        createRobot(AddCountRobot::class, block)
    }

    fun checkShotCount(count: Int) {
        checkElementText(AddArrowCountTestTag.SHOT_COUNT, count.toString(), true)
    }

    fun checkTotalCount(count: Int?) {
        checkElementTextOrDoesNotExist(AddArrowCountTestTag.TOTAL_COUNT, count?.toString(), true)
    }

    fun checkInput(amount: Int, error: String? = null) {
        performV2Single {
            matchTextBox(AddArrowCountTestTag.ADD_COUNT_INPUT)
            +CodexNodeInteraction.AssertTextEquals(amount.toString())
            +CodexNodeInteraction.AssertHasError(error)
        }
        checkElementIsDisplayedOrDoesNotExist(AddArrowCountTestTag.ADD_COUNT_INPUT_ERROR, error != null)
    }

    fun setInputAmount(amount: Int, error: String? = null) {
        performV2Single {
            matchTextBox(AddArrowCountTestTag.ADD_COUNT_INPUT)
            +CodexNodeInteraction.SetText(amount.toString())
            +CodexNodeInteraction.AssertHasError(error)
        }
        Espresso.closeSoftKeyboard()
        performV2Single {
            +CodexNodeMatcher.HasTestTag(AddArrowCountTestTag.ADD_COUNT_INPUT_ERROR)
            if (error == null) +CodexNodeInteraction.AssertDoesNotExist()
            else +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
    }

    fun clickIncreaseInput() {
        clickElement(AddArrowCountTestTag.INPUT_PLUS_BUTTON)
    }

    fun clickDecreaseInput() {
        clickElement(AddArrowCountTestTag.INPUT_MINUS_BUTTON)
    }

    fun checkAddNotExist() {
        checkElementDoesNotExist(AddArrowCountTestTag.SUBMIT)
    }

    fun clickAdd() {
        clickElement(AddArrowCountTestTag.SUBMIT)
    }

    fun checkRoundComplete() {
        checkElementIsDisplayed(AddArrowCountTestTag.ROUND_COMPLETE)
    }

    fun checkSightMarkIndicator(distance: String, sightMark: String?) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.SIGHT_MARK)
            +CodexNodeInteraction.AssertContentDescriptionEquals((sightMark ?: "None") + " $distance:")
        }
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
}
