package eywa.projectcodex.instrumentedTests.robots.archerRoundScore

import androidx.compose.ui.test.*
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.common.utils.transpose
import eywa.projectcodex.components.archerRoundScore.scorePad.ScorePadScreen.TestTag
import eywa.projectcodex.components.mainActivity.MainActivity

class ScorePadRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ArcherRoundRobot(composeTestRule, TestTag.SCREEN) {
    fun waitForLoad() {
        CustomConditionWaiter.waitForComposeCondition {
            composeTestRule
                    .onAllNodesWithTag(TestTag.CELL)
                    .onFirst()
                    .assertIsDisplayed()
        }
    }

    /**
     * Checks all cells including headers
     */
    fun checkScorePadData(list: List<ExpectedRowData>) {
        val allCells = list.map { it.asList() }.transpose().flatten()
        val nodes = composeTestRule.onAllNodesWithTag(TestTag.CELL)

        allCells.forEachIndexed { index, text ->
            CustomConditionWaiter.waitForComposeCondition {
                nodes[index].assertTextEquals(text)
            }
        }
        nodes.assertCountEquals(allCells.size)
    }

    fun clickOkOnNoDataDialog() {
        clickDialogOk("No arrows entered")
        InputEndRobot(composeTestRule).apply { checkEndTotal(0) }
    }

    /**
     * @param rowIndex does not include the header row
     */
    fun clickRow(rowIndex: Int) {
        composeTestRule
                .onAllNodesWithTag(TestTag.CELL, useUnmergedTree = true)[rowIndex]
                .performScrollTo()
                .performClick()
    }

    private fun clickDropdownMenuItem(menuItem: String) {
        composeTestRule
                .onNode(
                        matcher = hasTestTag(TestTag.DROPDOWN_MENU_ITEM)
                                .and(hasAnyDescendant(hasText(menuItem))),
                        useUnmergedTree = true
                )
                .performClick()
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
