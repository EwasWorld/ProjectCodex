package eywa.projectcodex.instrumentedTests.robots

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.sharedUi.RadioButtonDialogTestTag
import eywa.projectcodex.components.viewScores.ui.ViewScoresTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.shootDetails.AddEndRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ScorePadRobot

class ViewScoresRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, ViewScoresTestTag.SCREEN) {
    fun waitForLoad() {
        perform {
            useUnmergedTree = true
            allNodes(CodexNodeMatcher.HasTestTag(ViewScoresTestTag.LIST_ITEM))
            +CodexNodeGroupToOne.First
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
    }

    fun clickOkOnEmptyTableDialog() {
        clickDialogOk("Table is empty")
        MainMenuRobot(composeTestRule)
    }

    /**
     * Wait for the number of rows on the screen to be [rowCount]
     */
    fun waitForRowCount(rowCount: Int) {
        CustomConditionWaiter.waitForComposeCondition {
            composeTestRule
                    .onAllNodesWithTag(ViewScoresTestTag.LIST_ITEM.getTestTag(), true)
                    .assertCountEquals(rowCount)
        }
    }

    fun scrollToRow(rowIndex: Int) {
        composeTestRule.onNodeWithTag(ViewScoresTestTag.LAZY_COLUMN.getTestTag()).performScrollToIndex(rowIndex)
    }

    private fun performOnRowItem(rowIndex: Int, action: SemanticsNodeInteraction.() -> Unit) {
        scrollToRow(rowIndex)
        composeTestRule
                .onAllNodesWithTag(ViewScoresTestTag.LIST_ITEM.getTestTag(), useUnmergedTree = true)[rowIndex]
                .action()
    }

    fun clickRowForMultiSelect(rowIndex: Int) {
        performOnRowItem(rowIndex) { performClick() }
    }

    fun clickRow(rowIndex: Int, block: ScorePadRobot.() -> Unit = {}) {
        performOnRowItem(rowIndex) { performClick() }
        ScorePadRobot(composeTestRule).apply { block() }
    }

    fun longClickRow(rowIndex: Int) {
        performOnRowItem(rowIndex) { performTouchInput { longClick() } }

        perform {
            useUnmergedTree = true
            allNodes(CodexNodeMatcher.HasTestTag(ViewScoresTestTag.DROPDOWN_MENU_ITEM))
            +CodexNodeGroupToOne.First
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
    }

    fun clickDropdownMenuItem(menuItem: String) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(ViewScoresTestTag.DROPDOWN_MENU_ITEM)
            +CodexNodeMatcher.HasAnyDescendant(CodexNodeMatcher.HasText(menuItem))
            +CodexNodeInteraction.PerformClick().waitFor()
        }
    }

    fun clickEmailDropdownMenuItem(block: EmailScoreRobot.() -> Unit = {}) {
        clickDropdownMenuItem(CommonStrings.EMAIL_MENU_ITEM)
        EmailScoreRobot(composeTestRule).apply { block() }
    }

    fun clickEditDropdownMenuItem(block: NewScoreRobot.() -> Unit = {}) {
        clickDropdownMenuItem(CommonStrings.EDIT_MENU_ITEM)
        NewScoreRobot(composeTestRule).apply { block() }
    }

    fun clickContinueDropdownMenuItem(block: AddEndRobot.() -> Unit = {}) {
        clickDropdownMenuItem(CommonStrings.CONTINUE_MENU_ITEM)
        AddEndRobot(composeTestRule).apply { block() }
    }

    fun clickScorePadDropdownMenuItem(block: ScorePadRobot.() -> Unit = {}) {
        clickDropdownMenuItem(CommonStrings.SCORE_PAD_MENU_ITEM)
        ScorePadRobot(composeTestRule).apply { block() }
    }

