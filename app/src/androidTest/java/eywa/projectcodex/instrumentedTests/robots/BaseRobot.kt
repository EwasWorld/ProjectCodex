package eywa.projectcodex.instrumentedTests.robots

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.components.mainActivity.MainActivity
import kotlin.reflect.KClass

abstract class BaseRobot(
        protected val composeTestRule: ComposeTestRule<MainActivity>,
        fragment: KClass<out Fragment>
) {
    protected val scenario: ActivityScenario<MainActivity> = composeTestRule.activityRule.scenario

    init {
        if (!TestUtils.isFragmentShowing(scenario, fragment)) {
            throw IllegalStateException("Tried to create robot for ${fragment.simpleName} while it's not showing")
        }
    }

    fun clickElement(testTag: String) {
        composeTestRule.onNodeWithTag(testTag).performClick()
    }

    fun checkElementText(testTag: String, text: String) {
        composeTestRule.onNodeWithTag(testTag).assertTextEquals(text)
    }

    fun checkElementDoesNotExist(testTag: String) {
        composeTestRule.onNodeWithTag(testTag).assertDoesNotExist()
    }
}
