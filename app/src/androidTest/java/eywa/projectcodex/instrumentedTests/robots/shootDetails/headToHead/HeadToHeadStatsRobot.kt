package eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.headToHeadEnd.stats.ui.HeadToHeadStatsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.robots.selectFace.SelectFaceBaseRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsRobot

class HeadToHeadStatsRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, HeadToHeadStatsTestTag.SCREEN) {
    private val facesRobot = SelectFaceBaseRobot(::performV2)

    fun checkNoHeats() {
        TODO()
    }

    fun checkDate(text: String) {
//        checkElementText(StatsTestTag.DATE_TEXT, text, true)
        TODO()
    }

    fun checkRound(round: String?) {
//        checkElementText(StatsTestTag.ROUND_TEXT, text ?: "N/A", true)
        TODO()
    }

    fun checkH2hInfo(info: String) {
//        checkElementText(StatsTestTag.ROUND_TEXT, text ?: "N/A", true)
        TODO()
    }

    fun checkFaces(expectedFacesString: String) {
        facesRobot.checkFaces(expectedFacesString)
    }
}
