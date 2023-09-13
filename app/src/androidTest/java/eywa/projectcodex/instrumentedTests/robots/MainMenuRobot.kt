package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.mainMenu.MainMenuTestTag
import eywa.projectcodex.core.mainActivity.MainActivity

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

    fun clickClassificationTables(block: ClassificationTablesRobot.() -> Unit = {}) {
        clickElement(MainMenuTestTag.CLASSIFICATION_TABLES_BUTTON)
        ClassificationTablesRobot(composeTestRule).apply { block() }
    }

    fun clickSightMarks(): SightMarksRobot {
        clickElement(MainMenuTestTag.SIGHT_MARKS_BUTTON)
        return SightMarksRobot(composeTestRule, this)
    }

    fun clickArcherInfo(block: ArcherInfoRobot.() -> Unit = {}) {
        clickElement(MainMenuTestTag.ARCHER_INFO_BUTTON)
        ArcherInfoRobot(composeTestRule).apply { block() }
    }

    fun clickAboutIcon(block: AboutRobot.() -> Unit) {
        clickElement(MainMenuTestTag.ABOUT_BUTTON)
        AboutRobot(composeTestRule).apply(block)
    }

    fun clickCancelWhatsNewDialog() {
        clickDialogCancel("What's new")
    }

    fun clickCancelOnExitDialog() {
        clickDialogCancel("Going so soon?")
    }
}
