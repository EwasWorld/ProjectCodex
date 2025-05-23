package eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead

import androidx.test.espresso.Espresso
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.addEnd.AddEndTestTag
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHead.addEnd.HeadToHeadAddEndTestTag
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGridColumnTestTag
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGridTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.clickDialogCancel
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.clickDialogOk
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.RobotDslMarker
import eywa.projectcodex.instrumentedTests.robots.common.PerformFn
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ArrowInputsRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsAddCountRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.common.SightMarkIndicatorRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.headToHead.GridSetDsl.CellValue

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
        performGroup {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.TYPE_CELL.get(1, 1), substring = true)
            +CodexNodeMatcher.IsNotCached
            +CodexNodeGroupInteraction.AssertCount(rowsToIsTotal.size).waitFor()
            +CodexNodeGroupInteraction.ForEach(
                    rowsToIsTotal.map { (row, _) -> listOf(CodexNodeInteraction.AssertTextEquals(row)) }
            )
        }

        val hasAnyArrowRow = rowsToIsTotal.any { !it.second }
        if (hasAnyArrowRow) {
            performGroup {
                +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.ARROW_CELL.get(1, 1), substring = true)
                +CodexNodeMatcher.IsNotCached
                +CodexNodeGroupInteraction.AssertCount(rowsToIsTotal.size).waitFor()
                +CodexNodeGroupInteraction.ForEach(
                        rowsToIsTotal.map { (_, isTotal) ->
                            if (isTotal) {
                                listOf(CodexNodeInteraction.AssertTextEquals("-"))
                            }
                            else {
                                // Valid arrow values: m (miss), 1-9, 10, X, or . (placeholder)
                                val arrowValue = "(?:[m1-9X\\.]|10)"
                                // 1 to 3 arrows separated by a '-'
                                val regex = "$arrowValue(?:-$arrowValue){${endSize - 1}}"
                                listOf(CodexNodeInteraction.AssertTextMatchesRegex(regex))
                            }
                        }
                )
            }
        }
        else {
            performSingle {
                +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.ARROW_CELL.get(1, 1), substring = true)
                +CodexNodeMatcher.IsNotCached
                +CodexNodeInteraction.AssertDoesNotExist()
            }
        }

        performGroup {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.END_TOTAL_CELL.get(1, 1), substring = true)
            +CodexNodeMatcher.IsNotCached
            +CodexNodeGroupInteraction.AssertCount(rowsToIsTotal.size).waitFor()
            +CodexNodeGroupInteraction.ForEach(
                    rowsToIsTotal.map { (row, isTotal) ->
                        if (row == "Result") {
                            listOf(CodexNodeInteraction.AssertTextMatchesRegex("(Win|Loss|Tie)"))
                        }
                        else if (isTotal) {
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
            typeTag: String,
            arrows: List<String>,
            score: Int,
            teamScore: CellValue = CellValue.NoColumn,
            points: CellValue = CellValue.NoColumn,
    ) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.ARROW_CELL.get(1, 1, type))
            +CodexNodeInteraction.PerformClick()
        }
        arrows.forEach {
            clickScoreButton(it)
        }

        checkArrowRow(
                rowIndex = rowIndex,
                type = type,
                typeTag = typeTag,
                arrows = arrows,
                score = score,
                teamScore = teamScore,
                points = points
        )
    }

    fun checkArrowRow(
            rowIndex: Int,
            type: String,
            typeTag: String,
            arrows: List<String>,
            score: Int,
            teamScore: CellValue = CellValue.NoColumn,
            points: CellValue = CellValue.NoColumn,
    ) {
        gridSetDsl.checkRow(
                rowIndex = rowIndex,
                type = type,
                typeTag = typeTag,
                arrows = CellValue.Value(arrows.joinToString("-")),
                score = score,
                teamScore = teamScore,
                points = points
        )
    }

    fun setTotalRow(
            rowIndex: Int,
            type: String,
            typeTag: String,
            score: Int,
            arrows: CellValue = CellValue.Empty,
            teamScore: CellValue = CellValue.NoColumn,
            points: CellValue = CellValue.NoColumn,
    ) {
        performSingle {
            useUnmergedTree()
            +CodexNodeMatcher.HasAnyAncestor(
                    CodexNodeMatcher.HasTestTag(
                            testTag = HeadToHeadGridColumnTestTag.END_TOTAL_CELL.get(1, 1, typeTag),
                    ),
            )
            +CodexNodeMatcher.HasSetTextAction
            +CodexNodeInteraction.SetText(score.toString()).waitFor()
        }
        Espresso.closeSoftKeyboard()

        checkTotalRow(
                rowIndex = rowIndex,
                type = type,
                typeTag = typeTag,
                score = score,
                arrows = arrows,
                teamScore = teamScore,
                points = points,
        )
    }

    fun checkTotalRow(
            rowIndex: Int,
            type: String,
            typeTag: String,
            score: Int,
            arrows: CellValue = CellValue.Empty,
            teamScore: CellValue = CellValue.NoColumn,
            points: CellValue = CellValue.NoColumn,
    ) {
        gridSetDsl.checkRow(
                rowIndex = rowIndex,
                type = type,
                typeTag = typeTag,
                arrows = arrows,
                score = score,
                teamScore = teamScore,
                points = points,
                isEditable = true,
        )
    }

    fun clickResultRow(rowIndex: Int, newValue: HeadToHeadResult) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(
                    HeadToHeadGridColumnTestTag.END_TOTAL_CELL.get(1, 1, GridSetDsl.RESULTS_NAME_AND_TAG),
            )
            +CodexNodeMatcher.IsNotCached
            +CodexNodeInteraction.PerformClick().waitFor()
        }
        gridSetDsl.checkResultsRow(rowIndex, newValue.asString())
    }

    fun checkResultRow(rowIndex: Int, result: HeadToHeadResult) {
        gridSetDsl.checkResultsRow(rowIndex, result.asString())
    }

    /**
     * Should stay on add end screen, use overload for jump to add match screen
     */
    fun clickNextEnd() {
        clickElement(HeadToHeadAddEndTestTag.NEXT_END_BUTTON, scrollTo = true)
        checkScreenIsShown()
    }

    fun clickInsertEnd() {
        clickElement(HeadToHeadAddEndTestTag.NEXT_END_BUTTON, scrollTo = true)
        createRobot(HeadToHeadScorePadRobot::class) {}
    }

    fun clickNextEnd(block: HeadToHeadAddMatchRobot.() -> Unit) {
        clickElement(HeadToHeadAddEndTestTag.NEXT_END_BUTTON, scrollTo = true)
        createRobot(HeadToHeadAddMatchRobot::class, block)
    }

    fun checkArrowRowError(error: String?, type: String) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.ARROW_CELL.get(1, 1, type))
            +CodexNodeInteraction.AssertHasError(error)
        }
        checkElementTextOrDoesNotExist(HeadToHeadAddEndTestTag.GRID_ERROR_TEXT, error)
    }

    fun checkTotalRowError(error: String?, type: String) {
        performSingle {
            +CodexNodeMatcher.HasAnyAncestor(
                    CodexNodeMatcher.HasTestTag(
                            testTag = HeadToHeadGridColumnTestTag.END_TOTAL_CELL.get(1, 1, type)
                    )
            )
            +CodexNodeInteraction.AssertHasError(error)
        }
        checkElementTextOrDoesNotExist(HeadToHeadAddEndTestTag.GRID_ERROR_TEXT, error)
    }

    fun checkSetResult(set: Int, result: HeadToHeadResult) {
        checkElementText(HeadToHeadAddEndTestTag.SET_RESULT, "Set $set: ${result.asString()}")
    }

    fun checkNoSetResult() {
        checkElementDoesNotExist(HeadToHeadAddEndTestTag.SET_RESULT)
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
        else "Opponent: (rank $rank)\n$name"
        checkElementText(HeadToHeadAddEndTestTag.OPPONENT, text)

//        perform {
//            singleNode {
//                +CodexNodeMatcher.HasTestTag(HeadToHeadAddEndTestTag.OPPONENT)
//                +CodexNodeInteraction.AssertTextContains(text).waitFor()
//            }
//        }
    }

    fun checkShootOffWinChip(rowIndex: Int, result: Boolean?) {
        gridSetDsl.checkShootOffRow(rowIndex, result)
    }

    fun checkShootOffWinChipNotShown() {
        gridSetDsl.checkShootOffRowNotShown()
    }

    fun tapIsShootOffWin(rowIndex: Int, newValue: Boolean?) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(
                    HeadToHeadGridColumnTestTag.END_TOTAL_CELL.get(1, 1, GridSetDsl.SHOOT_OFF_TAG),
            )
            +CodexNodeMatcher.IsNotCached
            +CodexNodeInteraction.PerformClick().waitFor()
        }
        checkShootOffWinChip(rowIndex, newValue)
    }

    fun checkShootOffChip(isWin: Boolean) {
        checkCheckboxState(HeadToHeadAddEndTestTag.IS_SHOOT_OFF_CHECKBOX, isWin, useUnmergedTree = true)
    }

    fun checkShootOffChipNotShown() {
        checkElementDoesNotExist(HeadToHeadAddEndTestTag.IS_SHOOT_OFF_CHECKBOX)
    }

    fun tapIsShootOff(newValue: Boolean) {
        clickElement(HeadToHeadAddEndTestTag.IS_SHOOT_OFF_CHECKBOX, useUnmergedTree = true)
        checkShootOffChip(newValue)
    }

    fun checkSighters(count: Int) {
        checkElementTextOrDoesNotExist(AddEndTestTag.SIGHTERS, count.toString(), true)
    }

    fun clickSighters(block: ShootDetailsAddCountRobot.() -> Unit) {
        clickElement(AddEndTestTag.SIGHTERS, true)
        createRobot(ShootDetailsAddCountRobot::class, block)
    }

    fun clickEditRows(block: EditRowsDialogRobot.() -> Unit) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridTestTag.EDIT_ROWS_BUTTON)
            +CodexNodeMatcher.IsNotCached
            +CodexNodeInteraction.PerformClick()
        }
        EditRowsDialogRobot(::perform).apply(block)
    }

    fun clickConfirmEdit() {
        clickElement(HeadToHeadAddEndTestTag.SAVE_BUTTON)
    }

    fun clickCreateNextMatch(block: HeadToHeadAddMatchRobot.() -> Unit) {
        clickElement(HeadToHeadAddEndTestTag.CREATE_NEXT_MATCH_BUTTON)
        createRobot(HeadToHeadAddMatchRobot::class, block)
    }

    @RobotDslMarker
    class EditRowsDialogRobot(private val performFn: PerformFn) {
        fun checkEditRowsDialog(vararg rowsToValue: Pair<String, String>) {
            performFn {
                allNodes {
                    +CodexNodeMatcher.HasTestTag(HeadToHeadAddEndTestTag.EDIT_ROW_TYPES_DIALOG_ITEM)
                    +CodexNodeGroupInteraction.AssertCount(rowsToValue.size)
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
            performFn {
                singleNode {
                    +CodexNodeMatcher.HasTestTag(HeadToHeadAddEndTestTag.EDIT_ROW_TYPES_DIALOG_WARNING)
                    if (shown) {
                        +CodexNodeInteraction.AssertIsDisplayed()
                    }
                    else {
                        +CodexNodeInteraction.AssertDoesNotExist()
                    }
                }
            }
        }

        fun clickCancel() {
            performFn.clickDialogCancel()
        }

        fun clickOk() {
            performFn.clickDialogOk()
        }
    }
}
