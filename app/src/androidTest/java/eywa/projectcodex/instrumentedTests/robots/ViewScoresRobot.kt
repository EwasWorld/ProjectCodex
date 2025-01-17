package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.sharedUi.RadioButtonDialogTestTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.viewScores.actionBar.filters.ViewScoresFiltersTestTag
import eywa.projectcodex.components.viewScores.screenUi.ViewScoresRowTestTag
import eywa.projectcodex.components.viewScores.screenUi.ViewScoresTestTag
import eywa.projectcodex.components.viewScores.screenUi.viewScoresListItemTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.dsl.assertTextEqualsOrDoesntExist
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsAddCountRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsAddEndRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.ShootDetailsScorePadRobot

class ViewScoresRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, ViewScoresTestTag.SCREEN) {
    fun waitForLoad() {
        perform {
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
        clickDialogOk("Table is empty")
        createRobot(MainMenuRobot::class) {}
    }

    /**
     * Wait for the number of rows on the screen to be [rowCount]
     */
    fun waitForRowCount(rowCount: Int) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(ViewScoresTestTag.LAZY_COLUMN)
            +CodexNodeInteraction.PerformScrollToIndex(rowCount - 1).waitFor()
        }
        performSingle {
            +CodexNodeMatcher.HasTestTag(viewScoresListItemTestTag(rowCount - 1))
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
        performSingle {
            +CodexNodeMatcher.HasTestTag(viewScoresListItemTestTag(rowCount))
            +CodexNodeInteraction.AssertDoesNotExist().waitFor()
        }
        // If checking doesn't exist waited for a list change,
        // double check it didn't result in too many nodes being removed
        performSingle {
            +CodexNodeMatcher.HasTestTag(viewScoresListItemTestTag(rowCount - 1))
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
    }

    fun scrollToRow(rowIndex: Int) {
        perform {
            singleNode {
                +CodexNodeMatcher.HasTestTag(ViewScoresTestTag.LAZY_COLUMN)
                +CodexNodeInteraction.PerformScrollToIndex(rowIndex)
            }
        }
    }

    private fun performOnRowItem(rowIndex: Int, action: CodexNodeInteraction) {
        scrollToRow(rowIndex)
        perform {
            singleNode {
                +CodexNodeMatcher.HasTestTag(viewScoresListItemTestTag(rowIndex))
                +CodexNodeInteraction.PerformScrollTo()
                +action
            }
        }
    }

    fun clickRowForMultiSelect(rowIndex: Int) {
        performOnRowItem(rowIndex, CodexNodeInteraction.PerformClick())
    }

    fun clickRow(rowIndex: Int, block: ShootDetailsScorePadRobot.() -> Unit = {}) {
        performOnRowItem(rowIndex, CodexNodeInteraction.PerformClick())
        createRobot(ShootDetailsScorePadRobot::class, block)
    }

    fun clickRowCount(rowIndex: Int, block: ShootDetailsAddCountRobot.() -> Unit = {}) {
        performOnRowItem(rowIndex, CodexNodeInteraction.PerformClick())
        createRobot(ShootDetailsAddCountRobot::class, block)
    }

    fun longClickRow(rowIndex: Int) {
        performOnRowItem(rowIndex, CodexNodeInteraction.PerformLongClick())

        perform {
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
        perform {
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

    fun clickContinueDropdownMenuItem(block: ShootDetailsAddEndRobot.() -> Unit = {}) {
        clickDropdownMenuItem(CommonStrings.CONTINUE_MENU_ITEM)
        createRobot(ShootDetailsAddEndRobot::class, block)
    }

    fun clickViewDropdownMenuItem(block: ShootDetailsAddCountRobot.() -> Unit = {}) {
        clickDropdownMenuItem(CommonStrings.VIEW_MENU_ITEM)
        createRobot(ShootDetailsAddCountRobot::class, block)
    }

    fun clickScorePadDropdownMenuItem(block: ShootDetailsScorePadRobot.() -> Unit = {}) {
        clickDropdownMenuItem(CommonStrings.SCORE_PAD_MENU_ITEM)
        createRobot(ShootDetailsScorePadRobot::class, block)
    }

    fun checkDropdownMenuItemNotThere(menuItem: String) {
        // Check at least one menu item is showing
        perform {
            allNodes {
                useUnmergedTree()
                +CodexNodeMatcher.HasTestTag(ViewScoresTestTag.DROPDOWN_MENU_ITEM)
                toSingle(CodexNodeGroupToOne.First) {
                    +CodexNodeInteraction.AssertIsDisplayed().waitFor()
                }
            }
        }
        // Check that the intended menu item is not showing
        perform {
            singleNode {
                +CodexNodeMatcher.HasTestTag(ViewScoresTestTag.DROPDOWN_MENU_ITEM)
                +CodexNodeMatcher.HasAnyDescendant(CodexNodeMatcher.HasText(menuItem))
                +CodexNodeInteraction.AssertDoesNotExist()
            }
        }
    }

    fun chooseConvertDialogOption(convertType: String) {
        checkDialogIsDisplayed(CONVERT_SCORE_DIALOG_TITLE)
        perform {
            allNodes {
                +CodexNodeMatcher.HasTestTag(RadioButtonDialogTestTag.RADIO_BUTTON)
                toSingle(CodexNodeGroupToOne.Filter(CodexNodeMatcher.HasText(convertType))) {
                    +CodexNodeInteraction.PerformClick()
                }
            }
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
        if (text != null) {
            performSingle {
                +CodexNodeMatcher.HasTestTag(ViewScoresTestTag.LAZY_COLUMN)
                +CodexNodeInteraction.PerformScrollToIndex(rowIndex)
            }
        }
        perform {
            singleNode {
                useUnmergedTree()
                +CodexNodeMatcher.HasAnyAncestor(CodexNodeMatcher.HasTestTag(viewScoresListItemTestTag(rowIndex)))
                +CodexNodeMatcher.HasTestTag(testTag)
                +CodexNodeMatcher.IsNotCached
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
        perform {
            singleNode {
                useUnmergedTree()
                +CodexNodeMatcher.HasTestTag(viewScoresListItemTestTag(rowIndex - 1))
                scrollToParentIndex(rowIndex - 1)
            }
        }
        perform {
            singleNode {
                useUnmergedTree()
                +CodexNodeMatcher.HasTestTag(viewScoresListItemTestTag(rowIndex))
                +CodexNodeInteraction.AssertDoesNotExist().waitFor()
            }
        }
    }

    fun checkContentDescription(rowIndex: Int, vararg description: String) {
        perform {
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
        perform {
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
        perform {
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
        perform {
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
        perform {
            singleNode {
                +CodexNodeMatcher.HasTestTag(ViewScoresFiltersTestTag.COLLAPSED_BUTTON)
                +CodexNodeInteraction.PerformClick()
            }
        }
        ViewScoresFiltersRobot(::perform, ::performDatePickerDateSelection).apply(block)
    }

    fun checkFiltersCount(count: Int) {
        perform {
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
