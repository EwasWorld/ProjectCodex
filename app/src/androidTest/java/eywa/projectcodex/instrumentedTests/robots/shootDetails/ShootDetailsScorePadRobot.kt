package eywa.projectcodex.instrumentedTests.robots.shootDetails

import androidx.core.text.isDigitsOnly
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher

class ShootDetailsScorePadRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, ScorePadTestTag.SCREEN) {
    fun waitForLoad() {
        performGroup {
            +CodexNodeMatcher.HasTestTag(ScorePadTestTag.CELL)
            toSingle(CodexNodeGroupToOne.First) {
                +CodexNodeInteraction.AssertIsDisplayed()
            }
        }
    }

    /**
     * Checks all cells including headers
     */
    fun checkScorePadData(list: List<ExpectedRowData>) {
        performGroup {
            val allCells = list
                    .drop(1)
                    .flatMap { it.asList() }
                    .mapNotNull {
                        if (it == "T" || it == "GT") {
                            null
                        }
                        else {
                            CodexNodeInteraction.AssertTextEquals(it).waitFor()
                        }
                    }
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(ScorePadTestTag.CELL)
            +CodexNodeMatcher.IsNotCached
            +CodexNodeGroupInteraction.ForEach(allCells.map { listOf(it) })
            +CodexNodeGroupInteraction.AssertCount(allCells.size)
        }
    }

    fun clickOkOnNoDataDialog() {
        clickDialogOk("No arrows entered")
        createRobot(ShootDetailsAddEndRobot::class) { checkEndTotal(0) }
    }

    /**
     * @param endNumber 1-indexed
     */
    fun clickEnd(endNumber: Int) {
        performGroup {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(ScorePadTestTag.CELL)
            +CodexNodeMatcher.IsNotCached
            toSingle(CodexNodeGroupToOne.HasContentDescription("End $endNumber")) {
                +CodexNodeInteraction.PerformScrollTo()
                +CodexNodeInteraction.PerformClick()
            }
        }
    }

    private fun clickDropdownMenuItem(menuItem: String) {
        performSingle {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(ScorePadTestTag.DROPDOWN_MENU_ITEM)
            +CodexNodeMatcher.HasAnyDescendant(CodexNodeMatcher.HasText(menuItem))
            +CodexNodeMatcher.IsNotCached
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun clickEditDropdownMenuItem(block: ShootDetailsEditEndRobot.() -> Unit) {
        clickDropdownMenuItem(CommonStrings.EDIT_MENU_ITEM)
        createRobot(ShootDetailsEditEndRobot::class, block)
    }

    fun clickInsertDropdownMenuItem(block: ShootDetailsInsertEndRobot.() -> Unit) {
        clickDropdownMenuItem(CommonStrings.INSERT_MENU_ITEM)
        createRobot(ShootDetailsInsertEndRobot::class, block)
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
            val golds: List<String>,
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
                listOf(golds.toString()),
                runningTotal?.toString(),
        )

        constructor(
                header: String?,
                main: String,
                hits: Int,
                score: Int,
                golds: List<Int>,
                runningTotal: Int?,
        ) : this(
                header,
                main,
                hits.toString(),
                score.toString(),
                golds.map { it.toString() },
                runningTotal?.toString(),
        )

        fun asList() = listOfNotNull(
                header,
                main.takeIf { it != "Arrows" },
                hits.takeIf { it.isDigitsOnly() },
                score.takeIf { it.isDigitsOnly() },
                *golds.filter { it.isDigitsOnly() }.toTypedArray(),
                runningTotal,
        )
    }
}
