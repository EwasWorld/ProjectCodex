package eywa.projectcodex.instrumentedTests.robots

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
}
