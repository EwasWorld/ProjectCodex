package eywa.projectcodex.instrumentedTests.robots

import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.ui.test.*
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.onViewWithClassName
import eywa.projectcodex.common.setDatePickerValue
import eywa.projectcodex.common.setTimePickerValue
import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.components.newScore.NewScoreFragment
import eywa.projectcodex.components.newScore.NewScoreScreen.TestTag
import eywa.projectcodex.instrumentedTests.robots.archerRoundScore.InputEndRobot
import java.util.*

class NewScoreRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, NewScoreFragment::class) {
    init {
        waitForDatabaseUpdate()
    }

    private fun waitForDatabaseUpdate() {
        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription() = "Waiting for database update to complete"

            override fun checkCondition() = try {
                checkElementDoesNotExist(TestTag.DATABASE_WARNING)
                true
            }
            catch (e: java.lang.AssertionError) {
                false
            }
        })
    }

    fun checkTime(time: String) {
        checkElementText(TestTag.TIME_BUTTON, time)
    }

    fun checkDate(date: String) {
        checkElementText(TestTag.DATE_BUTTON, date)
    }

    fun setTime(calendar: Calendar) {
        setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
        )
    }

    fun setTime(hours: Int, minutes: Int) {
        clickElement(TestTag.TIME_BUTTON)
        onViewWithClassName(TimePicker::class.java).perform(setTimePickerValue(hours, minutes))
        Espresso.onView(ViewMatchers.withText("OK")).perform(ViewActions.click())
    }

    fun setDate(calendar: Calendar) {
        setDate(
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.YEAR),
        )
    }

    fun setDate(day: Int, month: Int, year: Int) {
        clickElement(TestTag.DATE_BUTTON)
        val calendar = Calendar.getInstance()
        // Use a different hour/minute to ensure it's not overwriting the time
        calendar.set(year, month, day, 13, 15, 0)
        onViewWithClassName(DatePicker::class.java).perform(setDatePickerValue(calendar))
        Espresso.onView(ViewMatchers.withText("OK")).perform(ViewActions.click())
    }

    fun clickSubmitNewScore(block: InputEndRobot.() -> Unit = {}) {
        clickElement(TestTag.SUBMIT_BUTTON)
        InputEndRobot(composeTestRule).apply { block() }
    }

    fun clickSubmitEditScore() {
        clickElement(TestTag.SUBMIT_BUTTON)
    }

    fun clickReset() {
        clickElement(TestTag.RESET_BUTTON)
    }

    fun clickCancel() {
        clickElement(TestTag.CANCEL_BUTTON)
    }

    fun clickSelectedRound() {
        clickElement(TestTag.SELECTED_ROUND)
    }

    fun clickRoundDialogRound(displayName: String) {
        composeTestRule.onNode(
                hasAnyAncestor(hasTestTag(TestTag.ROUND_DIALOG))
                        .and(hasText(displayName))
        ).performClick()
    }

    fun clickRoundDialogNoRound() {
        clickElement(SimpleDialogTestTag.POSITIVE_BUTTON)
    }

    fun checkSelectedRound(displayName: String) {
        checkElementText(TestTag.SELECTED_ROUND, displayName)
    }

    fun checkSelectedSubtype(displayName: String) {
        checkElementText(TestTag.SELECTED_SUBTYPE, displayName)
    }

    fun clickSelectedSubtype() {
        clickElement(TestTag.SELECTED_SUBTYPE)
    }

    fun clickSubtypeDialogSubtype(index: Int) {
        composeTestRule.onNodeWithTag(TestTag.SUBTYPE_DIALOG)
                .onChildAt(index)
                .performClick()
    }
}