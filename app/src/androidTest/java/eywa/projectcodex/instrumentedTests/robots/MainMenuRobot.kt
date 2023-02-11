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

class MainMenuRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, MainMenuFragment::class) {
    init {
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class))
    }

    fun clickNewScore(block: NewScoreRobot.() -> Unit = {}) {
        clickElement(MainMenuScreen.TestTag.NEW_SCORE)
        NewScoreRobot(composeTestRule).apply { block() }
    }

    fun clickViewScores(block: ViewScoresRobot.() -> Unit = {}) {
        clickElement(MainMenuScreen.TestTag.VIEW_SCORES)
        ViewScoresRobot(composeTestRule).apply { block() }
    }

    fun clickHandicapTables(block: HandicapTablesRobot.() -> Unit = {}) {
        clickElement(MainMenuScreen.TestTag.HANDICAP_TABLES)
        HandicapTablesRobot(composeTestRule).apply { block() }
    }

    fun clickCancelOnExitDialog() {
        composeTestRule.onNode(
                hasTestTag(SimpleDialogTestTag.TITLE)
                        .and(hasText("Going so soon?"))
        ).assertIsDisplayed()
        composeTestRule.onNode(hasTestTag(SimpleDialogTestTag.NEGATIVE_BUTTON)).performClick()
    }
}
