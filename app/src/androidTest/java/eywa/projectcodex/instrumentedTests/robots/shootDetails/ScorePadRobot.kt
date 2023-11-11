package eywa.projectcodex.instrumentedTests.robots.shootDetails

import androidx.core.text.isDigitsOnly
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.common.utils.ListUtils.transpose
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher

class ScorePadRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, ScorePadTestTag.SCREEN) {
    fun waitForLoad() {
        perform {
            allNodes(CodexNodeMatcher.HasTestTag(ScorePadTestTag.CELL))
            +CodexNodeGroupToOne.First
            +CodexNodeInteraction.AssertIsDisplayed()
        }
    }

    /**
     * Checks all cells including headers
     */
    fun checkScorePadData(list: List<ExpectedRowData>) {
        perform {
            val allCells = list
                    .drop(1)
                    .map { it.asList() }
                    .transpose()
                    .flatten()
                    .mapNotNull {
                        if (it == null || it == "T" || it == "GT") {
                            null
                        }
                        else {
                            CodexNodeInteraction.AssertTextEquals(it).waitFor()
                        }
                    }
            useUnmergedTree = true
            allNodes(CodexNodeMatcher.HasTestTag(ScorePadTestTag.CELL))
            +CodexNodeGroupInteraction.ForEach(allCells.map { listOf(it) })
            +CodexNodeGroupInteraction.AssertCount(allCells.size)
        }
    }

    fun clickOkOnNoDataDialog() {
        clickDialogOk("No arrows entered")
        AddEndRobot(composeTestRule).apply { checkEndTotal(0) }
    }

    /**
     * @param endNumber 1-indexed
     */
    fun clickEnd(endNumber: Int) {
        perform {
            useUnmergedTree = true
            allNodes(CodexNodeMatcher.HasTestTag(ScorePadTestTag.CELL))
            +CodexNodeGroupToOne.HasContentDescription("End $endNumber")
            +CodexNodeInteraction.PerformScrollTo()
            +CodexNodeInteraction.PerformClick()
        }
    }

    private fun clickDropdownMenuItem(menuItem: String) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(ScorePadTestTag.DROPDOWN_MENU_ITEM)
            +CodexNodeMatcher.HasAnyDescendant(CodexNodeMatcher.HasText(menuItem))
            +CodexNodeInteraction.PerformClick()
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

        fun asList() = listOf(
                header,
                main.takeIf { it != "Arrows" },
                hits.takeIf { it.isDigitsOnly() },
                score.takeIf { it.isDigitsOnly() },
                golds.takeIf { it.isDigitsOnly() },
                runningTotal,
        )
    }
}
