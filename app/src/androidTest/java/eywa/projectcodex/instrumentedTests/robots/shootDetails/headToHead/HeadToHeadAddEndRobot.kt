package eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd.HeadToHeadAddEndTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.robots.RobotDslMarker
import eywa.projectcodex.instrumentedTests.robots.common.PerformFnV2
import eywa.projectcodex.instrumentedTests.robots.shootDetails.AddCountRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.common.SightMarkIndicatorRobot

class HeadToHeadAddEndRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, HeadToHeadAddEndTestTag.SCREEN) {
    val sightMarkIndicatorRobot = SightMarkIndicatorRobot(this)

    fun checkRows(vararg rowsToIsTotal: Pair<String, Boolean>) {
        TODO()
    }

    fun setArrowRow(rowIndex: Int, arrows: List<Int>) {
        TODO("Set and check row totals")
    }

    fun setTotalRow(rowIndex: Int, total: Int) {
        TODO("Set and check row totals")
    }

    fun clickResultRow(rowIndex: Int, newValue: String) {
        TODO("Set and check row totals")
    }

    fun checkArrowRow(rowIndex: Int, arrows: List<Int>) {
        TODO("Set and check row totals")
    }

    fun checkTotalRow(rowIndex: Int, total: Int) {
        TODO("Set and check row totals")
    }

    fun checkResultRow(rowIndex: Int, result: String) {
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

    fun checkNoRunningTotals() {
        TODO()
    }

    fun checkRunningTotals(team: Int, opponent: Int) {
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

    fun checkSighters(sighters: Int) {
        TODO()
    }

    fun clickSighters(block: AddCountRobot.() -> Unit) {
        TODO()
        createRobot(AddCountRobot::class, block)
    }

    fun clickEditRows(block: EditRowsDialogRobot.() -> Unit) {
        TODO()
        EditRowsDialogRobot(::performV2).apply(block)
    }

    fun clickConfirmEdit() {
        TODO()
    }

    fun checkMatchComplete() {
        TODO()
    }

    @RobotDslMarker
    class EditRowsDialogRobot(private val performFn: PerformFnV2) {
        fun checkEditRowsDialog(vararg rowsToValue: Pair<String, String>) {
            TODO()
        }

        fun clickEditRowsDialogRow(row: String) {
            TODO()
        }

        fun checkEditRowsDialogUnknownResultWarningShown(shown: Boolean = true) {
            TODO()
        }

        fun clickCancel() {
            TODO()
        }

        fun clickOk() {
            TODO()
        }
    }
}
