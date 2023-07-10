package eywa.projectcodex.instrumentedTests.robots

import androidx.compose.ui.test.*
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import eywa.projectcodex.R
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.helpShowcase.ui.ComposeHelpShowcaseTestTag
import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.mainActivity.MainActivity
import java.util.*

abstract class BaseRobot(
        protected val composeTestRule: ComposeTestRule<MainActivity>,
        private val screenTestTag: String,
        private val screenStack: Stack<BaseRobot> = Stack(),
) {
    constructor(
            composeTestRule: ComposeTestRule<MainActivity>,
            screenTestTag: CodexTestTag,
    ) : this(composeTestRule, screenTestTag.getTestTag())

    constructor(
            composeTestRule: ComposeTestRule<MainActivity>,
            screenTestTag: CodexTestTag,
            previousScreen: BaseRobot,
            addScreenToStack: Boolean = true,
    ) : this(
            composeTestRule,
            screenTestTag.getTestTag(),
            previousScreen.screenStack.apply { if (addScreenToStack) push(previousScreen) else pop() },
    )

    protected val scenario: ActivityScenario<MainActivity> = composeTestRule.activityRule.scenario

    init {
        check(checkScreenIsShown()) { "Tried to create robot for $screenTestTag while it's not showing" }
    }

    /**
     * Checks that the screen associated with the current robot is displayed.
     * Checks that the node with the tag [screenTestTag] is shown,
     */
    fun checkScreenIsShown(): Boolean {
        CustomConditionWaiter.waitForComposeCondition { checkElementIsDisplayed(screenTestTag) }
        return true
    }

    fun clickElement(testTag: CodexTestTag, text: String? = null, useUnmergedTree: Boolean = false) =
            clickElement(testTag.getTestTag(), text, useUnmergedTree)

    fun clickElement(testTag: String, text: String? = null, useUnmergedTree: Boolean = false) {
        var matcher = hasTestTag(testTag)
        if (text != null) {
            matcher = matcher.and(hasText(text))
        }
        composeTestRule.onNode(matcher, useUnmergedTree).performClick()
    }

    fun checkElementText(testTag: CodexTestTag, text: String, useUnmergedTree: Boolean = false) =
            checkElementText(testTag.getTestTag(), text, useUnmergedTree)

    fun checkElementText(testTag: String, text: String, useUnmergedTree: Boolean = false) {
        composeTestRule.onNodeWithTag(testTag, useUnmergedTree).assertTextEquals(text)
    }

    fun checkElementText(testTag: CodexTestTag, index: Int, text: String, useUnmergedTree: Boolean = false) {
        composeTestRule.onAllNodesWithTag(testTag.getTestTag(), useUnmergedTree)[index].assertTextEquals(text)
    }

    fun checkLastElementText(testTag: CodexTestTag, text: String) {
        composeTestRule.onAllNodesWithTag(testTag.getTestTag()).onLast().assertTextEquals(text)
    }

    fun checkElementIsDisplayed(testTag: CodexTestTag, text: String? = null, useUnmergedTree: Boolean = false) =
            checkElementIsDisplayed(testTag.getTestTag(), text, useUnmergedTree)

    fun checkAllElements(testTag: CodexTestTag, check: SemanticsMatcher, useUnmergedTree: Boolean = false) =
            composeTestRule.onAllNodesWithTag(testTag.getTestTag(), useUnmergedTree).assertAll(check)


    fun checkElementIsDisplayed(testTag: String, text: String? = null, useUnmergedTree: Boolean = false) {
        var matcher = hasTestTag(testTag)
        if (text != null) {
            matcher = matcher.and(hasText(text))
        }
        composeTestRule.onNode(matcher, useUnmergedTree).assertIsDisplayed()
    }

    fun checkAtLeastOneElementIsDisplayed(testTag: CodexTestTag, useUnmergedTree: Boolean = false) {
        composeTestRule.onAllNodesWithTag(testTag.getTestTag(), useUnmergedTree).onFirst().assertIsDisplayed()
    }

    fun checkAtLeastOneElementIsDisplayed(testTag: String, useUnmergedTree: Boolean = false) {
        composeTestRule.onAllNodesWithTag(testTag, useUnmergedTree).onFirst().assertIsDisplayed()
    }

    fun checkElementDoesNotExist(testTag: CodexTestTag, useUnmergedTree: Boolean = false) =
            checkElementDoesNotExist(testTag.getTestTag(), useUnmergedTree)

    fun checkElementDoesNotExist(testTag: String, useUnmergedTree: Boolean = false) {
        composeTestRule.onNodeWithTag(testTag, useUnmergedTree).assertDoesNotExist()
    }

    fun checkCheckboxState(testTag: CodexTestTag, isChecked: Boolean, useUnmergedTree: Boolean = false) =
            checkCheckboxState(testTag.getTestTag(), isChecked, useUnmergedTree)

    fun checkCheckboxState(testTag: String, isChecked: Boolean, useUnmergedTree: Boolean = false) {
        val node = composeTestRule.onNodeWithTag(testTag, useUnmergedTree)
        if (isChecked) node.assertIsSelected() else node.assertIsNotSelected()
    }

    fun scrollTo(testTag: CodexTestTag, rowIndex: Int) =
            composeTestRule.onNodeWithTag(testTag.getTestTag()).performScrollToIndex(rowIndex)

    fun setText(testTag: CodexTestTag, text: String) = setText(testTag.getTestTag(), text)

    fun setText(testTag: String, text: String, append: Boolean = false) {
        if (!append) {
            composeTestRule.onNodeWithTag(testTag).performTextClearance()
        }
        composeTestRule.onNodeWithTag(testTag).performTextInput(text)
    }

    fun setChip(testTag: CodexTestTag, value: Boolean) {
        var actual = false
        try {
            checkCheckboxState(testTag, false)
        }
        catch (e: AssertionError) {
            actual = true
        }
        if (actual != value) {
            clickElement(testTag)
        }
    }

    fun clickDialogOk(titleText: String) = clickDialog(titleText, SimpleDialogTestTag.POSITIVE_BUTTON)
    fun clickDialogCancel(titleText: String) = clickDialog(titleText, SimpleDialogTestTag.NEGATIVE_BUTTON)

    private fun clickDialog(titleText: String, buttonTag: String) {
        CustomConditionWaiter.waitForComposeCondition("Waiting for $titleText dialog to display") {
            composeTestRule
                    .onNode(hasTestTag(SimpleDialogTestTag.TITLE).and(hasText(titleText)))
                    .assertIsDisplayed()
        }
        composeTestRule.onNodeWithTag(buttonTag).performClick()
    }

    fun clickHomeIcon(): MainMenuRobot {
        clickElement(MainActivity.MainActivityTestTag.HOME_ICON)
        return MainMenuRobot(composeTestRule)
    }

    fun clickHelpIcon() {
        clickElement(MainActivity.MainActivityTestTag.HELP_ICON)
        CustomConditionWaiter.waitForComposeCondition("Waiting for help to appear") {
            checkElementIsDisplayed(ComposeHelpShowcaseTestTag.CLOSE_BUTTON)
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
        clickElement(ComposeHelpShowcaseTestTag.NEXT_BUTTON)

        CustomConditionWaiter.waitFor(400)
        CustomConditionWaiter.waitForComposeCondition("Waiting for help to appear") {
            checkElementIsDisplayed(ComposeHelpShowcaseTestTag.TITLE)
        }
    }

    fun clickHelpShowcaseClose() {
        clickElement(ComposeHelpShowcaseTestTag.CLOSE_BUTTON)

        CustomConditionWaiter.waitForComposeCondition("Waiting for help to disappear") {
            checkElementDoesNotExist(ComposeHelpShowcaseTestTag.CLOSE_BUTTON)
        }
    }

    fun checkHelpShowcaseIsDisplayed() {
        checkElementIsDisplayed(ComposeHelpShowcaseTestTag.CLOSE_BUTTON)
    }

    protected fun <R : BaseRobot> popRobot(): R = screenStack.pop().apply { checkScreenIsShown() } as R

    fun <R : BaseRobot> clickAndroidBack(): R {
        pressBack()
        return popRobot()
    }
}
