package eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.addEnd.AddEndTestTag
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd.HeadToHeadAddEndTestTag
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridColumnTestTag
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.clickDialogCancel
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.clickDialogOk
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.matchTextBox
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.RobotDslMarker
import eywa.projectcodex.instrumentedTests.robots.common.PerformFnV2
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ArrowInputsRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsAddCountRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.common.SightMarkIndicatorRobot

fun HeadToHeadResult.asString() = when (this) {
    HeadToHeadResult.WIN -> "Win"
    HeadToHeadResult.LOSS -> "Loss"
    HeadToHeadResult.TIE -> "Tie"
    HeadToHeadResult.INCOMPLETE -> "Incomplete"
    HeadToHeadResult.UNKNOWN -> "Unknown"
}

class HeadToHeadAddEndRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ArrowInputsRobot(composeTestRule, HeadToHeadAddEndTestTag.SCREEN) {
    val sightMarkIndicatorRobot = SightMarkIndicatorRobot(this, HeadToHeadAddEndTestTag.SCREEN)
    val gridSetDsl = GridSetDsl(1, 1, this)

    /**
     * Check the correct row names are displayed and that the arrow or end total columns are correctly editable
     */
    fun checkRows(endSize: Int, vararg rowsToIsTotal: Pair<String, Boolean>) {
        performV2Group {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.TYPE_CELL.get(1, 1))
            +CodexNodeMatcher.IsNotCached
            +CodexNodeGroupInteraction.AssertCount(rowsToIsTotal.size).waitFor()
            +CodexNodeGroupInteraction.ForEach(
                    rowsToIsTotal.map { (row, _) -> listOf(CodexNodeInteraction.AssertTextEquals(row)) }
            )
        }

