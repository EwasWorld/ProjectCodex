package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.common.utils.transpose
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeAction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupAction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher

class ScorePadRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, ScorePadTestTag.SCREEN) {
    fun waitForLoad() {
        perform {
            allNodes(CodexNodeMatcher.HasTestTag(ScorePadTestTag.CELL))
            +CodexNodeGroupToOne.First
            +CodexNodeAction.AssertIsDisplayed
        }
    }

    /**
     * Checks all cells including headers
     */
    fun checkScorePadData(list: List<ExpectedRowData>) {
        val allCells = list.map { it.asList() }.transpose().flatten().map { CodexNodeAction.AssertTextEquals(it) }
        perform {
            allNodes(CodexNodeMatcher.HasTestTag(ScorePadTestTag.CELL))
            +CodexNodeGroupAction.ForEach(allCells)
            +CodexNodeGroupAction.AssertCount(allCells.size)
        }
    }

    fun clickOkOnNoDataDialog() {
        clickDialogOk("No arrows entered")
        AddEndRobot(composeTestRule).apply { checkEndTotal(0) }
    }

    /**
     * @param rowIndex does not include the header row
     */
    fun clickRow(rowIndex: Int) {
        perform {
            useUnmergedTree = true
            allNodes(CodexNodeMatcher.HasTestTag(ScorePadTestTag.CELL))
            +CodexNodeGroupToOne.Index(rowIndex)
            +CodexNodeAction.PerformScrollTo
            +CodexNodeAction.PerformClick
        }
    }

    private fun clickDropdownMenuItem(menuItem: String) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(ScorePadTestTag.DROPDOWN_MENU_ITEM)
            +CodexNodeMatcher.HasAnyDescendant(CodexNodeMatcher.HasText(menuItem))
            +CodexNodeAction.PerformClick
        }
    }

    fun clickEditDropdownMenuItem(block: EditEndRobot.() -> Unit) {
        clickDropdownMenuItem(CommonStrings.EDIT_MENU_ITEM)
        EditEndRobot(composeTestRule).apply { block() }
    }

    fun clickInsertDropdownMenuItem(block: InsertEndRobot.() -> Unit) {
        clickDropdownMenuItem(CommonStrings.INSERT_MENU_ITEM)
        InsertEndRobot(composeTestRule).apply { block() }
    }

    fun clickDeleteDropdownMenuItem(dialogAction: Boolean, endNumber: Int) {
        clickDropdownMenuItem(CommonStrings.DELETE_MENU_ITEM)

        checkElementText(SimpleDialogTestTag.MESSAGE, "Are you sure you want to delete end $endNumber")

        val titleText = "Delete end"
        if (dialogAction) clickDialogOk(titleText) else clickDialogCancel(titleText)
    }

    private object CommonStrings {
        const val EDIT_MENU_ITEM = "Edit end"
        const val DELETE_MENU_ITEM = "Delete end"
        const val INSERT_MENU_ITEM = "Insert end above"
    }

    data class ExpectedRowData(
            val header: String?,
            val main: String,
            val hits: String,
            val score: String,
            val golds: String,
            val runningTotal: String?,
    ) {
        constructor(
                header: String?,
                main: String,
                hits: Int,
                score: Int,
                golds: Int,
                runningTotal: Int?,
        ) : this(
                header,
                main,
                hits.toString(),
                score.toString(),
                golds.toString(),
                runningTotal?.toString(),
        )

        fun asList() = listOfNotNull(header ?: "", main, hits, score, golds, runningTotal ?: "-")
    }
}
