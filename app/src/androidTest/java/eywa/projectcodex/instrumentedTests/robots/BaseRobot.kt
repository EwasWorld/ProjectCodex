package eywa.projectcodex.instrumentedTests.robots

import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.helpShowcase.ui.ComposeHelpShowcaseTestTag
import eywa.projectcodex.common.onViewWithClassName
import eywa.projectcodex.common.setDatePickerValue
import eywa.projectcodex.common.setTimePickerValue
import eywa.projectcodex.common.sharedUi.DateSelectorRowTestTag
import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.checkInputtedText
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.matchDataRowValue
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.setText
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.dsl.TestActionDslGroupNode
import eywa.projectcodex.instrumentedTests.dsl.TestActionDslMarker
import eywa.projectcodex.instrumentedTests.dsl.TestActionDslRoot
import eywa.projectcodex.instrumentedTests.dsl.TestActionDslSingleNode
import eywa.projectcodex.instrumentedTests.robots.common.Robot
import java.util.Calendar
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

@DslMarker
annotation class RobotDslMarker

@RobotDslMarker
@TestActionDslMarker
abstract class BaseRobot(
        private val composeTestRule: ComposeTestRule<MainActivity>,
        val screenTestTag: CodexTestTag,
) : Robot {
    protected val scenario: ActivityScenario<MainActivity> = composeTestRule.activityRule.scenario

    init {
        check(checkScreenIsShown()) { "Tried to create robot for $screenTestTag while it's not showing" }
    }

    /**
     * Checks that the screen associated with the current robot is displayed.
     * Checks that the node with the tag [screenTestTag] is shown,
     */
    fun checkScreenIsShown(): Boolean {
        performSingle {
            +CodexNodeMatcher.HasTestTag(this@BaseRobot.screenTestTag)
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
        return true
    }

    override fun perform(config: TestActionDslRoot.() -> Unit) {
        TestActionDslRoot().apply(config).perform(composeTestRule)
    }

    fun performSingle(config: TestActionDslSingleNode.First.() -> Unit) {
        perform { singleNode { config() } }
    }

    fun performGroup(config: TestActionDslGroupNode.First.() -> Unit) {
        perform { allNodes { config() } }
    }

    fun setDateAndTime(calendar: Calendar) {
        clickElement(DateSelectorRowTestTag.DATE_BUTTON)
        performDatePickerDateSelection(calendar)
        clickElement(DateSelectorRowTestTag.TIME_BUTTON)
        performTimePickerTimeSelection(calendar)
    }

    fun performDatePickerDateSelection(calendar: Calendar) {
        CustomConditionWaiter.waitForClassToAppear(DatePicker::class.java)
        onViewWithClassName(DatePicker::class.java).perform(setDatePickerValue(calendar))
        Espresso.onView(ViewMatchers.withText("OK")).perform(ViewActions.click())
    }

    fun performTimePickerTimeSelection(calendar: Calendar) {
        CustomConditionWaiter.waitForClassToAppear(TimePicker::class.java)
        onViewWithClassName(TimePicker::class.java).perform(
                setTimePickerValue(
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                )
        )
        Espresso.onView(ViewMatchers.withText("OK")).perform(ViewActions.click())
    }

    fun clickElement(testTag: CodexTestTag, useUnmergedTree: Boolean = false, scrollTo: Boolean = false) {
        performSingle {
            useUnmergedTree(useUnmergedTree)
            +CodexNodeMatcher.HasTestTag(testTag)
            if (scrollTo) {
                +CodexNodeInteraction.PerformScrollTo()
            }
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun checkElementText(testTag: CodexTestTag, text: String, useUnmergedTree: Boolean = false) {
        perform {
            singleNode {
                useUnmergedTree(useUnmergedTree)
                +CodexNodeMatcher.HasTestTag(testTag)
                +CodexNodeInteraction.AssertTextEquals(text).waitFor()
            }
        }
    }

    fun checkElementTextOrDoesNotExist(testTag: CodexTestTag, text: String?, useUnmergedTree: Boolean = false) {
        if (text == null) {
            checkElementDoesNotExist(testTag, useUnmergedTree)
        }
        else {
            checkElementText(testTag, text, useUnmergedTree)
        }
    }

    fun checkElementIsDisplayedOrDoesNotExist(
            testTag: CodexTestTag,
            displayed: Boolean,
            useUnmergedTree: Boolean = false,
    ) {
        if (displayed) {
            checkElementIsDisplayed(testTag, useUnmergedTree)
        }
        else {
            checkElementDoesNotExist(testTag, useUnmergedTree)
        }
    }

    fun checkElementIsDisplayed(testTag: CodexTestTag, useUnmergedTree: Boolean = false, scrollTo: Boolean = false) {
        performSingle {
            useUnmergedTree(useUnmergedTree)
            +CodexNodeMatcher.HasTestTag(testTag)
            if (scrollTo) {
                +CodexNodeInteraction.PerformScrollTo()
            }
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
    }

    fun checkElementDoesNotExist(testTag: CodexTestTag, useUnmergedTree: Boolean = false) {
        performSingle {
            useUnmergedTree(useUnmergedTree)
            +CodexNodeMatcher.HasTestTag(testTag)
            +CodexNodeInteraction.AssertDoesNotExist()
        }
    }

    fun checkCheckboxState(testTag: CodexTestTag, isChecked: Boolean, useUnmergedTree: Boolean = false) {
        performSingle {
            useUnmergedTree(useUnmergedTree)
            +CodexNodeMatcher.HasTestTag(testTag)
            +CodexNodeInteraction.AssertIsSelected(isChecked).waitFor()
        }
    }

    fun checkDataRow(testTag: CodexTestTag, title: String, text: String) {
        checkDataRowContentDescription(testTag, "$text $title")
    }

    fun checkDataRowContentDescription(testTag: CodexTestTag, text: String) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(testTag)
            +CodexNodeInteraction.AssertContentDescriptionEquals(text).waitFor()
        }
    }

    fun checkDataRowValueText(testTag: CodexTestTag, text: String) {
        performGroup {
            useUnmergedTree()
            +CodexNodeMatcher.HasAnyAncestor(CodexNodeMatcher.HasTestTag(testTag))
            toSingle(CodexNodeGroupToOne.Index(1)) {
                +CodexNodeInteraction.AssertTextEquals(text).waitFor()
            }
        }
    }

    fun clickDataRowValue(testTag: CodexTestTag) {
        performSingle {
            matchDataRowValue(testTag)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun setText(testTag: CodexTestTag, text: String, append: Boolean = false) {
        perform {
            setText(testTag, text, append)
        }
    }

    fun checkInputtedText(testTag: CodexTestTag, text: String) {
        perform {
            checkInputtedText(testTag, text)
        }
    }

    fun setChip(testTag: CodexTestTag, value: Boolean, currentValue: Boolean) {
        if (value == currentValue) return
        performSingle {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(testTag)
            +CodexNodeInteraction.PerformScrollTo()
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun checkDialogIsDisplayed(titleText: String) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(SimpleDialogTestTag.TITLE)
            +CodexNodeMatcher.HasText(titleText)
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
    }

    fun clickDialogOk(titleText: String) = clickDialog(titleText, SimpleDialogTestTag.POSITIVE_BUTTON)

    fun clickDialogCancel(titleText: String) = clickDialog(titleText, SimpleDialogTestTag.NEGATIVE_BUTTON)

    private fun clickDialog(titleText: String, buttonTag: CodexTestTag) {
        checkDialogIsDisplayed(titleText)
        clickElement(buttonTag)
    }

    fun clickHomeIcon() {
        clickElement(MainActivity.MainActivityTestTag.HOME_ICON)
    }

    fun clickHelpIcon() {
        clickElement(MainActivity.MainActivityTestTag.HELP_ICON)
        checkHelpShowcaseIsDisplayed()
    }

    fun cycleThroughComposeHelpDialogs() {
        clickHelpIcon()
        while (true) {
            checkHelpShowcaseIsDisplayed()
            try {
                clickHelpShowcaseNext()
            }
            catch (e: AssertionError) {
                clickHelpShowcaseClose()
                break
            }
        }
    }

    fun clickHelpShowcaseNext() {
        try {
            clickElement(ComposeHelpShowcaseTestTag.NEXT_BUTTON)
        }
        catch (e: Exception) {
            clickElement(ComposeHelpShowcaseTestTag.CANVAS)
        }

        CustomConditionWaiter.waitFor(400)
        checkHelpShowcaseIsDisplayed()
    }

    fun clickHelpShowcaseClose() {
        try {
            clickElement(ComposeHelpShowcaseTestTag.CLOSE_BUTTON)
        }
        catch (e: Exception) {
            clickElement(ComposeHelpShowcaseTestTag.CANVAS)
        }

        performSingle {
            +CodexNodeMatcher.HasTestTag(ComposeHelpShowcaseTestTag.CLOSE_BUTTON)
            +CodexNodeInteraction.AssertDoesNotExist().waitFor()
        }
    }

    fun checkHelpShowcaseIsDisplayed() {
        checkElementIsDisplayed(ComposeHelpShowcaseTestTag.TITLE)
    }

    override fun <R : BaseRobot> createRobot(clazz: KClass<R>, block: R.() -> Unit) {
        clazz.primaryConstructor!!.call(composeTestRule).apply(block)
    }
}
