package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.sharedUi.RadioButtonDialogTestTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.viewScores.actionBar.filters.ViewScoresFiltersTestTag
import eywa.projectcodex.components.viewScores.screenUi.ViewScoresRowTestTag
import eywa.projectcodex.components.viewScores.screenUi.ViewScoresTestTag
import eywa.projectcodex.components.viewScores.screenUi.viewScoresListItemTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.checkDialogIsDisplayed
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.click
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.clickDialogCancel
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.clickDialogOk
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.dsl.assertTextEqualsOrDoesntExist
import eywa.projectcodex.instrumentedTests.robots.shootDetails.AddCountRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.AddEndRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ScorePadRobot

class ViewScoresRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, ViewScoresTestTag.SCREEN) {
    fun waitForLoad() {
        performV2 {
            allNodes {
                useUnmergedTree()
                +CodexNodeMatcher.HasTestTag(ViewScoresTestTag.LIST_ITEM, true)
                toSingle(CodexNodeGroupToOne.First) {
                    +CodexNodeInteraction.AssertIsDisplayed().waitFor()
                }
            }
        }
    }

    fun clickOkOnEmptyTableDialog() {
        performV2 {
            checkDialogIsDisplayed("Table is empty")
        }
        performV2 {
            clickDialogOk()
        }
        createRobot(MainMenuRobot::class) {}
    }

    /**
     * Wait for the number of rows on the screen to be [rowCount]
     */
    fun waitForRowCount(rowCount: Int) {
        performV2 {
            allNodes {
                useUnmergedTree()
                +CodexNodeMatcher.HasTestTag(ViewScoresTestTag.LIST_ITEM, substring = true)
                +CodexNodeMatcher.IsNotCached
                +CodexNodeGroupInteraction.AssertCount(rowCount).waitFor()
            }
        }
    }

    fun scrollToRow(rowIndex: Int) {
        performV2 {
            singleNode {
                +CodexNodeMatcher.HasTestTag(ViewScoresTestTag.LAZY_COLUMN)
                +CodexNodeInteraction.PerformScrollToIndex(rowIndex)
            }
        }
    }

    private fun performOnRowItem(rowIndex: Int, action: CodexNodeInteraction) {
        scrollToRow(rowIndex)
        performV2 {
            singleNode {
                +CodexNodeMatcher.HasTestTag(viewScoresListItemTestTag(rowIndex))
                +action
            }
        }
    }

    fun clickRowForMultiSelect(rowIndex: Int) {
        performOnRowItem(rowIndex, CodexNodeInteraction.PerformClick())
    }

    fun clickRow(rowIndex: Int, block: ScorePadRobot.() -> Unit = {}) {
        performOnRowItem(rowIndex, CodexNodeInteraction.PerformClick())
        createRobot(ScorePadRobot::class, block)
    }

    fun clickRowCount(rowIndex: Int, block: AddCountRobot.() -> Unit = {}) {
        performOnRowItem(rowIndex, CodexNodeInteraction.PerformClick())
        createRobot(AddCountRobot::class, block)
    }

    fun longClickRow(rowIndex: Int) {
        performOnRowItem(rowIndex, CodexNodeInteraction.PerformLongClick())

        performV2 {
            allNodes {
                useUnmergedTree()
                +CodexNodeMatcher.HasTestTag(ViewScoresTestTag.DROPDOWN_MENU_ITEM)
                toSingle(CodexNodeGroupToOne.First) {
                    +CodexNodeInteraction.AssertIsDisplayed().waitFor()
                }
            }
        }
    }