    fun checkDropdownMenuItemNotThere(menuItem: String) {
        // Check at least one menu item is showing
        perform {
            useUnmergedTree = true
            allNodes(CodexNodeMatcher.HasTestTag(ViewScoresTestTag.DROPDOWN_MENU_ITEM))
            +CodexNodeGroupToOne.First
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
        // Check that the intended menu item is not showing
        composeTestRule
                .onNode(
                        hasTestTag(ViewScoresTestTag.DROPDOWN_MENU_ITEM.getTestTag())
                                .and(hasAnyDescendant(hasText(menuItem)))
                )
                .assertDoesNotExist()
    }

    fun chooseConvertDialogOption(convertType: String) {
        checkDialogIsDisplayed(CONVERT_SCORE_DIALOG_TITLE)
        composeTestRule
                .onAllNodesWithTag(RadioButtonDialogTestTag.RADIO_BUTTON)
                .filterToOne(hasText(convertType))
                .performClick()
    }

    fun clickConvertDialogOk() {
        clickDialogOk(CONVERT_SCORE_DIALOG_TITLE)
    }

    fun clickConvertDialogCancel() {
        clickDialogCancel(CONVERT_SCORE_DIALOG_TITLE)
    }

    fun clickDeleteDialogOk() {
        clickDialogOk(DELETE_ENTRY_DIALOG_TITLE)
    }

    fun clickDeleteDialogCancel() {
        clickDialogCancel(DELETE_ENTRY_DIALOG_TITLE)
    }

    private fun waitForTextInRow(rowIndex: Int, text: String) {
        CustomConditionWaiter.waitForComposeCondition {
            performOnRowItem(rowIndex) { assert(hasAnyDescendant(hasText(text, substring = true))) }
        }
    }

    fun waitForHsg(rowIndex: Int, hsg: String?) = waitForTextInRow(rowIndex, hsg ?: "-/-/-")
    fun waitForDate(rowIndex: Int, date: String) = waitForTextInRow(rowIndex, date)
    fun waitForHandicap(rowIndex: Int, handicap: Int?) = waitForTextInRow(rowIndex, handicap?.toString() ?: "-")
    fun waitForRoundName(rowIndex: Int, roundName: String?) = waitForTextInRow(rowIndex, roundName ?: "No Round")

    fun checkContentDescription(rowIndex: Int, description: String) {
        performOnRowItem(rowIndex) { assert(hasContentDescription(description)) }
    }

    fun clickStartMultiSelectMode() {
        clickElement(ViewScoresTestTag.MULTI_SELECT_START)
    }

    fun clickMultiSelectSelectAll() {
        clickElement(ViewScoresTestTag.MULTI_SELECT_ALL)
    }

    fun clickCancelMultiSelectMode() {
        clickElement(ViewScoresTestTag.MULTI_SELECT_CANCEL)
    }

    fun clickMultiSelectEmail(block: EmailScoreRobot.() -> Unit = {}) {
        clickElement(ViewScoresTestTag.MULTI_SELECT_EMAIL)
        EmailScoreRobot(composeTestRule).apply { block() }
    }

    fun checkMultiSelectMode(isInMultiSelectMode: Boolean = true) {
        checkElementIsDisplayed(
                if (isInMultiSelectMode) ViewScoresTestTag.MULTI_SELECT_CANCEL
                else ViewScoresTestTag.MULTI_SELECT_START
        )
    }

    fun checkEntriesNotSelectable() {
        checkAllElements(ViewScoresTestTag.LIST_ITEM, isSelectable().not())
    }

    /**
     * Check that all rows are selectable
     * and that [rowIndexes] are selected and all other rows are not selected
     */
    fun checkEntriesSelected(rowIndexes: Iterable<Int>, totalEntries: Int) {
        repeat(totalEntries) {
            val node = composeTestRule.onAllNodesWithTag(ViewScoresTestTag.LIST_ITEM.getTestTag())[it]
            node.assertIsSelectable()
            if (rowIndexes.contains(it)) node.assertIsSelected() else node.assertIsNotSelected()
        }
    }

    object CommonStrings {
        const val SCORE_PAD_MENU_ITEM = "Score pad"
        const val CONTINUE_MENU_ITEM = "Continue"
        const val DELETE_MENU_ITEM = "Delete"
        const val EMAIL_MENU_ITEM = "Email"
        const val EDIT_MENU_ITEM = "Edit info"
        const val CONVERT_MENU_ITEM = "Convert"
        const val CONVERT_XS_TO_TENS_OPTION = "Xs to 10s"
        const val CONVERT_TEN_ZONE_TO_FIVE_ZONE_OPTION = "10-zone to 5-zone"
    }

    companion object {
        private const val CONVERT_SCORE_DIALOG_TITLE = "Convert score"
        private const val DELETE_ENTRY_DIALOG_TITLE = "Delete entry"
    }
}
