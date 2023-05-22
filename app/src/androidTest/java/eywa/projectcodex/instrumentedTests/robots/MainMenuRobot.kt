package eywa.projectcodex.instrumentedTests.robots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.components.mainMenu.MainMenuScreen

class MainMenuRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, MainMenuScreen.TestTag.SCREEN) {
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

    fun clickSightMarks(block: SightMarksRobot.() -> Unit = {}) {
        clickElement(MainMenuScreen.TestTag.SIGHT_MARKS)
        SightMarksRobot(composeTestRule).apply { block() }
    }

    fun clickAboutIcon(block: AboutRobot.() -> Unit) {
        clickElement(MainMenuScreen.TestTag.ABOUT)
        AboutRobot(composeTestRule).apply(block)
    }

    fun clickCancelOnExitDialog() {
        composeTestRule
                .onNode(hasTestTag(SimpleDialogTestTag.TITLE).and(hasText("Going so soon?")))
                .assertIsDisplayed()
        composeTestRule.onNode(hasTestTag(SimpleDialogTestTag.NEGATIVE_BUTTON)).performClick()
    }
}
