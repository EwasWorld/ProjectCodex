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
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsAddCountRobot
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
                    listOf(
                            CodexNodeMatcher.HasTestTag(
                                    testTag = HeadToHeadGridColumnTestTag.TYPE_CELL.get(match, 1),
                                    substring = true
                            )
                    )
            )
        }
        performGroup {
            useUnmergedTree()
            +CodexNodeMatcher.HasAnyAncestor(
                    CodexNodeMatcher.HasTestTag(
                            testTag = HeadToHeadGridColumnTestTag.MATCH_RESULT.get(match, 1),
                            substring = true,
                    ),
            )
            toSingle(CodexNodeGroupToOne.Index(1)) {
                +CodexNodeInteraction.AssertTextEquals(result.asString()).waitFor()
            }
        }
        GridDsl(match, this).apply(config)
    }

    fun clickSighters(match: Int, block: ShootDetailsAddCountRobot.() -> Unit) {
        clickDataRowValue(HeadToHeadScorePadMatchTestTag.SIGHTERS.getTestTag(match))
        createRobot(ShootDetailsAddCountRobot::class, block)
    }

    fun openEditEnd(match: Int, setNumber: Int, block: HeadToHeadAddEndRobot.() -> Unit) {
        clickSet(match, setNumber)
        clickSetMenuItem(match, setNumber, "Edit set")
        createRobot(HeadToHeadAddEndRobot::class, block)
    }

    fun deleteEnd(match: Int, setNumber: Int) {
        clickSet(match, setNumber)
        clickSetMenuItem(match, setNumber, "Delete set")
        clickDialogOk("Delete set")
    }

    fun insertEnd(match: Int, setNumber: Int, block: HeadToHeadAddEndRobot.() -> Unit) {
        clickSet(match, setNumber)
        clickSetMenuItem(match, setNumber, "Insert set above")
        createRobot(HeadToHeadAddEndRobot::class, block)
    }

    fun checkCannotAddOrInsertEnd(match: Int, setNumber: Int) {
        checkCannotAddEnd(match)
        checkCannotInsertEnd(match, setNumber)
    }

    private fun checkCannotAddEnd(match: Int) {
        clickMatchEditButton(match)

        // Wait for menu to appear
        performSingle {
            +CodexNodeMatcher.HasTestTag(HeadToHeadScorePadMatchTestTag.MATCH_DROPDOWN_MENU_ITEM.getTestTag(match))
            +CodexNodeMatcher.HasText("Edit match")
            +CodexNodeMatcher.IsNotCached
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }

        performSingle {
            +CodexNodeMatcher.HasTestTag(HeadToHeadScorePadMatchTestTag.MATCH_DROPDOWN_MENU_ITEM.getTestTag(match))
            +CodexNodeMatcher.HasText("Add set")
            +CodexNodeMatcher.IsNotCached
            +CodexNodeInteraction.AssertDoesNotExist()
        }

        // Dismiss dropdown
        clickMatchEditButton(match)
    }

    fun checkAddEndOptionAvailable(match: Int) {
        clickMatchEditButton(match)

        // Wait for menu to appear
        performSingle {
            +CodexNodeMatcher.HasTestTag(HeadToHeadScorePadMatchTestTag.MATCH_DROPDOWN_MENU_ITEM.getTestTag(match))
            +CodexNodeMatcher.HasText("Edit match")
            +CodexNodeMatcher.IsNotCached
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }

        performSingle {
            +CodexNodeMatcher.HasTestTag(HeadToHeadScorePadMatchTestTag.MATCH_DROPDOWN_MENU_ITEM.getTestTag(match))
            +CodexNodeMatcher.HasText("Add set")
            +CodexNodeMatcher.IsNotCached
            +CodexNodeInteraction.AssertIsDisplayed()
        }

        // Dismiss dropdown
        clickMatchEditButton(match)
    }

    private fun checkCannotInsertEnd(match: Int, setNumber: Int) {
        clickSet(match, setNumber)

        // Wait for menu to appear
        performSingle {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.SET_DROPDOWN_MENU_ITEM.get(match, setNumber))
            +CodexNodeMatcher.HasText("Edit set")
            +CodexNodeMatcher.IsNotCached
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }

        performSingle {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.SET_DROPDOWN_MENU_ITEM.get(match, setNumber))
            +CodexNodeMatcher.HasText("Insert set above")
            +CodexNodeMatcher.IsNotCached
            +CodexNodeInteraction.AssertDoesNotExist()
        }

        // Dismiss dropdown
        performGroup {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.TYPE_CELL.get(match, setNumber), substring = true)
            toSingle(CodexNodeGroupToOne.First) {
                +CodexNodeInteraction.PerformClick().waitFor()
            }
        }
    }

    private fun clickSet(match: Int, setNumber: Int) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(HeadToHeadScorePadTestTag.SCREEN)
            +CodexNodeInteraction.PerformScrollToNode(
                    listOf(
                            CodexNodeMatcher.HasTestTag(
                                    testTag = HeadToHeadGridColumnTestTag.TYPE_CELL.get(match, setNumber),
                                    substring = true,
                            )
                    )
            )
        }
        performGroup {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.TYPE_CELL.get(match, setNumber), substring = true)
            toSingle(CodexNodeGroupToOne.First) {
                +CodexNodeInteraction.PerformClick()
            }
        }
    }

    private fun clickSetMenuItem(match: Int, setNumber: Int, text: String) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.SET_DROPDOWN_MENU_ITEM.get(match, setNumber))
            +CodexNodeMatcher.HasText(text)
            +CodexNodeMatcher.IsNotCached
            +CodexNodeInteraction.PerformClick().waitFor()
        }
    }

    private fun clickMatchEditButton(match: Int) {
        clickElement(HeadToHeadScorePadMatchTestTag.EDIT_MATCH_INFO_BUTTON.getTestTag(match), scrollTo = true)
    }

    private fun clickMatchMenuItem(match: Int, text: String) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(HeadToHeadScorePadMatchTestTag.MATCH_DROPDOWN_MENU_ITEM.getTestTag(match))
            +CodexNodeMatcher.HasText(text)
            +CodexNodeMatcher.IsNotCached
            +CodexNodeInteraction.PerformClick().waitFor()
        }
    }

    fun checkMatchMenuNotShown(match: Int) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(HeadToHeadScorePadMatchTestTag.MATCH_DROPDOWN_MENU_ITEM.getTestTag(match))
            +CodexNodeInteraction.AssertDoesNotExist().waitFor()
        }
    }

    fun checkSetMenuNotShown(match: Int, setNumber: Int) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.SET_DROPDOWN_MENU_ITEM.get(match, setNumber))
            +CodexNodeInteraction.AssertDoesNotExist().waitFor()
        }
    }

    fun clickEditMatchInfo(match: Int, block: HeadToHeadAddMatchRobot.() -> Unit) {
        clickMatchEditButton(match)
        clickMatchMenuItem(match, "Edit match")
        createRobot(HeadToHeadAddMatchRobot::class, block)
    }

    fun clickAddNewSet(match: Int, block: HeadToHeadAddEndRobot.() -> Unit) {
        clickMatchEditButton(match)
        clickMatchMenuItem(match, "Add set")
        createRobot(HeadToHeadAddEndRobot::class, block)
    }

    fun clickInsertMatch(match: Int, block: HeadToHeadAddMatchRobot.() -> Unit) {
        clickMatchEditButton(match)
        clickMatchMenuItem(match, "Insert match above")
        createRobot(HeadToHeadAddMatchRobot::class, block)
    }

    fun clickDeleteMatch(match: Int) {
        clickMatchEditButton(match)
        clickMatchMenuItem(match, "Delete match")
        clickDialogOk("Delete match")
    }

    fun checkMatchCount(count: Int) {
        repeat(count) { match ->
            checkElementIsDisplayed(
                    testTag = HeadToHeadScorePadMatchTestTag.MATCH_TEXT.getTestTag(match + 1),
                    scrollTo = true,
            )
        }
        checkElementDoesNotExist(HeadToHeadScorePadMatchTestTag.MATCH_TEXT.getTestTag(count + 1))
    }
}

