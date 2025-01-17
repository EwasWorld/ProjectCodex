package eywa.projectcodex.instrumentedTests.robots

import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.onViewWithClassName
import eywa.projectcodex.common.setDatePickerValue
import eywa.projectcodex.common.setTimePickerValue
import eywa.projectcodex.common.sharedUi.DateSelectorRowTestTag
import eywa.projectcodex.components.newScore.NewScoreTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.matchDataRowValue
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.robots.selectFace.SelectFaceBaseRobot
import eywa.projectcodex.instrumentedTests.robots.selectRound.SelectRoundBaseRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsAddCountRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsAddEndRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.HeadToHeadAddHeatRobot
import java.util.Calendar

class NewScoreRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, NewScoreTestTag.SCREEN) {
    val selectFaceBaseRobot = SelectFaceBaseRobot(::perform)
    val selectRoundsRobot = SelectRoundBaseRobot(::perform)

    init {
        waitForDatabaseUpdate()
    }

    private fun waitForDatabaseUpdate() {
        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription() = "Waiting for database update to complete"

            override fun checkCondition() = try {
                checkElementDoesNotExist(NewScoreTestTag.DATABASE_WARNING)
                true
            }
            catch (e: java.lang.AssertionError) {
                false
            }
        })
    }

    fun checkTime(time: String) {
        checkElementText(DateSelectorRowTestTag.TIME_BUTTON, time)
    }

    fun checkDate(date: String) {
        checkElementText(DateSelectorRowTestTag.DATE_BUTTON, date)
    }

    fun setTime(calendar: Calendar) {
        setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
        )
    }

    fun setTime(hours: Int, minutes: Int) {
        clickElement(DateSelectorRowTestTag.TIME_BUTTON)
        CustomConditionWaiter.waitForClassToAppear(TimePicker::class.java)
        onViewWithClassName(TimePicker::class.java).perform(setTimePickerValue(hours, minutes))
        Espresso.onView(ViewMatchers.withText("OK")).perform(ViewActions.click())
    }

    fun setDate(calendar: Calendar) {
        setDate(
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR),
        )
    }

    fun setDate(day: Int, month: Int, year: Int) {
        clickElement(DateSelectorRowTestTag.DATE_BUTTON)
        val calendar = Calendar.getInstance()
        // Use a different hour/minute to ensure it's not overwriting the time
        calendar.set(year, month - 1, day, 13, 15, 0)
        CustomConditionWaiter.waitForClassToAppear(DatePicker::class.java)
        onViewWithClassName(DatePicker::class.java).perform(setDatePickerValue(calendar))
        Espresso.onView(ViewMatchers.withText("OK")).perform(ViewActions.click())
    }

    fun checkType(expectedType: Type) {
        performSingle {
            matchDataRowValue(NewScoreTestTag.TYPE_SWITCH)
            +CodexNodeInteraction.AssertTextEquals(expectedType.text)
        }
    }

    fun clickType(expectedType: Type) {
        performSingle {
            matchDataRowValue(NewScoreTestTag.TYPE_SWITCH)
            +CodexNodeInteraction.PerformScrollTo().waitFor()
            +CodexNodeInteraction.PerformClick()
            +CodexNodeInteraction.AssertTextEquals(expectedType.text)
        }
    }

    fun checkIsH2hSetPoints(isSetPoints: Boolean) {
        performSingle {
            matchDataRowValue(NewScoreTestTag.H2H_STYLE_SWITCH)
            +CodexNodeInteraction.PerformScrollTo().waitFor()
            +CodexNodeInteraction.AssertTextEquals(
                    if (isSetPoints) "Set points" else "Total score"
            )
        }
    }

    fun clickH2hSetPoints(expectedIsSetPoints: Boolean) {
        performSingle {
            matchDataRowValue(NewScoreTestTag.H2H_STYLE_SWITCH)
            +CodexNodeInteraction.PerformScrollTo().waitFor()
            +CodexNodeInteraction.PerformClick()
            +CodexNodeInteraction.AssertTextEquals(
                    if (expectedIsSetPoints) "Set points" else "Total score"
            )
        }
    }

    fun checkIsH2hStandardFormat(isStandardFormat: Boolean) {
        performSingle {
            matchDataRowValue(NewScoreTestTag.H2H_FORMAT_SWITCH)
            +CodexNodeInteraction.PerformScrollTo().waitFor()
            +CodexNodeInteraction.AssertTextEquals(
                    if (isStandardFormat) "Standard" else "Non-standard"
            )
        }
    }

    fun clickH2hStandardFormat(expectedIsStandardFormat: Boolean) {
        performSingle {
            matchDataRowValue(NewScoreTestTag.H2H_FORMAT_SWITCH)
            +CodexNodeInteraction.PerformScrollTo().waitFor()
            +CodexNodeInteraction.AssertTextEquals(
                    if (expectedIsStandardFormat) "Standard" else "Non-standard"
            )
        }
    }

    fun setHeadToHeadFields(teamSize: Int, qualiRank: Int?, totalArchers: Int?) {
        setText(NewScoreTestTag.H2H_TEAM_SIZE_INPUT, teamSize.toString())
        setText(NewScoreTestTag.H2H_QUALI_RANK_INPUT, qualiRank?.toString() ?: "")
        setText(NewScoreTestTag.H2H_TOTAL_ARCHERS_INPUT, totalArchers?.toString() ?: "")
    }

    fun checkHeadToHeadFieldsAreError() {
        checkElementIsDisplayed(NewScoreTestTag.H2H_TEAM_SIZE_ERROR)
        checkElementIsDisplayed(NewScoreTestTag.H2H_QUALI_RANK_ERROR)
        checkElementIsDisplayed(NewScoreTestTag.H2H_TOTAL_ARCHERS_ERROR)
    }

    fun clickSubmitNewScore(block: ShootDetailsAddEndRobot.() -> Unit = {}) {
        clickElement(NewScoreTestTag.SUBMIT_BUTTON, scrollTo = true)
        createRobot(ShootDetailsAddEndRobot::class, block)
    }

    fun clickSubmitNewScoreCount(block: ShootDetailsAddCountRobot.() -> Unit = {}) {
        clickElement(NewScoreTestTag.SUBMIT_BUTTON, scrollTo = true)
        createRobot(ShootDetailsAddCountRobot::class, block)
    }

    fun clickSubmitNewScoreHeadToHead(block: HeadToHeadAddHeatRobot.() -> Unit = {}) {
        clickElement(NewScoreTestTag.SUBMIT_BUTTON, scrollTo = true)
        createRobot(HeadToHeadAddHeatRobot::class, block)
    }

    fun clickSubmitEditScore() {
        clickElement(NewScoreTestTag.SUBMIT_BUTTON, scrollTo = true)
    }

    fun clickReset() {
        clickElement(NewScoreTestTag.RESET_BUTTON)
    }

    fun clickCancel() {
        clickElement(NewScoreTestTag.CANCEL_BUTTON)
    }

    enum class Type(val text: String) {
        SCORE("Score"),
        COUNT("Count"),
        HEAD_TO_HEAD("Head to head"),
    }
}
