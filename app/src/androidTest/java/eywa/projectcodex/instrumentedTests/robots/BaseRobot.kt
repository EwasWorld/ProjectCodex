package eywa.projectcodex.instrumentedTests.robots

import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.onAllNodesWithTag
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
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.setText
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.dsl.TestActionDsl
import eywa.projectcodex.instrumentedTests.dsl.TestActionDslMarker
import eywa.projectcodex.instrumentedTests.robots.common.Robot
import java.util.Calendar
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

@DslMarker
annotation class RobotDslMarker

@RobotDslMarker
@TestActionDslMarker
abstract class BaseRobot(
        protected val composeTestRule: ComposeTestRule<MainActivity>,
        private val screenTestTag: CodexTestTag,
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
        perform {
            +CodexNodeMatcher.HasTestTag(this@BaseRobot.screenTestTag)
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
        return true
    }

    override fun perform(config: TestActionDsl.() -> Unit) {
        TestActionDsl().apply(config).run(composeTestRule)
    }

    fun setDateAndTime(calendar: Calendar) {
        perform {
            +CodexNodeMatcher.HasTestTag(DateSelectorRowTestTag.DATE_BUTTON)
            +CodexNodeInteraction.PerformClick()
        }
        performDatePickerDateSelection(calendar)
        perform {
            +CodexNodeMatcher.HasTestTag(DateSelectorRowTestTag.TIME_BUTTON)
            +CodexNodeInteraction.PerformClick()
        }
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

    fun clickElement(
            testTag: CodexTestTag,
            text: String? = null,
            index: Int? = null,
            useUnmergedTree: Boolean = false
    ) {
        perform {
            val matchers = listOfNotNull(
                    CodexNodeMatcher.HasTestTag(testTag),
                    text?.let { CodexNodeMatcher.HasText(text) },
            )

            if (index != null) {
                allNodes(*matchers.toTypedArray())
                +CodexNodeGroupToOne.Index(index)
            }
            else {
                matchers.forEach { +it }
            }
            this.useUnmergedTree = useUnmergedTree

            +CodexNodeInteraction.PerformClick()
        }
    }


    fun checkElementText(testTag: CodexTestTag, text: String, index: Int? = null, useUnmergedTree: Boolean = false) {
        perform {
            val matcher = CodexNodeMatcher.HasTestTag(testTag)

            if (index != null) {
                allNodes(matcher)
                +CodexNodeGroupToOne.Index(index)
            }
            else {
                +matcher
            }
            this.useUnmergedTree = useUnmergedTree

            +CodexNodeInteraction.AssertTextEquals(text)
        }
    }

    fun checkAllElements(testTag: CodexTestTag, check: SemanticsMatcher, useUnmergedTree: Boolean = false) =
            composeTestRule.onAllNodesWithTag(testTag.getTestTag(), useUnmergedTree).assertAll(check)


    fun checkElementIsDisplayed(testTag: CodexTestTag, text: String? = null, useUnmergedTree: Boolean = false) {
        perform {
            this.useUnmergedTree = useUnmergedTree
            +CodexNodeMatcher.HasTestTag(testTag)
            if (text != null) {
                +CodexNodeMatcher.HasText(text)
            }
            +CodexNodeInteraction.AssertIsDisplayed()
        }
    }

    fun checkElementDoesNotExist(testTag: CodexTestTag, useUnmergedTree: Boolean = false) {
        perform {
            this.useUnmergedTree = useUnmergedTree
            +CodexNodeMatcher.HasTestTag(testTag)
            +CodexNodeInteraction.AssertDoesNotExist()
        }
    }

    fun checkCheckboxState(testTag: CodexTestTag, isChecked: Boolean, useUnmergedTree: Boolean = false) {
        perform {
            this.useUnmergedTree = useUnmergedTree
            +CodexNodeMatcher.HasTestTag(testTag)
            +CodexNodeInteraction.AssertIsSelected(isChecked)
        }
    }

    fun setText(testTag: CodexTestTag, text: String, append: Boolean = false) {
        perform {
            setText(testTag, text, append)
        }
    }

    fun setChip(testTag: CodexTestTag, value: Boolean, currentValue: Boolean) {
        if (value == currentValue) return
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(testTag)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun checkDialogIsDisplayed(titleText: String) {
        perform {
            +CodexNodeMatcher.HasTestTag(SimpleDialogTestTag.TITLE)
            +CodexNodeMatcher.HasText(titleText)
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
    }

    fun clickDialogOk(titleText: String) = clickDialog(titleText, SimpleDialogTestTag.POSITIVE_BUTTON)
    fun clickDialogCancel(titleText: String) = clickDialog(titleText, SimpleDialogTestTag.NEGATIVE_BUTTON)

    private fun clickDialog(titleText: String, buttonTag: CodexTestTag) {
        checkDialogIsDisplayed(titleText)
        perform {
            +CodexNodeMatcher.HasTestTag(buttonTag)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun clickHomeIcon() {
        perform {
            +CodexNodeMatcher.HasTestTag(MainActivity.MainActivityTestTag.HOME_ICON)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun clickHelpIcon() {
        perform {
            +CodexNodeMatcher.HasTestTag(MainActivity.MainActivityTestTag.HELP_ICON)
            +CodexNodeInteraction.PerformClick()
        }
        perform {
            +CodexNodeMatcher.HasTestTag(ComposeHelpShowcaseTestTag.CLOSE_BUTTON)
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
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
        perform {
            +CodexNodeMatcher.HasTestTag(ComposeHelpShowcaseTestTag.NEXT_BUTTON)
            +CodexNodeInteraction.PerformClick()
        }

        CustomConditionWaiter.waitFor(400)
        checkHelpShowcaseIsDisplayed()
    }

    fun clickHelpShowcaseClose() {
        perform {
            +CodexNodeMatcher.HasTestTag(ComposeHelpShowcaseTestTag.CLOSE_BUTTON)
            +CodexNodeInteraction.PerformClick()
        }
        perform {
            +CodexNodeMatcher.HasTestTag(ComposeHelpShowcaseTestTag.CLOSE_BUTTON)
            +CodexNodeInteraction.AssertDoesNotExist().waitFor()
        }
    }

    fun checkHelpShowcaseIsDisplayed() {
        perform {
            +CodexNodeMatcher.HasTestTag(ComposeHelpShowcaseTestTag.CLOSE_BUTTON)
            +CodexNodeInteraction.AssertIsDisplayed()
        }
    }

    override fun <R : BaseRobot> createRobot(clazz: KClass<R>, block: R.() -> Unit) {
        clazz.primaryConstructor!!.call(composeTestRule).apply(block)
    }
}
