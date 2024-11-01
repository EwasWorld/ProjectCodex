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
import eywa.projectcodex.instrumentedTests.robots.shootDetails.AddCountRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.AddEndRobot
import java.util.Calendar

class NewScoreRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, NewScoreTestTag.SCREEN) {
    val selectFaceBaseRobot = SelectFaceBaseRobot(::performV2)
    val selectRoundsRobot = SelectRoundBaseRobot(::performV2)

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

    fun checkType(isScoring: Boolean = true) {
        val expected = if (isScoring) "Score" else "Count"
        perform {
            matchDataRowValue(NewScoreTestTag.TYPE_SWITCH)
            +CodexNodeInteraction.AssertTextEquals(expected)
        }
    }

    fun clickType(becomesIsScoring: Boolean = true) {
        val expected = if (becomesIsScoring) "Score" else "Count"
        perform {
            matchDataRowValue(NewScoreTestTag.TYPE_SWITCH)
            +CodexNodeInteraction.PerformClick()
            +CodexNodeInteraction.AssertTextEquals(expected)
        }
    }

    fun clickSubmitNewScore(block: AddEndRobot.() -> Unit = {}) {
        clickElement(NewScoreTestTag.SUBMIT_BUTTON)
        createRobot(AddEndRobot::class, block)
    }

    fun clickSubmitNewScoreCount(block: AddCountRobot.() -> Unit = {}) {
        clickElement(NewScoreTestTag.SUBMIT_BUTTON)
        createRobot(AddCountRobot::class, block)
    }

    fun clickSubmitEditScore() {
        clickElement(NewScoreTestTag.SUBMIT_BUTTON)
    }

    fun clickSubmitEditScoreChangeToCount(block: AddCountRobot.() -> Unit = {}) {
        clickElement(NewScoreTestTag.SUBMIT_BUTTON)
        createRobot(AddCountRobot::class, block)
    }

    fun clickSubmitEditScoreChangeToScore(block: AddEndRobot.() -> Unit = {}) {
        clickElement(NewScoreTestTag.SUBMIT_BUTTON)
        createRobot(AddEndRobot::class, block)
    }

    fun clickReset() {
        clickElement(NewScoreTestTag.RESET_BUTTON)
    }

    fun clickCancel() {
        clickElement(NewScoreTestTag.CANCEL_BUTTON)
    }
}
