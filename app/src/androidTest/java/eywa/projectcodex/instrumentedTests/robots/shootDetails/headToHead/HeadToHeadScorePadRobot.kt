package eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGridColumnTestTag
import eywa.projectcodex.components.shootDetails.headToHead.scorePad.HeadToHeadScorePadMatchTestTag
import eywa.projectcodex.components.shootDetails.headToHead.scorePad.HeadToHeadScorePadTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.dsl.TestActionDslSingleNode
import eywa.projectcodex.instrumentedTests.robots.BaseRobot
import eywa.projectcodex.instrumentedTests.robots.RobotDslMarker
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsRobot

class HeadToHeadScorePadRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, HeadToHeadScorePadTestTag.SCREEN) {
    fun checkEmpty() {
        checkElementIsDisplayed(HeadToHeadScorePadTestTag.ADD_MATCH_BUTTON)
    }

    fun clickEmptyScreenAddMatchButton() {
        clickElement(HeadToHeadScorePadTestTag.ADD_MATCH_BUTTON)
    }

    fun checkMaxRank(match: Int, rank: Int?) {
        if (rank == null) {
            checkElementDoesNotExist(HeadToHeadScorePadMatchTestTag.MAX_RANK.getTestTag(match))
        }
        else {
            val text = when (rank) {
                1 -> "Gold"
                2 -> "Silver"
                3 -> "Bronze"
                else -> rank.toString()
            }
            checkDataRowValueText(HeadToHeadScorePadMatchTestTag.MAX_RANK.getTestTag(match), text)
        }
    }

    fun checkMatchIsBye(match: Int) {
        checkElementText(HeadToHeadScorePadMatchTestTag.NO_ENDS.getTestTag(match), "Bye")
    }

    fun checkMatchDetails(match: Int, heat: String?, sighters: Int, opponentName: String?, opponentRank: Int?) {
        if (heat == null) {
            checkElementText(
                    testTag = HeadToHeadScorePadMatchTestTag.MATCH_TEXT.getTestTag(match),
                    text = "Match $match",
            )
        }
        else {
            checkDataRow(
                    testTag = HeadToHeadScorePadMatchTestTag.MATCH_TEXT.getTestTag(match),
                    title = "Match $match:",
                    text = heat,
            )
        }

        checkDataRowValueText(
                testTag = HeadToHeadScorePadMatchTestTag.SIGHTERS.getTestTag(match),
                text = sighters.toString(),
        )

        if (opponentName == null && opponentRank == null) {
            checkElementDoesNotExist(HeadToHeadScorePadMatchTestTag.OPPONENT_TEXT.getTestTag(match))
        }
        else {
            val opponentString =
                    if (opponentRank == null) "Opponent: $opponentName"
                    else if (opponentName == null) "Opponent (rank $opponentRank)"
                    else "Opponent (rank $opponentRank): $opponentName"
            checkElementText(
                    testTag = HeadToHeadScorePadMatchTestTag.OPPONENT_TEXT.getTestTag(match),
                    text = opponentString,
            )
        }
    }

    fun checkNoGrid(match: Int) {
        checkElementDoesNotExist(HeadToHeadScorePadMatchTestTag.GRID.getTestTag(match))
    }

    fun checkGrid(match: Int, result: HeadToHeadResult, config: GridDsl.() -> Unit) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(HeadToHeadScorePadTestTag.SCREEN)
            +CodexNodeInteraction.PerformScrollToNode(
                    listOf(CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.TYPE_CELL.get(match, 1)))
            )
        }
        checkDataRowValueText(HeadToHeadGridColumnTestTag.MATCH_RESULT.get(match, 1), result.asString())
        GridDsl(match, this).apply(config)
    }

    fun openEditEnd(match: Int, setNumber: Int, block: HeadToHeadAddEndRobot.() -> Unit) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(HeadToHeadScorePadTestTag.SCREEN)
            +CodexNodeInteraction.PerformScrollToNode(
                    listOf(CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.TYPE_CELL.get(match, setNumber)))
            )
        }
        performGroup {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.TYPE_CELL.get(match, setNumber))
            toSingle(CodexNodeGroupToOne.First) {
                +CodexNodeInteraction.PerformClick()
            }
        }
        createRobot(HeadToHeadAddEndRobot::class, block)
    }
}

