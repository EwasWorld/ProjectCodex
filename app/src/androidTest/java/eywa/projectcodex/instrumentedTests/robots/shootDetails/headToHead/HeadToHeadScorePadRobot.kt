package eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.scorePad.HeadToHeadScorePadTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.robots.RobotDslMarker
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsRobot

class HeadToHeadScorePadRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, HeadToHeadScorePadTestTag.SCREEN) {
    fun checkEmpty() {
        TODO()
    }

    fun clickEmptyScreenAddMatchButton() {
        checkEmpty()
        TODO()
    }

    fun checkMatchIsBye(match: Int) {
        TODO()
    }

    fun checkMatchDetails(match: Int, heat: Int, sighters: Int, opponentName: String?, opponentRank: Int?) {
        TODO()
    }

    fun checkNoGrid(match: Int) {
        TODO()
    }

    fun checkGrid(match: Int, result: HeadToHeadResult, config: GridDsl.() -> Unit) {
        TODO()
    }

    fun openEditEnd(match: Int, endNumber: Int, block: HeadToHeadAddEndRobot.() -> Unit) {
        TODO()
        createRobot(HeadToHeadAddEndRobot::class, block)
    }
}

@RobotDslMarker
class GridDsl {
    fun checkEnd(
            endNumber: Int,
            result: HeadToHeadResult,
            runningTotal: String,
            config: GridEndDsl.() -> Unit,
    ) {
        TODO()
    }
}

@RobotDslMarker
class GridEndDsl {
    fun checkRow(
            rowIndex: Int,
            type: String,
            arrows: String?,
            score: Int,
            teamScore: Int?,
            points: Int?,
    ) {
        TODO()
    }

    fun checkResultsRow(rowIndex: Int, result: String, points: Int?) {
        TODO()
    }

    companion object {
        const val DEFAULT_ARCHER_NAME = "Self"
        const val DEFAULT_OPPONENT_NAME = "Opponent"
    }
}
