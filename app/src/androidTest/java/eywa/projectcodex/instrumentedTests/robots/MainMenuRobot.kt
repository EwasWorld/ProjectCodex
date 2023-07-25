package eywa.projectcodex.instrumentedTests.robots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.components.mainMenu.MainMenuTestTag

class MainMenuRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, MainMenuTestTag.SCREEN) {
    fun clickNewScore(block: NewScoreRobot.() -> Unit = {}) {
        clickElement(MainMenuTestTag.NEW_SCORE_BUTTON)
        NewScoreRobot(composeTestRule).apply { block() }
    }

    fun clickViewScores(block: ViewScoresRobot.() -> Unit = {}) {
        clickElement(MainMenuTestTag.VIEW_SCORE_BUTTON)
        ViewScoresRobot(composeTestRule).apply { block() }
    }

    fun clickHandicapTables(block: HandicapTablesRobot.() -> Unit = {}) {
        clickElement(MainMenuTestTag.HANDICAP_TABLES_BUTTON)
        HandicapTablesRobot(composeTestRule).apply { block() }
    }

    fun clickSightMarks(): SightMarksRobot {
        clickElement(MainMenuTestTag.SIGHT_MARKS_BUTTON)
        return SightMarksRobot(composeTestRule, this)
    }

    fun clickAboutIcon(block: AboutRobot.() -> Unit) {
        clickElement(MainMenuTestTag.ABOUT_BUTTON)
        AboutRobot(composeTestRule).apply(block)
    }

    fun clickCancelOnExitDialog() {
        composeTestRule
                .onNode(hasTestTag(SimpleDialogTestTag.TITLE).and(hasText("Going so soon?")))
                .assertIsDisplayed()
        composeTestRule.onNode(hasTestTag(SimpleDialogTestTag.NEGATIVE_BUTTON)).performClick()
    }
}