@RobotDslMarker
class GridDsl(
        private val match: Int,
        private val robot: BaseRobot,
) {
    fun checkEnd(
            setNumber: Int,
            result: HeadToHeadResult?,
            runningTotal: String,
            rowCount: Int,
            config: GridSetDsl.() -> Unit,
    ) {
        if (result == null) {
            robot.checkElementDoesNotExist(HeadToHeadGridColumnTestTag.SET_RESULT.get(match, setNumber))
        }
        else {
            robot.checkDataRowValueText(HeadToHeadGridColumnTestTag.SET_RESULT.get(match, setNumber), result.asString())
        }

        robot.checkDataRowValueText(HeadToHeadGridColumnTestTag.SET_RUNNING_TOTAL.get(match, setNumber), runningTotal)

        robot.performGroup {
            +CodexNodeMatcher.HasTestTag(
                    testTag = HeadToHeadGridColumnTestTag.END_TOTAL_CELL.get(match, setNumber),
                    substring = true,
            )
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
            type: String,
            testTag: HeadToHeadGridColumnTestTag,
            config: TestActionDslSingleNode.() -> Unit
    ) {
        robot.performSingle {
            +CodexNodeMatcher.HasTestTag(testTag.get(match, setNumber, type), substring = true)
            +CodexNodeMatcher.IsNotCached
            config()
        }
    }

    private fun checkCell(
            type: String,
            testTag: HeadToHeadGridColumnTestTag,
            content: CellValue,
    ) {
        when (content) {
            is CellValue.NoColumn -> {
                robot.performSingle {
                    +CodexNodeMatcher.HasTestTag(testTag.get(match, setNumber))
                    +CodexNodeInteraction.AssertDoesNotExist()
                }
            }

            is CellValue.NoCell -> {
                performOn(type, testTag) {
                    +CodexNodeInteraction.AssertDoesNotExist()
                }
            }

            else -> {
                performOn(type, testTag) {
                    +CodexNodeInteraction.AssertTextEquals(if (content is CellValue.Value) content.value else "-")
                            .waitFor()
                }
            }
        }
    }

    fun checkRow(
            rowIndex: Int,
            type: String,
            typeTag: String,
            arrows: CellValue,
            score: Int,
            teamScore: CellValue,
            points: CellValue = CellValue.NoColumn,
            isEditable: Boolean = false,
    ) {
        robot.performGroup {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.TYPE_CELL.get(match, setNumber), substring = true)
            +CodexNodeMatcher.IsNotCached
            toSingle(CodexNodeGroupToOne.Index(rowIndex)) {
                +CodexNodeInteraction.AssertTextEquals(type)
            }
        }

        robot.performSingle {
            val matcher = CodexNodeMatcher.HasTestTag(
                    HeadToHeadGridColumnTestTag.END_TOTAL_CELL.get(match, setNumber, typeTag),
            )
            if (isEditable) +CodexNodeMatcher.HasAnyAncestor(matcher) else +matcher
            +CodexNodeInteraction.AssertTextEquals(score.toString()).waitFor()
        }

        checkCell(typeTag, HeadToHeadGridColumnTestTag.ARROW_CELL, arrows)
        checkCell(typeTag, HeadToHeadGridColumnTestTag.TEAM_TOTAL_CELL, teamScore)
        checkCell(typeTag, HeadToHeadGridColumnTestTag.POINTS_CELL, points)
    }

    fun checkResultsRow(rowIndex: Int, result: String) {
        robot.performGroup {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.TYPE_CELL.get(match, setNumber), substring = true)
            +CodexNodeMatcher.IsNotCached
            toSingle(CodexNodeGroupToOne.Index(rowIndex)) {
                +CodexNodeInteraction.AssertTextEquals(RESULTS_NAME_AND_TAG)
            }
        }

        performOn(RESULTS_NAME_AND_TAG, HeadToHeadGridColumnTestTag.TYPE_CELL) {
            +CodexNodeInteraction.AssertTextEquals(RESULTS_NAME_AND_TAG)
        }
        performOn(RESULTS_NAME_AND_TAG, HeadToHeadGridColumnTestTag.END_TOTAL_CELL) {
            +CodexNodeInteraction.AssertTextEquals(result)
        }
    }

    fun checkShootOffRow(rowIndex: Int, result: Boolean?) {
        checkShootOffRow(
                rowIndex = rowIndex,
                iconContentDescription = when (result) {
                    true -> "True"
                    false -> "False"
                    null -> "Not applicable"
                },
        )
    }

    fun checkShootOffRow(rowIndex: Int, iconContentDescription: String) {
        val testTagAppend = SHOOT_OFF_TAG

        robot.performGroup {
            +CodexNodeMatcher.HasTestTag(HeadToHeadGridColumnTestTag.TYPE_CELL.get(match, setNumber), substring = true)
            +CodexNodeMatcher.IsNotCached
            toSingle(CodexNodeGroupToOne.Index(rowIndex)) {
                +CodexNodeInteraction.AssertTextEquals(SHOOT_OFF_NAME)
            }
        }

        performOn(testTagAppend, HeadToHeadGridColumnTestTag.TYPE_CELL) {
            +CodexNodeInteraction.AssertTextEquals(SHOOT_OFF_NAME)
        }
        performOn(testTagAppend, HeadToHeadGridColumnTestTag.END_TOTAL_CELL) {
            +CodexNodeInteraction.AssertContentDescriptionEquals(iconContentDescription).waitFor()
        }
    }

    fun checkShootOffRowNotShown() {
        robot.checkElementDoesNotExist(HeadToHeadGridColumnTestTag.TYPE_CELL.get(match, setNumber, SHOOT_OFF_TAG))
    }

    sealed class CellValue {
        data object NoColumn : CellValue()
        data object NoCell : CellValue()
        data object Empty : CellValue()
        data class Value(val value: String) : CellValue() {
            constructor(value: Int) : this(value.toString())
        }
    }

    companion object {
        const val DEFAULT_ARCHER_NAME = "Self"
        const val DEFAULT_OPPONENT_NAME = "Opponent"
        const val DEFAULT_TEAM_NAME = "Team"
        const val DEFAULT_TEAM_MATE_NAME = "Team mate(s)"
        const val TEAM_MATE_TAG = "Teammates"
        const val SHOOT_OFF_TAG = "Closesttocentre"
        const val SHOOT_OFF_NAME = "Closest\nto centre"
        const val RESULTS_NAME_AND_TAG = "Result"
    }
}