@RobotDslMarker
class GridDsl(
        private val match: Int,
        private val robot: BaseRobot,
) {
    fun checkEnd(
            setNumber: Int,
            result: HeadToHeadResult,
            runningTotal: String,
            rowCount: Int,
            config: GridSetDsl.() -> Unit,
    ) {
        robot.checkDataRowValueText(HeadToHeadGridColumnTestTag.SET_RESULT.get(match, setNumber), result.asString())
        robot.checkDataRowValueText(HeadToHeadGridColumnTestTag.SET_RUNNING_TOTAL.get(match, setNumber), runningTotal)
        robot.performGroup {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.END_TOTAL_CELL.get(match, setNumber))
            +CodexNodeMatcher.IsNotCached
            +CodexNodeGroupInteraction.AssertCount(rowCount).waitFor()
        }
        GridSetDsl(match, setNumber, robot).apply(config)
    }
}

@RobotDslMarker
class GridSetDsl(
        private val match: Int,
        private val setNumber: Int,
        private val robot: BaseRobot,
) {
    private fun performOn(
            rowIndex: Int,
            testTag: HeadToHeadGridColumnTestTag,
            config: TestActionDslSingleNode.() -> Unit
    ) {
        robot.performGroup {
            +CodexNodeMatcher.HasTestTag(testTag.get(match, setNumber))
            +CodexNodeMatcher.IsNotCached
            toSingle(CodexNodeGroupToOne.Index(rowIndex)) {
                config()
            }
        }
    }

    private fun checkCell(rowIndex: Int, testTag: HeadToHeadGridColumnTestTag, content: CellValue) {
        if (content is CellValue.NoColumn) {
            robot.performSingle {
                +CodexNodeMatcher.HasTestTag(testTag.get(match, setNumber))
                +CodexNodeInteraction.AssertDoesNotExist()
            }
        }
        else {
            performOn(rowIndex, testTag) {
                +CodexNodeInteraction.AssertTextEquals(if (content is CellValue.Value) content.value else "-")
            }
        }
    }

    fun checkRow(
            rowIndex: Int,
            type: String,
            arrows: CellValue,
            score: Int,
            teamScore: CellValue,
            points: CellValue = CellValue.NoColumn,
            isEditable: Boolean = false,
    ) {
        performOn(rowIndex, HeadToHeadGridColumnTestTag.TYPE_CELL) {
            +CodexNodeInteraction.AssertTextEquals(type)
        }

        robot.performGroup {
            val matcher = CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.END_TOTAL_CELL.get(match, setNumber))
            if (isEditable) +CodexNodeMatcher.HasAnyAncestor(matcher) else +matcher
            toSingle(CodexNodeGroupToOne.Index(rowIndex)) {
                +CodexNodeInteraction.AssertTextEquals(score.toString()).waitFor()
            }
        }

        checkCell(rowIndex, HeadToHeadGridColumnTestTag.ARROW_CELL, arrows)
        checkCell(rowIndex, HeadToHeadGridColumnTestTag.TEAM_TOTAL_CELL, teamScore)
        checkCell(rowIndex, HeadToHeadGridColumnTestTag.POINTS_CELL, points)
    }

    fun checkResultsRow(rowIndex: Int, result: String, points: CellValue) {
        performOn(rowIndex, HeadToHeadGridColumnTestTag.TYPE_CELL) {
            +CodexNodeInteraction.AssertTextEquals("Result")
        }
        performOn(rowIndex, HeadToHeadGridColumnTestTag.END_TOTAL_CELL) {
            +CodexNodeInteraction.AssertTextEquals(result)
        }
        checkCell(rowIndex, HeadToHeadGridColumnTestTag.POINTS_CELL, points)
    }

    sealed class CellValue {
        data object NoColumn : CellValue()
        data object Empty : CellValue()
        data class Value(val value: String) : CellValue() {
            constructor(value: Int) : this(value.toString())
        }
    }

    companion object {
        const val DEFAULT_ARCHER_NAME = "Self"
        const val DEFAULT_OPPONENT_NAME = "Opponent"
    }
}
