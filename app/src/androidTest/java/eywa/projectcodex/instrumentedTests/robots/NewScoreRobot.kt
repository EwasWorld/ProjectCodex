package eywa.projectcodex.instrumentedTests.robots

import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.ui.test.*
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import eywa.projectcodex.common.*
import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogTestTag
import eywa.projectcodex.components.newScore.NewScoreTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.robots.shootDetails.AddEndRobot
import eywa.projectcodex.instrumentedTests.robots.common.SelectFaceRobot
import java.util.*

class NewScoreRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, NewScoreTestTag.SCREEN) {
    val facesRobot = SelectFaceRobot(composeTestRule, NewScoreTestTag.SCREEN)

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
        checkElementText(NewScoreTestTag.TIME_BUTTON, time)
    }

    fun checkDate(date: String) {
        checkElementText(NewScoreTestTag.DATE_BUTTON, date)
    }

    fun setTime(calendar: Calendar) {
        setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
        )
    }

    fun setTime(hours: Int, minutes: Int) {
        clickElement(NewScoreTestTag.TIME_BUTTON)
        CustomConditionWaiter.waitForClassToAppear(TimePicker::class.java)
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
        clickElement(NewScoreTestTag.DATE_BUTTON)
        val calendar = Calendar.getInstance()
        // Use a different hour/minute to ensure it's not overwriting the time
        calendar.set(year, month, day, 13, 15, 0)
        CustomConditionWaiter.waitForClassToAppear(DatePicker::class.java)
        onViewWithClassName(DatePicker::class.java).perform(setDatePickerValue(calendar))
        Espresso.onView(ViewMatchers.withText("OK")).perform(ViewActions.click())
    }

    fun clickSubmitNewScore(block: AddEndRobot.() -> Unit = {}) {
        clickElement(NewScoreTestTag.SUBMIT_BUTTON)
        AddEndRobot(composeTestRule).apply { block() }
    }

    fun clickSubmitEditScore() {
        clickElement(NewScoreTestTag.SUBMIT_BUTTON)
    }

    fun clickReset() {
        clickElement(NewScoreTestTag.RESET_BUTTON)
    }

    fun clickCancel() {
        clickElement(NewScoreTestTag.CANCEL_BUTTON)
    }

    fun clickSelectedRound() {
        composeTestRule.onNode(
                hasParent(hasTestTag(NewScoreTestTag.SELECTED_ROUND.getTestTag())).and(hasClickAction()),
                useUnmergedTree = true,
        ).performClick()
        CustomConditionWaiter.waitForComposeCondition {
            composeTestRule.onNodeWithTag(SelectRoundDialogTestTag.ROUND_DIALOG.getTestTag()).assertIsDisplayed()
        }
    }

    fun clickRoundDialogRound(displayName: String, index: Int = 0) {
        composeTestRule.onAllNodes(
                displayName.split(" ").map { hasAnyChild(hasText(it)) }.fold(
                        hasTestTag(SelectRoundDialogTestTag.ROUND_DIALOG_ITEM.getTestTag())
                ) { a, b -> a.and(b) },
                true,
        )[index].performClick()
    }

    fun clickRoundDialogNoRound() {
        clickElement(SimpleDialogTestTag.POSITIVE_BUTTON)
    }

    fun checkSelectedRound(displayName: String) {
        composeTestRule.onNode(
                hasTestTag(NewScoreTestTag.SELECTED_ROUND.getTestTag()).and(hasAnyChild(hasText(displayName))),
                useUnmergedTree = true,
        ).assertIsDisplayed()
    }

    fun checkSelectedSubtype(displayName: String) {
        checkElementText(NewScoreTestTag.SELECTED_SUBTYPE, displayName)
    }

    fun clickSelectedSubtype() {
        composeTestRule.onNode(
                hasParent(hasTestTag(NewScoreTestTag.SELECTED_SUBTYPE.getTestTag())).and(hasClickAction()),
                useUnmergedTree = true,
        ).performClick()
        CustomConditionWaiter.waitForComposeCondition {
            composeTestRule.onNodeWithTag(SelectRoundDialogTestTag.SUBTYPE_DIALOG.getTestTag()).assertIsDisplayed()
        }
    }

    fun clickSubtypeDialogSubtype(displayName: String, index: Int = 0) {
        composeTestRule.onAllNodes(
                displayName.split(" ").map { hasAnyChild(hasText(it)) }.fold(
                        hasTestTag(SelectRoundDialogTestTag.ROUND_DIALOG_ITEM.getTestTag())
                ) { a, b -> a.and(b) },
                true,
        )[index].performClick()
    }
}
