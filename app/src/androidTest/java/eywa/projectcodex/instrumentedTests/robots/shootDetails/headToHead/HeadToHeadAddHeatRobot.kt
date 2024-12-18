package eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat.HeadToHeadAddHeatTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.common.SightMarkIndicatorRobot

class HeadToHeadAddHeatRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, HeadToHeadAddHeatTestTag.SCREEN) {
    val sightMarkIndicatorRobot = SightMarkIndicatorRobot(this)

    fun checkPrevious(heat: Int, score: String, result: String) {
        TODO()
    }

    fun selectHeat(heat: Int) {
        TODO("Click, dialog, and check")
    }

    fun checkHeat(heat: Int) {
        TODO()
    }

    fun checkOpponentRank(rank: Int?) {
        TODO()
    }

    fun checkOpponentRankIsError() {
        TODO()
    }

    fun setIsBye(newValue: Boolean) {
        TODO()
    }

    fun checkIsBye(isBye: Boolean) {
        TODO()
    }

    fun checkSightMark() {
        TODO()
    }

    fun setOpponent(name: String?, rank: Int?) {
        TODO()
    }

    fun clickStartMatch(block: HeadToHeadAddEndRobot.() -> Unit = {}) {
        // clickElement()
        TODO()
        createRobot(HeadToHeadAddEndRobot::class, block)
    }
}
