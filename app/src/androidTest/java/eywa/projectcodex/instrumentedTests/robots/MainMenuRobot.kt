package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.mainMenu.MainMenuTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.robots.referenceTables.HandicapTablesRobot

class MainMenuRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, MainMenuTestTag.SCREEN) {
    fun clickNewScore(block: NewScoreRobot.() -> Unit) {
        clickElement(MainMenuTestTag.NEW_SCORE_BUTTON)
        createRobot(NewScoreRobot::class, block)
    }

    fun clickViewScores(block: ViewScoresRobot.() -> Unit) {
        clickElement(MainMenuTestTag.VIEW_SCORE_BUTTON)
        createRobot(ViewScoresRobot::class, block)
    }

    fun clickHandicapTables(block: HandicapTablesRobot.() -> Unit) {
        clickElement(MainMenuTestTag.REFERENCE_TABLES_BUTTON)
        createRobot(HandicapTablesRobot::class, block)
    }

    fun clickSightMarks(block: SightMarksRobot.() -> Unit) {
        clickElement(MainMenuTestTag.SIGHT_MARKS_BUTTON)
        createRobot(SightMarksRobot::class, block)
    }

    fun clickArcherInfo(block: ArcherInfoRobot.() -> Unit) {
        clickElement(MainMenuTestTag.ARCHER_INFO_BUTTON)
        createRobot(ArcherInfoRobot::class, block)
    }

    fun clickAboutIcon(block: AboutRobot.() -> Unit) {
        clickElement(MainMenuTestTag.ABOUT_BUTTON)
        createRobot(AboutRobot::class, block)
    }

    fun clickCancelWhatsNewDialog() {
        clickDialogCancel("What's new")
    }

    fun clickCancelOnExitDialog() {
        clickDialogCancel("Going so soon?")
    }
}