        val hasAnyArrowRow = rowsToIsTotal.any { !it.second }
        if (hasAnyArrowRow) {
            performV2Group {
                +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.ARROW_CELL.get(1, 1))
                +CodexNodeMatcher.IsNotCached
                +CodexNodeGroupInteraction.AssertCount(rowsToIsTotal.size).waitFor()
                +CodexNodeGroupInteraction.ForEach(
                        rowsToIsTotal.map { (row, isTotal) ->
                            if (isTotal) {
                                listOf(CodexNodeInteraction.AssertTextEquals("-"))
                            }
                            else {
                                // 1 to 3 arrows separated by a '-', allow placeholder character '.'
                                val arrowValue = "[m\\dX\\.]"
                                val regex = "$arrowValue(?:-$arrowValue){${endSize - 1}}"
                                listOf(CodexNodeInteraction.AssertTextMatchesRegex(regex))
                            }
                        }
                )
            }
        }
        else {
            performV2Single {
                +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.ARROW_CELL.get(1, 1))
                +CodexNodeMatcher.IsNotCached
                +CodexNodeInteraction.AssertDoesNotExist()
            }
        }

        performV2Group {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.END_TOTAL_CELL.get(1, 1))
            +CodexNodeMatcher.IsNotCached
            +CodexNodeGroupInteraction.AssertCount(rowsToIsTotal.size).waitFor()
            +CodexNodeGroupInteraction.ForEach(
                    rowsToIsTotal.map { (row, isTotal) ->
                        if (isTotal) {
                            val matchers =
                                    listOf(
                                            CodexNodeMatcher.HasTextMatchingRegex("\\d*"),
                                            CodexNodeMatcher.HasSetTextAction,
                                    )
                            listOf(CodexNodeInteraction.Assert(CodexNodeMatcher.HasAnyChild(matchers)))
                        }
                        else {
                            listOf(
                                    CodexNodeInteraction.AssertTextMatchesRegex("\\d*"),
                                    CodexNodeInteraction.AssertFalse(CodexNodeMatcher.HasSetTextAction),
                            )
                        }
                    }
            )
        }
    }

    fun setArrowRow(
            rowIndex: Int,
            type: String,
            arrows: List<String>,
            score: Int,
            teamScore: GridSetDsl.CellValue = GridSetDsl.CellValue.NoColumn,
            points: GridSetDsl.CellValue = GridSetDsl.CellValue.NoColumn,
    ) {
        performV2Group {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.ARROW_CELL.get(1, 1))
            toSingle(CodexNodeGroupToOne.Index(rowIndex)) {
                +CodexNodeInteraction.PerformClick()
            }
        }
        arrows.forEach {
            clickScoreButton(it)
        }

        gridSetDsl.checkRow(
                rowIndex = rowIndex,
                type = type,
                arrows = GridSetDsl.CellValue.Value(arrows.joinToString("-")),
                score = score,
                teamScore = teamScore,
                points = points
        )
    }

    fun setTotalRow(
            rowIndex: Int,
            type: String,
            score: Int,
            arrows: GridSetDsl.CellValue = GridSetDsl.CellValue.Empty,
            teamScore: GridSetDsl.CellValue = GridSetDsl.CellValue.NoColumn,
            points: GridSetDsl.CellValue = GridSetDsl.CellValue.NoColumn,
    ) {
        performV2Group {
            useUnmergedTree()
            matchTextBox(HeadToHeadGridColumnTestTag.END_TOTAL_CELL.get(1, 1))
            toSingle(CodexNodeGroupToOne.Index(rowIndex)) {
                +CodexNodeInteraction.SetText(score.toString()).waitFor()
            }
        }

        gridSetDsl.checkRow(
                rowIndex = rowIndex,
                type = type,
                arrows = arrows,
                score = score,
                teamScore = teamScore,
                points = points,
                isEndTotalEditable = true,
        )
    }

    fun clickResultRow(rowIndex: Int, newValue: HeadToHeadResult, points: Int? = null) {
        clickElement(HeadToHeadGridColumnTestTag.END_TOTAL_CELL.get(1, 1))
        gridSetDsl.checkResultsRow(rowIndex, newValue.asString(), points)
    }

    fun checkResultRow(rowIndex: Int, result: HeadToHeadResult, points: Int? = null) {
        gridSetDsl.checkResultsRow(rowIndex, result.asString(), points)
    }

    /**
     * Should stay on add end screen, use overload for jump to add heat screen
     */
    fun clickNextEnd() {
        clickElement(HeadToHeadAddEndTestTag.NEXT_END_BUTTON)
        checkScreenIsShown()
    }

    fun clickNextEnd(block: HeadToHeadAddHeatRobot.() -> Unit) {
        clickElement(HeadToHeadAddEndTestTag.NEXT_END_BUTTON)
        createRobot(HeadToHeadAddHeatRobot::class, block)
    }

    fun checkArrowRowError(rowIndex: Int, error: String?) {
        performV2Group {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.ARROW_CELL.get(1, 1))
            toSingle(CodexNodeGroupToOne.Index(rowIndex)) {
                +CodexNodeInteraction.AssertHasError(error)
            }
        }
    }

    fun checkTotalRowError(rowIndex: Int, error: String?) {
        performV2Group {
            +CodexNodeMatcher.HasAnyAncestor(
                    CodexNodeMatcher.HasTestTag(
                            HeadToHeadGridColumnTestTag.END_TOTAL_CELL.get(
                                    1,
                                    1
                            )
                    )
            )
            toSingle(CodexNodeGroupToOne.Index(rowIndex)) {
                +CodexNodeInteraction.AssertHasError(error)
            }
        }
    }

    fun checkSetResult(set: Int, result: HeadToHeadResult) {
        checkElementText(HeadToHeadAddEndTestTag.SET_RESULT, "Set $set: ${result.asString()}")
    }

    fun checkShootOffSetResult(result: HeadToHeadResult) {
        checkElementText(HeadToHeadAddEndTestTag.SET_RESULT, "Shoot off: ${result.asString()}")
    }

    fun checkNoRunningTotals() {
        checkDataRowValueText(HeadToHeadAddEndTestTag.RUNNING_TOTALS, "Unknown")
    }

    fun checkRunningTotals(team: Int, opponent: Int) {
        checkElementIsDisplayed(HeadToHeadAddEndTestTag.RUNNING_TOTALS)
        checkDataRowValueText(HeadToHeadAddEndTestTag.RUNNING_TOTALS, "$team-$opponent")
    }

    fun checkOpponent(name: String?, rank: Int?) {
        if (name == null && rank == null) {
            checkElementDoesNotExist(HeadToHeadAddEndTestTag.OPPONENT)
            return
        }

        val text = if (rank == null) "Opponent:\n$name"
        else if (name == null) "Opponent:\nrank $rank"
        else "Opponent (rank $rank):\n$name"
        checkElementText(HeadToHeadAddEndTestTag.OPPONENT, text)
    }

    fun checkShootOffWinChip(isWin: Boolean) {
        checkCheckboxState(HeadToHeadAddEndTestTag.IS_SHOOT_OFF_WIN_CHECKBOX, isWin, useUnmergedTree = true)
    }

    fun checkShootOffWinChipNotShown() {
        checkElementDoesNotExist(HeadToHeadAddEndTestTag.IS_SHOOT_OFF_WIN_CHECKBOX)
    }

    fun tapIsShootOffWin(newValue: Boolean) {
        clickElement(HeadToHeadAddEndTestTag.IS_SHOOT_OFF_WIN_CHECKBOX, useUnmergedTree = true)
        checkShootOffWinChip(newValue)
    }

    fun checkSighters(count: Int) {
        checkElementTextOrDoesNotExist(AddEndTestTag.SIGHTERS, count.toString(), true)
    }

    fun clickSighters(block: ShootDetailsAddCountRobot.() -> Unit) {
        clickElement(AddEndTestTag.SIGHTERS, true)
        createRobot(ShootDetailsAddCountRobot::class, block)
    }

    fun clickEditRows(block: EditRowsDialogRobot.() -> Unit) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridTestTag.EDIT_ROWS_BUTTON)
            +CodexNodeMatcher.IsNotCached
            +CodexNodeInteraction.PerformClick()
        }
        EditRowsDialogRobot(::performV2).apply(block)
    }

    fun clickConfirmEdit() {
        clickElement(HeadToHeadAddEndTestTag.SAVE_BUTTON)
    }

    fun checkMatchComplete() {
        TODO()
    }

    fun clickCreateNextMatch(block: HeadToHeadAddHeatRobot.() -> Unit) {
        clickElement(HeadToHeadAddEndTestTag.CREATE_NEXT_MATCH_BUTTON)
        createRobot(HeadToHeadAddHeatRobot::class, block)
    }

    @RobotDslMarker
    class EditRowsDialogRobot(private val performFn: PerformFnV2) {
        fun checkEditRowsDialog(vararg rowsToValue: Pair<String, String>) {
            performFn {
                allNodes {
                    +CodexNodeMatcher.HasTestTag(HeadToHeadAddEndTestTag.EDIT_ROW_TYPES_DIALOG_ITEM)
                    +CodexNodeGroupInteraction.ForEach(
                            rowsToValue.map { (title, text) ->
                                listOf(CodexNodeInteraction.AssertContentDescriptionEquals("$text $title:"))
                            }
                    )
                }
            }
        }

        fun clickEditRowsDialogRow(row: String) {
            performFn {
                singleNode {
                    useUnmergedTree()
                    +CodexNodeMatcher.HasAnyAncestor(
                            CodexNodeMatcher.HasTestTag(HeadToHeadAddEndTestTag.EDIT_ROW_TYPES_DIALOG_ITEM),
                    )
                    +CodexNodeMatcher.HasAnySibling(CodexNodeMatcher.HasText("$row:"))
                    +CodexNodeMatcher.HasClickAction
                    +CodexNodeInteraction.PerformClick()
                }
            }
        }

        fun checkEditRowsDialogUnknownResultWarningShown(shown: Boolean = true) {
            TODO()
        }

        fun clickCancel() {
            performFn.clickDialogCancel()
        }

        fun clickOk() {
            performFn.clickDialogOk()
        }
    }
}
