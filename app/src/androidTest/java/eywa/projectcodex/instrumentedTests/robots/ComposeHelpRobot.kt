package eywa.projectcodex.instrumentedTests.robots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performClick
import eywa.projectcodex.R
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.click
import eywa.projectcodex.common.helpShowcase.ui.ComposeHelpShowcaseTestTag
import eywa.projectcodex.components.mainActivity.MainActivity

class ComposeHelpRobot(private val composeTestRule: ComposeTestRule<MainActivity>) {
    private val scenario = composeTestRule.activityRule.scenario
    private val animationDuration = 400

    // TODO Use something other than a flat ms wait. Can you check something's on the screen?

    fun clickHelpIcon() {
        R.id.action_bar__help.click()
        CustomConditionWaiter.waitFor(animationDuration)
    }

    fun clickNext() {
        composeTestRule.onNode(hasTestTag(ComposeHelpShowcaseTestTag.NEXT_BUTTON)).performClick()
        CustomConditionWaiter.waitFor(animationDuration * 2)
    }

    fun clickClose() {
        composeTestRule.onNode(hasTestTag(ComposeHelpShowcaseTestTag.CLOSE_BUTTON)).performClick()
        CustomConditionWaiter.waitFor(animationDuration)
    }

    fun checkHelpIsDisplayed() {
        composeTestRule.onNode(hasTestTag(ComposeHelpShowcaseTestTag.CLOSE_BUTTON)).assertIsDisplayed()
    }
}