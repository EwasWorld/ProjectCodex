package eywa.projectcodex.instrumentedTests.robots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.components.mainMenu.MainMenuFragment
import eywa.projectcodex.components.mainMenu.MainMenuScreen
import eywa.projectcodex.components.newScore.NewScoreFragment
import eywa.projectcodex.components.viewScores.ViewScoresFragment

class MainMenuRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, MainMenuFragment::class) {

    fun clickNewScore(block: NewScoreRobot.() -> Unit = {}) {
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class))
        composeTestRule.onNode(hasTestTag(MainMenuScreen.TestTag.NEW_SCORE)).performClick()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (NewScoreFragment::class))
        NewScoreRobot(composeTestRule).apply { block() }
    }

    fun clickViewScores(block: ViewScoresRobot.() -> Unit = {}) {
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class))
        composeTestRule.onNode(hasTestTag(MainMenuScreen.TestTag.VIEW_SCORES)).performClick()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (ViewScoresFragment::class))
        ViewScoresRobot(composeTestRule).apply { block() }
    }

    fun clickCancelOnExitDialog() {
        composeTestRule.onNode(
                hasTestTag(SimpleDialogTestTag.TITLE)
                        .and(hasText("Going so soon?"))
        ).assertIsDisplayed()
        composeTestRule.onNode(hasTestTag(SimpleDialogTestTag.NEGATIVE_BUTTON)).performClick()
    }
}