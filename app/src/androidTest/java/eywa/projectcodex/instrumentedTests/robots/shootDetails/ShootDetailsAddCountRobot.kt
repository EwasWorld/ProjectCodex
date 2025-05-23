package eywa.projectcodex.instrumentedTests.robots.shootDetails

import androidx.test.espresso.Espresso
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.addArrowCount.AddArrowCountTestTag
import eywa.projectcodex.components.shootDetails.addEnd.AddEndTestTag
import eywa.projectcodex.components.shootDetails.stats.ui.StatsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.matchTextBox
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.BaseRobot
import eywa.projectcodex.instrumentedTests.robots.NewScoreRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.common.SightMarkIndicatorRobot

class ShootDetailsAddCountRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, AddArrowCountTestTag.SCREEN) {
    val sightMarkIndicatorRobot = SightMarkIndicatorRobot(this, AddArrowCountTestTag.SCREEN)

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
        performSingle {
            +CodexNodeMatcher.HasTestTag(AddEndTestTag.REMAINING_ARROWS_CURRENT)
            +CodexNodeInteraction.AssertTextEquals(currentDistance).waitFor()
        }

        checkElementTextOrDoesNotExist(AddEndTestTag.REMAINING_ARROWS_LATER, laterDistances?.takeIf { it.isNotBlank() })
    }

    fun checkSightersCount(count: Int?) {
        checkElementTextOrDoesNotExist(AddArrowCountTestTag.SIGHTERS_COUNT, count?.toString(), true)
    }

    fun clickSighters(block: ShootDetailsAddCountRobot.() -> Unit) {
        clickElement(AddArrowCountTestTag.SIGHTERS_COUNT, true)
        createRobot(ShootDetailsAddCountRobot::class, block)
    }

    fun checkShotCount(count: Int) {
        checkElementText(AddArrowCountTestTag.SHOT_COUNT, count.toString(), true)
    }

    fun checkTotalCount(count: Int?) {
        checkElementTextOrDoesNotExist(AddArrowCountTestTag.TOTAL_COUNT, count?.toString(), true)
    }

    fun checkInput(amount: Int, error: String? = null) {
        performSingle {
            matchTextBox(AddArrowCountTestTag.ADD_COUNT_INPUT)
            +CodexNodeInteraction.AssertTextEquals(amount.toString())
            +CodexNodeInteraction.AssertHasError(error)
        }
        checkElementIsDisplayedOrDoesNotExist(AddArrowCountTestTag.ADD_COUNT_INPUT_ERROR, error != null)
    }

    fun setInputAmount(amount: Int, error: String? = null) {
        performSingle {
            matchTextBox(AddArrowCountTestTag.ADD_COUNT_INPUT)
            +CodexNodeInteraction.SetText(amount.toString())
            +CodexNodeInteraction.AssertHasError(error)
        }
        Espresso.closeSoftKeyboard()
        performSingle {
            +CodexNodeMatcher.HasTestTag(AddArrowCountTestTag.ADD_COUNT_INPUT_ERROR)
            if (error == null) {
                +CodexNodeInteraction.AssertDoesNotExist()
            }
            else {
                +CodexNodeInteraction.PerformScrollTo().waitFor()
                +CodexNodeInteraction.AssertIsDisplayed().waitFor()
            }
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
        clickElement(AddArrowCountTestTag.SUBMIT, scrollTo = true)
    }

    fun checkRoundComplete() {
        checkElementIsDisplayed(AddArrowCountTestTag.ROUND_COMPLETE)
    }
}
