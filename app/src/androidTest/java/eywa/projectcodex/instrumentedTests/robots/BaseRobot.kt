package eywa.projectcodex.instrumentedTests.robots

import androidx.compose.ui.test.*
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.components.mainActivity.MainActivity
import kotlin.reflect.KClass

abstract class BaseRobot(
        protected val composeTestRule: ComposeTestRule<MainActivity>,
        private val fragment: KClass<out Fragment>
) {
    protected val scenario: ActivityScenario<MainActivity> = composeTestRule.activityRule.scenario

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
}
