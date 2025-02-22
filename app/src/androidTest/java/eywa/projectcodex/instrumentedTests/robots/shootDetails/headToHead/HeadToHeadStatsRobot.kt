package eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.headToHead.stats.ui.HeadToHeadStatsTestTag
import eywa.projectcodex.components.shootDetails.stats.ui.StatsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.NewScoreRobot
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
        checkElementIsDisplayed(HeadToHeadStatsTestTag.NO_MATCHES_TEXT)
    }

    fun checkDate(text: String) {
        checkElementText(StatsTestTag.DATE_TEXT, text, true)
    }

    fun checkRound(text: String) {
        checkElementText(StatsTestTag.ROUND_TEXT, text ?: "N/A", true)
    }

    fun checkH2hInfo(text: String) {
        checkElementText(StatsTestTag.ROUND_H2H_INFO_TEXT, text, true)
    }

    private fun checkMatchCell(testTag: HeadToHeadStatsTestTag, rowIndex: Int, text: String) {
        performGroup {
            +CodexNodeMatcher.HasTestTag(testTag)
            toSingle(CodexNodeGroupToOne.Index(rowIndex)) {
                +CodexNodeInteraction.AssertTextEquals(text).waitFor()
            }
        }
    }

    fun checkMatchRow(rowIndex: Int, match: String, opponent: String, rank: String, result: String) {
        checkMatchCell(HeadToHeadStatsTestTag.MATCHES_TABLE_MATCH_CELL, rowIndex, match)
        checkMatchCell(HeadToHeadStatsTestTag.MATCHES_TABLE_OPPONENT_CELL, rowIndex, opponent)
        checkMatchCell(HeadToHeadStatsTestTag.MATCHES_TABLE_RANK_CELL, rowIndex, rank)
        checkMatchCell(HeadToHeadStatsTestTag.MATCHES_TABLE_RESULT_CELL, rowIndex, result)
    }

    fun checkNumbersBreakdownNotShown() {
        checkElementDoesNotExist(HeadToHeadStatsTestTag.NUMBERS_BREAKDOWN_TABLE)
        checkElementDoesNotExist(HeadToHeadStatsTestTag.NO_NUMBERS_BREAKDOWN_TEXT)
    }

    fun checkNumbersBreakdownNoData() {
        checkElementDoesNotExist(HeadToHeadStatsTestTag.NUMBERS_BREAKDOWN_TABLE)
        checkElementIsDisplayed(HeadToHeadStatsTestTag.NO_NUMBERS_BREAKDOWN_TEXT)
    }

    fun clickEditMainInfo(block: NewScoreRobot.() -> Unit) {
        clickElement(StatsTestTag.EDIT_SHOOT_INFO, useUnmergedTree = true)
        createRobot(NewScoreRobot::class, block)
    }

    fun checkNumbersBreakdown(
            rowCount: Int,
            handicapType: NumbersBreakdownRobot.HandicapType,
            columns: List<NumbersBreakdownRobot.Column>,
            block: NumbersBreakdownRobot.() -> Unit,
    ) {
        performGroup {
            +CodexNodeMatcher.HasTestTag(HeadToHeadStatsTestTag.NUMBERS_BREAKDOWN_TABLE_MATCH_CELL)
            +CodexNodeGroupInteraction.AssertCount(rowCount)
        }
        NumbersBreakdownRobot(::perform, handicapType, columns).apply(block)
    }

    fun checkFaces(expectedFacesString: String) {
        facesRobot.checkFaces(expectedFacesString)
    }

    @RobotDslMarker
    class NumbersBreakdownRobot(
            private val performFn: PerformFn,
            private val handicapType: HandicapType,
            columns: List<Column>,
    ) {
        init {
            check(columns.isNotEmpty())
            checkColumns(*columns.toTypedArray())
        }

        private fun checkColumns(vararg columns: Column) {
            Column.entries.forEach { column ->
                listOf(getArrowAverageTestTag(column), getEndAverageTestTag(column)).forEach {
                    if (columns.contains(column)) {
                        performFn {
                            allNodes {
                                +CodexNodeMatcher.HasTestTag(it)
                                +CodexNodeMatcher.IsNotCached
                                toSingle(CodexNodeGroupToOne.First) {
                                    +CodexNodeInteraction.PerformScrollTo()
                                    +CodexNodeInteraction.AssertIsDisplayed().waitFor()
                                }
                            }
                        }
                    }
                    else {
                        performFn {
                            singleNode {
                                +CodexNodeMatcher.HasTestTag(it)
                                +CodexNodeMatcher.IsNotCached
                                +CodexNodeInteraction.AssertDoesNotExist().waitFor()
                            }
                        }
                    }
                }
            }
        }

        fun checkRow(rowIndex: Int, match: String, handicap: Float?) {
            performFn {
                allNodes {
                    +CodexNodeMatcher.HasTestTag(HeadToHeadStatsTestTag.NUMBERS_BREAKDOWN_TABLE_MATCH_CELL)
                    toSingle(CodexNodeGroupToOne.Index(rowIndex)) {
                        +CodexNodeInteraction.AssertTextEquals(match).waitFor()
                    }
                }
            }

            val handicapColumn =
                    if (handicap == null) null
                    else if (handicapType == HandicapType.SELF) HeadToHeadStatsTestTag.NUMBERS_BREAKDOWN_TABLE_SELF_HC_CELL
                    else HeadToHeadStatsTestTag.NUMBERS_BREAKDOWN_TABLE_TEAM_HC_CELL

            if (handicapColumn != null) {
                performFn {
                    allNodes {
                        +CodexNodeMatcher.HasTestTag(handicapColumn)
                        toSingle(CodexNodeGroupToOne.Index(rowIndex)) {
                            +CodexNodeInteraction.AssertTextEquals(handicap!!.toString()).waitFor()
                        }
                    }
                }
            }

            listOf(
                    HeadToHeadStatsTestTag.NUMBERS_BREAKDOWN_TABLE_SELF_HC_CELL,
                    HeadToHeadStatsTestTag.NUMBERS_BREAKDOWN_TABLE_TEAM_HC_CELL,
            ).filter { it != handicapColumn }.forEach {
                performFn {
                    singleNode {
                        +CodexNodeMatcher.HasTestTag(it)
                        +CodexNodeInteraction.AssertDoesNotExist()
                    }
                }
            }
        }

        fun checkEndAverages(rowIndex: Int, vararg values: Pair<Column, Float?>) {
            values.forEach { (column, value) ->
                checkValue(getEndAverageTestTag(column), rowIndex, value)
            }
        }

        fun checkArrowAverages(rowIndex: Int, vararg values: Pair<Column, Float?>) {
            values.forEach { (column, value) ->
                checkValue(getArrowAverageTestTag(column), rowIndex, value)
            }
        }

        private fun checkValue(testTag: HeadToHeadStatsTestTag, rowIndex: Int, value: Float?) {
            performFn {
                allNodes {
                    +CodexNodeMatcher.HasTestTag(testTag)
                    toSingle(CodexNodeGroupToOne.Index(rowIndex)) {
                        +CodexNodeInteraction.AssertTextEquals(value?.toString() ?: "-").waitFor()
                    }
                }
            }
        }

        private fun getEndAverageTestTag(column: Column) = when (column) {
            Column.SELF -> HeadToHeadStatsTestTag.NUMBERS_BREAKDOWN_TABLE_SELF_END_AVG_CELL
            Column.OPPONENT -> HeadToHeadStatsTestTag.NUMBERS_BREAKDOWN_TABLE_OPPONENT_END_AVG_CELL
            Column.TEAM -> HeadToHeadStatsTestTag.NUMBERS_BREAKDOWN_TABLE_TEAM_END_AVG_CELL
            Column.DIFFERENCE -> HeadToHeadStatsTestTag.NUMBERS_BREAKDOWN_TABLE_DIFF_END_AVG_CELL
        }

        private fun getArrowAverageTestTag(column: Column) = when (column) {
            Column.SELF -> HeadToHeadStatsTestTag.NUMBERS_BREAKDOWN_TABLE_SELF_ARROW_AVG_CELL
            Column.OPPONENT -> HeadToHeadStatsTestTag.NUMBERS_BREAKDOWN_TABLE_OPPONENT_ARROW_AVG_CELL
            Column.TEAM -> HeadToHeadStatsTestTag.NUMBERS_BREAKDOWN_TABLE_TEAM_ARROW_AVG_CELL
            Column.DIFFERENCE -> HeadToHeadStatsTestTag.NUMBERS_BREAKDOWN_TABLE_DIFF_ARROW_AVG_CELL
        }

        enum class Column { SELF, OPPONENT, TEAM, DIFFERENCE }
        enum class HandicapType { SELF, TEAM }
    }
}
