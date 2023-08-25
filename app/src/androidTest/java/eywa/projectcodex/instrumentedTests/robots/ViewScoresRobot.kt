package eywa.projectcodex.instrumentedTests.robots

import androidx.compose.ui.test.*
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.sharedUi.RadioButtonDialogTestTag
import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.components.viewScores.ui.ViewScoresTestTag
import eywa.projectcodex.instrumentedTests.robots.shootDetails.AddEndRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ScorePadRobot

class ViewScoresRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, ViewScoresTestTag.SCREEN) {
    fun waitForLoad() {
        CustomConditionWaiter.waitForComposeCondition {
            checkAtLeastOneElementIsDisplayed(ViewScoresTestTag.LIST_ITEM, true)
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
        scrollTo(ViewScoresTestTag.LAZY_COLUMN, rowIndex)
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
        CustomConditionWaiter.waitForComposeCondition {
            checkAtLeastOneElementIsDisplayed(ViewScoresTestTag.DROPDOWN_MENU_ITEM, useUnmergedTree = true)
        }
    }

    fun clickDropdownMenuItem(menuItem: String) {
        composeTestRule
                .onNode(
                        matcher = hasTestTag(ViewScoresTestTag.DROPDOWN_MENU_ITEM.getTestTag())
                                .and(hasAnyDescendant(hasText(menuItem))),
                        useUnmergedTree = true
                )
                .performClick()
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
        checkAtLeastOneElementIsDisplayed(ViewScoresTestTag.DROPDOWN_MENU_ITEM, useUnmergedTree = true)
        // Check that the intended menu item is not showing
        composeTestRule
                .onNode(
                        hasTestTag(ViewScoresTestTag.DROPDOWN_MENU_ITEM.getTestTag())
                                .and(hasAnyDescendant(hasText(menuItem)))
                )
                .assertDoesNotExist()
    }

    fun chooseConvertDialogOption(convertType: String) {
        CustomConditionWaiter.waitForComposeCondition("Waiting for convert dialog to display") {
            composeTestRule.onNode(
                    hasTestTag(SimpleDialogTestTag.TITLE)
                            .and(hasText(CONVERT_SCORE_DIALOG_TITLE))
            ).assertIsDisplayed()
        }
        composeTestRule
                .onAllNodesWithTag(RadioButtonDialogTestTag.RADIO_BUTTON)
                .filterToOne(hasText(convertType))
                .performClick()
    }

    fun clickConvertDialogOk() {
        CustomConditionWaiter.waitForComposeCondition("Waiting for convert dialog to display") {
            composeTestRule.onNode(
                    hasTestTag(SimpleDialogTestTag.TITLE)
                            .and(hasText(CONVERT_SCORE_DIALOG_TITLE))
            ).assertIsDisplayed()
        }
        composeTestRule
                .onNodeWithTag(SimpleDialogTestTag.POSITIVE_BUTTON)
                .performClick()
    }

    fun clickConvertDialogCancel() {
        CustomConditionWaiter.waitForComposeCondition("Waiting for convert dialog to display") {
            composeTestRule.onNode(
                    hasTestTag(SimpleDialogTestTag.TITLE)
                            .and(hasText(CONVERT_SCORE_DIALOG_TITLE))
            ).assertIsDisplayed()
        }
        composeTestRule
                .onNodeWithTag(SimpleDialogTestTag.NEGATIVE_BUTTON)
                .performClick()
    }

    fun clickDeleteDialogOk() {
        CustomConditionWaiter.waitForComposeCondition("Waiting for delete dialog to display") {
            composeTestRule.onNode(
                    hasTestTag(SimpleDialogTestTag.TITLE)
                            .and(hasText(DELETE_ENTRY_DIALOG_TITLE))
            ).assertIsDisplayed()
        }
        composeTestRule
                .onNodeWithTag(SimpleDialogTestTag.POSITIVE_BUTTON)
                .performClick()
    }

    fun clickDeleteDialogCancel() {
        CustomConditionWaiter.waitForComposeCondition("Waiting for delete dialog to display") {
            composeTestRule.onNode(
                    hasTestTag(SimpleDialogTestTag.TITLE)
                            .and(hasText(DELETE_ENTRY_DIALOG_TITLE))
            ).assertIsDisplayed()
        }
        composeTestRule
                .onNodeWithTag(SimpleDialogTestTag.NEGATIVE_BUTTON)
                .performClick()
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
        const val CONTINUE_MENU_ITEM = "Continue scoring"
        const val DELETE_MENU_ITEM = "Delete"
        const val EMAIL_MENU_ITEM = "Email score"
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