    fun clickDropdownMenuItem(menuItem: String) {
        performV2 {
            singleNode {
                useUnmergedTree()
                +CodexNodeMatcher.HasTestTag(ViewScoresTestTag.DROPDOWN_MENU_ITEM)
                +CodexNodeMatcher.HasAnyDescendant(CodexNodeMatcher.HasText(menuItem))
                +CodexNodeInteraction.PerformClick().waitFor()
            }
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

    fun clickViewDropdownMenuItem(block: AddCountRobot.() -> Unit = {}) {
        clickDropdownMenuItem(CommonStrings.VIEW_MENU_ITEM)
        createRobot(AddCountRobot::class, block)
    }

    fun clickScorePadDropdownMenuItem(block: ScorePadRobot.() -> Unit = {}) {
        clickDropdownMenuItem(CommonStrings.SCORE_PAD_MENU_ITEM)
        createRobot(ScorePadRobot::class, block)
    }

    fun checkDropdownMenuItemNotThere(menuItem: String) {
        // Check at least one menu item is showing
        performV2 {
            allNodes {
                useUnmergedTree()
                +CodexNodeMatcher.HasTestTag(ViewScoresTestTag.DROPDOWN_MENU_ITEM)
                toSingle(CodexNodeGroupToOne.First) {
                    +CodexNodeInteraction.AssertIsDisplayed().waitFor()
                }
            }
        }
        // Check that the intended menu item is not showing
        performV2 {
            singleNode {
                +CodexNodeMatcher.HasTestTag(ViewScoresTestTag.DROPDOWN_MENU_ITEM)
                +CodexNodeMatcher.HasAnyDescendant(CodexNodeMatcher.HasText(menuItem))
                +CodexNodeInteraction.AssertDoesNotExist()
            }
        }
    }

    fun chooseConvertDialogOption(convertType: String) {
        performV2 {
            checkDialogIsDisplayed(CONVERT_SCORE_DIALOG_TITLE)
        }
        performV2 {
            allNodes {
                +CodexNodeMatcher.HasTestTag(RadioButtonDialogTestTag.RADIO_BUTTON)
                toSingle(CodexNodeGroupToOne.Filter(CodexNodeMatcher.HasText(convertType))) {
                    +CodexNodeInteraction.PerformClick()
                }
            }
        }
    }

    fun clickConvertDialogOk() {
        performV2 {
            checkDialogIsDisplayed(CONVERT_SCORE_DIALOG_TITLE)
        }
        performV2 {
            clickDialogOk()
        }
    }

    fun clickConvertDialogCancel() {
        performV2 {
            checkDialogIsDisplayed(CONVERT_SCORE_DIALOG_TITLE)
        }
        performV2 {
            clickDialogCancel()
        }
    }

    fun clickDeleteDialogOk() {
        performV2 {
            checkDialogIsDisplayed(DELETE_ENTRY_DIALOG_TITLE)
        }
        performV2 {
            clickDialogOk()
        }
    }

    fun clickDeleteDialogCancel() {
        performV2 {
            checkDialogIsDisplayed(DELETE_ENTRY_DIALOG_TITLE)
        }
        performV2 {
            clickDialogCancel()
        }
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
        performV2 {
            singleNode {
                useUnmergedTree()
                +CodexNodeMatcher.HasAnyAncestor(CodexNodeMatcher.HasTestTag(viewScoresListItemTestTag(rowIndex)))
                +CodexNodeMatcher.HasTestTag(testTag)
                +CodexNodeMatcher.IsNotCached
                if (text != null) {
                    scrollToParentIndex(rowIndex)
                }
                +assertTextEqualsOrDoesntExist(text).waitFor()
            }
        }
    }

    fun waitForArrowCount(rowIndex: Int, count: Int) =
            waitForTextInRow(rowIndex, ViewScoresRowTestTag.COUNT, count.toString())

    fun waitForHsg(rowIndex: Int, hsg: String?) =
            waitForTextInRow(rowIndex, ViewScoresRowTestTag.HSG, hsg ?: "-/-/-")

    fun waitForDate(rowIndex: Int, date: String) = waitForTextInRow(rowIndex, ViewScoresRowTestTag.DATE, date)
    fun waitForHandicap(rowIndex: Int, handicap: Int?) =
            waitForTextInRow(rowIndex, ViewScoresRowTestTag.HANDICAP, handicap?.toString())

    fun waitForRoundName(rowIndex: Int, roundName: String?) =
            waitForTextInRow(rowIndex, ViewScoresRowTestTag.FIRST_NAME, roundName)

    fun waitForRowNotExist(rowIndex: Int) {
        performV2 {
            singleNode {
                useUnmergedTree()
                +CodexNodeMatcher.HasTestTag(viewScoresListItemTestTag(rowIndex - 1))
                scrollToParentIndex(rowIndex - 1)
            }
        }
        performV2 {
            singleNode {
                useUnmergedTree()
                +CodexNodeMatcher.HasTestTag(viewScoresListItemTestTag(rowIndex))
                +CodexNodeInteraction.AssertDoesNotExist().waitFor()
            }
        }
    }

    fun checkContentDescription(rowIndex: Int, vararg description: String) {
        performV2 {
            singleNode {
                +CodexNodeMatcher.HasTestTag(viewScoresListItemTestTag(rowIndex))
                +CodexNodeInteraction.AssertContentDescriptionEquals(description.toList()).waitFor()
            }
        }
    }

    /**
     * @param items accepts [String]s (HSG) and [Int]s (arrow count). Order is the order they should appear
     */
    fun checkRows(items: List<Any>) {
        items.forEachIndexed { index, expected ->
            when (expected) {
                is String -> waitForHsg(index, expected)
                is Int -> waitForArrowCount(index, expected)
            }
        }
//        waitForRowNotExist(items.size)
    }

    /**
     * @see checkRows
     */
    fun checkRows(vararg items: Any) = checkRows(items.toList())

    fun clickStartMultiSelectMode() {
        performV2 {
            click(ViewScoresTestTag.MULTI_SELECT_START)
        }
    }

    fun clickMultiSelectSelectAll() {
        performV2 {
            click(ViewScoresTestTag.MULTI_SELECT_ALL)
        }
    }

    fun clickCancelMultiSelectMode() {
        performV2 {
            click(ViewScoresTestTag.MULTI_SELECT_CANCEL)
        }
    }

    fun clickMultiSelectEmail(block: EmailScoreRobot.() -> Unit = {}) {
        performV2 {
            click(ViewScoresTestTag.MULTI_SELECT_EMAIL)
        }
        createRobot(EmailScoreRobot::class, block)
    }

    fun checkMultiSelectMode(isInMultiSelectMode: Boolean = true) {
        performV2 {
            singleNode {
                +CodexNodeMatcher.HasTestTag(
                        if (isInMultiSelectMode) ViewScoresTestTag.MULTI_SELECT_CANCEL
                        else ViewScoresTestTag.MULTI_SELECT_START
                )
                +CodexNodeInteraction.AssertIsDisplayed()
            }
        }
    }

    fun checkEntriesNotSelectable() {
        performV2 {
            allNodes {
                +CodexNodeMatcher.HasTestTag(ViewScoresTestTag.LIST_ITEM)
                +CodexNodeGroupInteraction.AssertAll(CodexNodeMatcher.IsNotSelectable)
            }
        }
    }

    /**
     * Check that all rows are selectable
     * and that [rowIndexes] are selected and all other rows are not selected
     */
    fun checkEntriesSelected(rowIndexes: Iterable<Int>, totalEntries: Int) {
        performV2 {
            allNodes {
                +CodexNodeMatcher.HasTestTag(ViewScoresTestTag.LIST_ITEM, true)
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
    }

    fun clickFilters(block: ViewScoresFiltersRobot.() -> Unit) {
        performV2 {
            singleNode {
                +CodexNodeMatcher.HasTestTag(ViewScoresFiltersTestTag.COLLAPSED_BUTTON)
                +CodexNodeInteraction.PerformClick()
            }
        }
        ViewScoresFiltersRobot(::performV2, ::performDatePickerDateSelection).apply(block)
    }

    fun checkFiltersCount(count: Int) {
        performV2 {
            singleNode {
                useUnmergedTree()
                +CodexNodeMatcher.HasTestTag(ViewScoresFiltersTestTag.COLLAPSED_FILTER_COUNT)
                +CodexNodeInteraction.AssertTextEquals(count.toString())
            }
        }
    }

    object CommonStrings {
        const val SCORE_PAD_MENU_ITEM = "Score pad"
        const val CONTINUE_MENU_ITEM = "Continue"
        const val VIEW_MENU_ITEM = "View"
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
