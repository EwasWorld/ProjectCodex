package eywa.projectcodex.instrumentedTests.robots

import androidx.compose.ui.test.*
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.sharedUi.RadioButtonDialogTestTag
import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.components.mainActivity.MainActivity
import eywa.projectcodex.components.mainMenu.MainMenuFragment
import eywa.projectcodex.components.viewScores.ViewScoresFragment
import eywa.projectcodex.components.viewScores.ui.ViewScoresScreen.TestTag

class ViewScoresRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, ViewScoresFragment::class) {
    fun clickOkOnEmptyTableDialog() {
        composeTestRule.onNode(
                hasTestTag(SimpleDialogTestTag.TITLE)
                        .and(hasText("Table is empty"))
        ).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SimpleDialogTestTag.POSITIVE_BUTTON).performClick()
        CustomConditionWaiter.waitForFragmentToShow(scenario, (MainMenuFragment::class))
    }

    /**
     * Wait for the number of rows on the screen to be [rowCount]
     */
    fun waitForRowCount(rowCount: Int) {
        CustomConditionWaiter.waitForComposeCondition {
            composeTestRule
                    .onAllNodesWithTag(TestTag.LIST_ITEM, true)
                    .assertCountEquals(rowCount)
        }
    }

    private fun performOnRowItem(rowIndex: Int, action: SemanticsNodeInteraction.() -> Unit) {
        composeTestRule
                .onNodeWithTag(TestTag.LAZY_COLUMN)
                .performScrollToIndex(rowIndex)
        composeTestRule
                .onAllNodesWithTag(TestTag.LIST_ITEM, useUnmergedTree = true)[rowIndex]
                .action()
    }

    fun clickRow(rowIndex: Int) = performOnRowItem(rowIndex) { performClick() }
    fun longClickRow(rowIndex: Int) = performOnRowItem(rowIndex) { performTouchInput { longClick() } }

    fun clickDropdownMenuItem(menuItem: String) {
        composeTestRule
                .onNode(
                        matcher = hasTestTag(TestTag.DROPDOWN_MENU_ITEM)
                                .and(hasAnyDescendant(hasText(menuItem))),
                        useUnmergedTree = true
                )
                .performClick()
    }

    fun clickEmailDropdownMenuItem(block: EmailScoreRobot.() -> Unit = {}) {
        clickDropdownMenuItem(CommonStrings.EMAIL_MENU_ITEM)
        EmailScoreRobot(composeTestRule).apply { block() }
    }

    fun checkDropdownMenuItemNotThere(menuItem: String) {
        // Check at least one menu item is showing
        composeTestRule
                .onAllNodesWithTag(TestTag.DROPDOWN_MENU_ITEM)
                .onFirst()
                .assertIsDisplayed()
        // Check that the intended menu item is not showing
        composeTestRule
                .onNode(
                        hasTestTag(TestTag.DROPDOWN_MENU_ITEM)
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
        composeTestRule.onNodeWithTag(TestTag.MULTI_SELECT_START).performClick()
    }

    fun clickMultiSelectSelectAll() {
        composeTestRule.onNodeWithTag(TestTag.MULTI_SELECT_ALL).performClick()
    }

    fun clickCancelMultiSelectMode() {
        composeTestRule.onNodeWithTag(TestTag.MULTI_SELECT_CANCEL).performClick()
    }

    fun clickMultiSelectEmail(block: EmailScoreRobot.() -> Unit = {}) {
        composeTestRule.onNodeWithTag(TestTag.MULTI_SELECT_EMAIL).performClick()
        EmailScoreRobot(composeTestRule).apply { block() }
    }

    fun checkMultiSelectMode(isInMultiSelectMode: Boolean = true) {
        (if (isInMultiSelectMode) TestTag.MULTI_SELECT_CANCEL else TestTag.MULTI_SELECT_START)
                .let { tag -> composeTestRule.onNodeWithTag(tag).assertIsDisplayed() }
    }

    fun checkEntriesNotSelectable() {
        composeTestRule.onAllNodesWithTag(TestTag.LIST_ITEM).assertAll(isSelectable().not())
    }

    /**
     * Check that all rows are selectable
     * and that [rowIndexes] are selected and all other rows are not selected
     */
    fun checkEntriesSelected(rowIndexes: Iterable<Int>, totalEntries: Int) {
        repeat(totalEntries) {
            val node = composeTestRule.onAllNodesWithTag(TestTag.LIST_ITEM)[it]
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