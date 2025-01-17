package eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.headToHeadEnd.stats.ui.HeadToHeadStatsTestTag
import eywa.projectcodex.components.shootDetails.stats.ui.StatsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.robots.RobotDslMarker
import eywa.projectcodex.instrumentedTests.robots.common.PerformFn
import eywa.projectcodex.instrumentedTests.robots.selectFace.SelectFaceBaseRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.common.HandicapAndClassificationSectionRobot

class HeadToHeadStatsRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, HeadToHeadStatsTestTag.SCREEN) {
    private val facesRobot = SelectFaceBaseRobot(::perform)
    val handicapAndClassificationRobot = HandicapAndClassificationSectionRobot(composeTestRule, screenTestTag)

    fun checkNoMatches() {
        checkElementIsDisplayed(HeadToHeadStatsTestTag.NO_HEATS_TEXT)
    }

    fun checkDate(text: String) {
        checkElementText(StatsTestTag.DATE_TEXT, text, true)
    }

    fun checkRound(text: String) {
        checkElementText(StatsTestTag.ROUND_TEXT, text, true)
    }

    fun checkH2hInfo(text: String) {
        checkElementText(StatsTestTag.ROUND_H2H_INFO_TEXT, text, true)
    }

    fun checkMatchRow(rowIndex: Int, match: String, opponent: String, rank: String, result: String) {
        TODO()
    }

    fun checkNumbersBreakdownNotShown() {
        checkElementDoesNotExist(HeadToHeadStatsTestTag.NUMBERS_BREAKDOWN_TABLE)
        checkElementDoesNotExist(HeadToHeadStatsTestTag.NO_NUMBERS_BREAKDOWN_TEXT)
    }

    fun checkNumbersBreakdownNoData() {
        checkElementDoesNotExist(HeadToHeadStatsTestTag.NUMBERS_BREAKDOWN_TABLE)
        checkElementIsDisplayed(HeadToHeadStatsTestTag.NO_NUMBERS_BREAKDOWN_TEXT)
    }

    fun checkNumbersBreakdown(
            handicapType: NumbersBreakdownRobot.HandicapType,
            columns: List<NumbersBreakdownRobot.Column>,
            block: NumbersBreakdownRobot.() -> Unit,
    ) {
        TODO()
    }

    fun checkFaces(expectedFacesString: String) {
        facesRobot.checkFaces(expectedFacesString)
    }

    @RobotDslMarker
    class NumbersBreakdownRobot(
            private val performFn: PerformFn,
            val handicapType: HandicapType,
            columns: List<Column>,
    ) {
        init {
            checkColumns(*columns.toTypedArray())
        }

        fun checkColumns(vararg column: Column) {
            TODO("Check columns exist and others don't")
        }

        fun checkRow(rowIndex: Int, match: String, handicap: Float?) {
//            if (handicapType == null) {
//
//            }
//            else {
//
//            }
            TODO()
        }

        fun checkEndAverages(rowIndex: Int, vararg values: Pair<Column, Float?>) {
            TODO()
        }

        fun checkArrowAverages(rowIndex: Int, vararg values: Pair<Column, Float?>) {
            TODO()
        }

        enum class Column { SELF, OPPONENT, TEAM, DIFFERENCE }
        enum class HandicapType { SELF, TEAM }
    }
}
