package eywa.projectcodex.instrumentedTests.robots

import androidx.compose.ui.test.isSelectable
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.sharedUi.RadioButtonDialogTestTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.viewScores.ui.ViewScoresEntryRowTestTag
import eywa.projectcodex.components.viewScores.ui.ViewScoresTestTag
import eywa.projectcodex.components.viewScores.ui.viewScoresListItemTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.assertTextEqualsOrNotExist
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.dsl.TestActionDsl
import eywa.projectcodex.instrumentedTests.robots.shootDetails.AddEndRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ScorePadRobot

private fun TestActionDsl.inRow(rowIndex: Int) {
    useUnmergedTree = true
    +CodexNodeMatcher.HasAnyAncestor(CodexNodeMatcher.HasTestTag(viewScoresListItemTestTag(rowIndex)))
}

class ViewScoresRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, ViewScoresTestTag.SCREEN) {
    fun waitForLoad() {
        perform {
            useUnmergedTree = true
            allNodes(CodexNodeMatcher.HasTestTag(ViewScoresTestTag.LIST_ITEM, true))
            +CodexNodeGroupToOne.First
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
    }

    fun clickOkOnEmptyTableDialog() {
        clickDialogOk("Table is empty")
        createRobot(MainMenuRobot::class) {}
    }

    /**
     * Wait for the number of rows on the screen to be [rowCount]
     */
    fun waitForRowCount(rowCount: Int) {
        perform {
            useUnmergedTree = true
            allNodes(CodexNodeMatcher.HasTestTag(ViewScoresTestTag.LIST_ITEM, substring = true))
            +CodexNodeGroupInteraction.AssertCount(rowCount).waitFor()
        }
    }

    fun scrollToRow(rowIndex: Int) {
        perform {
            +CodexNodeMatcher.HasTestTag(ViewScoresTestTag.LAZY_COLUMN)
            +CodexNodeInteraction.PerformScrollToIndex(rowIndex)
        }
    }

    private fun performOnRowItem(rowIndex: Int, action: CodexNodeInteraction) {
        scrollToRow(rowIndex)
        perform {
            +CodexNodeMatcher.HasTestTag(viewScoresListItemTestTag(rowIndex))
            +action
        }
    }

    fun clickRowForMultiSelect(rowIndex: Int) {
        performOnRowItem(rowIndex, CodexNodeInteraction.PerformClick())
    }

    fun clickRow(rowIndex: Int, block: ScorePadRobot.() -> Unit = {}) {
        performOnRowItem(rowIndex, CodexNodeInteraction.PerformClick())
        createRobot(ScorePadRobot::class, block)
    }

    fun longClickRow(rowIndex: Int) {
        performOnRowItem(rowIndex, CodexNodeInteraction.PerformLongClick())

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
        createRobot(EmailScoreRobot::class, block)
    }

    fun clickEditDropdownMenuItem(block: NewScoreRobot.() -> Unit = {}) {
        clickDropdownMenuItem(CommonStrings.EDIT_MENU_ITEM)
        createRobot(NewScoreRobot::class, block)
    }

    fun clickContinueDropdownMenuItem(block: AddEndRobot.() -> Unit = {}) {
        clickDropdownMenuItem(CommonStrings.CONTINUE_MENU_ITEM)
        createRobot(AddEndRobot::class, block)
    }

    fun clickScorePadDropdownMenuItem(block: ScorePadRobot.() -> Unit = {}) {
        clickDropdownMenuItem(CommonStrings.SCORE_PAD_MENU_ITEM)
        createRobot(ScorePadRobot::class, block)
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
        perform {
            +CodexNodeMatcher.HasTestTag(ViewScoresTestTag.DROPDOWN_MENU_ITEM)
            +CodexNodeMatcher.HasAnyDescendant(CodexNodeMatcher.HasText(menuItem))
            +CodexNodeInteraction.AssertDoesNotExist()
        }
    }

    fun chooseConvertDialogOption(convertType: String) {
        checkDialogIsDisplayed(CONVERT_SCORE_DIALOG_TITLE)
        perform {
            allNodes(CodexNodeMatcher.HasTestTag(RadioButtonDialogTestTag.RADIO_BUTTON))
            +CodexNodeGroupToOne.Filter(CodexNodeMatcher.HasText(convertType))
            +CodexNodeInteraction.PerformClick()
        }
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
        performOnRowItem(
                rowIndex,
                CodexNodeInteraction.Assert(
                        CodexNodeMatcher.HasAnyDescendant(CodexNodeMatcher.HasText(text, true))
                ).waitFor()
        )
    }

    private fun waitForTextInRow(
            rowIndex: Int,
            testTag: CodexTestTag,
            text: String?,
    ) {
        perform {
            inRow(rowIndex)
            +CodexNodeMatcher.HasTestTag(testTag)
            assertTextEqualsOrNotExist(text)
        }
    }

    fun waitForHsg(rowIndex: Int, hsg: String?) =
            waitForTextInRow(rowIndex, ViewScoresEntryRowTestTag.HSG, hsg ?: "-/-/-")

    fun waitForDate(rowIndex: Int, date: String) = waitForTextInRow(rowIndex, ViewScoresEntryRowTestTag.DATE, date)
    fun waitForHandicap(rowIndex: Int, handicap: Int?) =
            waitForTextInRow(rowIndex, ViewScoresEntryRowTestTag.HANDICAP, handicap?.toString())

    fun waitForRoundName(rowIndex: Int, roundName: String?) =
            waitForTextInRow(rowIndex, ViewScoresEntryRowTestTag.FIRST_NAME, roundName)

    fun checkContentDescription(rowIndex: Int, vararg description: String) {
        perform {
            +CodexNodeMatcher.HasTestTag(viewScoresListItemTestTag(rowIndex))
            +CodexNodeInteraction.AssertContentDescriptionEquals(description.toList())
        }
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
        createRobot(EmailScoreRobot::class, block)
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
        perform {
            allNodes(CodexNodeMatcher.HasTestTag(ViewScoresTestTag.LIST_ITEM, true))
            +CodexNodeGroupInteraction.ForEach(
                    (0 until totalEntries).map {
                        listOf(
                                CodexNodeInteraction.AssertIsSelectable(),
                                CodexNodeInteraction.AssertIsSelected(rowIndexes.contains(it)),
                        )
                    }
            )
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
