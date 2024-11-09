package eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd.HeadToHeadAddEndTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsRobot

class HeadToHeadAddEndRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, HeadToHeadAddEndTestTag.SCREEN) {
    fun setArrowRow(rowIndex: Int, arrows: List<Int>) {
        TODO("Set and check row totals")
    }

    fun setTotalRow(rowIndex: Int, total: Int) {
        TODO("Set and check row totals")
    }

    fun clickNextEnd() {
        TODO("Check still on addEnd screen and not heat")
    }

    fun clickNextEnd(block: HeadToHeadAddHeatRobot.() -> Unit) {
        TODO()
//        clickElement(NewScoreTestTag.SUBMIT_BUTTON)
        createRobot(HeadToHeadAddHeatRobot::class, block)
    }

    fun checkArrowRowError(rowIndex: Int, error: String?) {
        TODO()
    }

    fun checkTotalRowError(rowIndex: Int, error: String?) {
        TODO()
    }

    fun checkSetResult(result: HeadToHeadResult) {
        TODO()
    }

    fun checkRunningTotals(team: Int, opponent: Int) {
        TODO()
    }

    fun checkSightMark() {
        TODO()
    }

    fun checkOpponent(name: String?, rank: Int?) {
        TODO()
    }

    fun checkShootOffWin(isWin: Boolean) {
        TODO()
    }

    fun tapIsShootOffWin(newValue: Boolean) {
        TODO()
    }
}
