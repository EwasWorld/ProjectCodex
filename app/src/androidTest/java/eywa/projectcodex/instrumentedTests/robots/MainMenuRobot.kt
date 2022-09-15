package eywa.projectcodex.instrumentedTests.robots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.sharedUi.SimpleAlertDialogTestTag
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.components.mainMenu.MainMenuFragment
import eywa.projectcodex.components.mainMenu.MainMenuScreen
import eywa.projectcodex.components.newScore.NewScoreFragment
import eywa.projectcodex.components.viewScores.ViewScoresFragment

class MainMenuRobot(private val composeTestRule: ComposeTestRule<MainActivity>) {
    private val scenario = composeTestRule.activityRule.scenario

    init {
        if (!TestUtils.isFragmentShowing(scenario, MainMenuFragment::class)) {
            throw IllegalStateException("Tried to create main menu robot while not on main menu")
        }
    }

    fun clickNewScore() {
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class))
        composeTestRule.onNode(hasTestTag(MainMenuScreen.TestTag.NEW_SCORE)).performClick()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (NewScoreFragment::class))
    }

    fun clickViewScores() {
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class))
        composeTestRule.onNode(hasTestTag(MainMenuScreen.TestTag.VIEW_SCORES)).performClick()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewScoresFragment::class))
    }

    fun checkExitDialogShowing() {
        composeTestRule.onNode(hasText("Going so soon?")).assertIsDisplayed()
    }

    fun clickCancelOnExitDialog() {
        composeTestRule.onNode(hasTestTag(SimpleAlertDialogTestTag.NEGATIVE_BUTTON)).performClick()
    }
}