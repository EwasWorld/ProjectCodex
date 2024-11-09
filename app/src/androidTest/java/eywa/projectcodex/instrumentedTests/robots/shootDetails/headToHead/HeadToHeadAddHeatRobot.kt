package eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat.HeadToHeadAddHeatTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsRobot

class HeadToHeadAddHeatRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, HeadToHeadAddHeatTestTag.SCREEN) {
    fun checkPrevious(heat: Int, score: String, result: String) {
        TODO()
    }

    fun selectMatch(heat: Int) {
        TODO("Click, dialog, and check")
    }

    fun checkMatch(heat: Int) {
        TODO()
    }

    fun checkMatchNotSelectedIsError() {
        TODO()
    }

    fun checkOpponentRankIsError() {
        TODO()
    }

    fun setIsBye(newValue: Boolean) {
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
