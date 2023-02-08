package eywa.projectcodex.instrumentedTests.robots

import androidx.compose.ui.test.*
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import eywa.projectcodex.R
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.click
import eywa.projectcodex.common.helpShowcase.ui.ComposeHelpShowcaseTestTag
import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.components.mainActivity.MainActivity
import kotlin.reflect.KClass

abstract class BaseRobot(
        protected val composeTestRule: ComposeTestRule<MainActivity>,
        private val fragment: KClass<out Fragment>
) {
    protected val scenario: ActivityScenario<MainActivity> = composeTestRule.activityRule.scenario

    // TODO Use something other than a flat ms wait. Can you check something's on the screen?
    private val helpAnimationDuration = 400

    init {
        check(isShown()) { "Tried to create robot for ${fragment.simpleName} while it's not showing" }
    }

    // TODO Composify?
    fun isShown() = TestUtils.isFragmentShowing(scenario, fragment)

    fun clickElement(testTag: String) {
        composeTestRule.onNodeWithTag(testTag).performClick()
    }

    fun checkElementText(testTag: String, text: String) {
        composeTestRule.onNodeWithTag(testTag).assertTextEquals(text)
    }

    fun checkElementDoesNotExist(testTag: String) {
        composeTestRule.onNodeWithTag(testTag).assertDoesNotExist()
    }

    fun clickDialogOk(titleText: String) = clickDialog(titleText, SimpleDialogTestTag.POSITIVE_BUTTON)
    fun clickDialogCancel(titleText: String) = clickDialog(titleText, SimpleDialogTestTag.NEGATIVE_BUTTON)

    private fun clickDialog(
            titleText: String,
            buttonTag: String,
    ) {
        CustomConditionWaiter.waitForComposeCondition("Waiting for $titleText dialog to display") {
            composeTestRule
                    .onNode(
                            hasTestTag(SimpleDialogTestTag.TITLE).and(hasText(titleText))
                    )
                    .assertIsDisplayed()
        }
        composeTestRule
                .onNodeWithTag(buttonTag)
                .performClick()
    }

    fun clickHomeIcon() {
        R.id.action_bar__home.click()
    }

    fun clickHelpIcon() {
        R.id.action_bar__help.click()
        CustomConditionWaiter.waitFor(helpAnimationDuration)
    }

    fun clickHelpShowcaseNext() {
        composeTestRule.onNode(hasTestTag(ComposeHelpShowcaseTestTag.NEXT_BUTTON)).performClick()
        CustomConditionWaiter.waitFor(helpAnimationDuration * 2)
    }

    fun clickHelpShowcaseClose() {
        composeTestRule.onNode(hasTestTag(ComposeHelpShowcaseTestTag.CLOSE_BUTTON)).performClick()
        CustomConditionWaiter.waitFor(helpAnimationDuration)
    }

    fun checkHelpShowcaseIsDisplayed() {
        composeTestRule.onNode(hasTestTag(ComposeHelpShowcaseTestTag.CLOSE_BUTTON)).assertIsDisplayed()
    }
}
