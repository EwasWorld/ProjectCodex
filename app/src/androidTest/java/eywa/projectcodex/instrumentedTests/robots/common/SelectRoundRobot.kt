package eywa.projectcodex.instrumentedTests.robots.common

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogTestTag.FILTER
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogTestTag.ROUND_DIALOG
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogTestTag.ROUND_DIALOG_ITEM
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogTestTag.SELECTED_ROUND_ROW
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogTestTag.SELECTED_SUBTYPE_ROW
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogTestTag.SUBTYPE_DIALOG
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.BaseRobot

class SelectRoundRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
        screenTestTag: CodexTestTag,
) : BaseRobot(composeTestRule, screenTestTag) {
    fun clickSelectedRound() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasAnyAncestor(CodexNodeMatcher.HasTestTag(SELECTED_ROUND_ROW))
            +CodexNodeMatcher.HasClickAction
            +CodexNodeInteraction.PerformClick
        }
        perform {
            +CodexNodeMatcher.HasTestTag(ROUND_DIALOG)
            +CodexNodeInteraction.AssertIsDisplayed.waitFor()
        }
    }

    fun checkSelectRoundDialogOptions(displayNames: List<String>) {
        displayNames.forEach { displayName ->
            perform {
                useUnmergedTree = true
                displayName
                        .split(" ")
                        .forEach { +CodexNodeMatcher.HasAnyDescendant(CodexNodeMatcher.HasText(it)) }
                +CodexNodeMatcher.HasTestTag(ROUND_DIALOG_ITEM)
                +CodexNodeInteraction.AssertIsDisplayed.waitFor()
            }
        }
    }

    fun checkSelectRoundDialogOptionsNotExist(displayNames: List<String>) {
        displayNames.forEach { displayName ->
            perform {
                useUnmergedTree = true
                displayName
                        .split(" ")
                        .forEach { +CodexNodeMatcher.HasAnyDescendant(CodexNodeMatcher.HasText(it)) }
                +CodexNodeMatcher.HasTestTag(ROUND_DIALOG_ITEM)
                +CodexNodeInteraction.AssertDoesNotExist.waitFor()
            }
        }
    }

    fun checkNoFiltersAreOn() {
        Filter.values().forEach {
            perform {
                useUnmergedTree = true
                +CodexNodeMatcher.HasTestTag(FILTER)
                +CodexNodeMatcher.HasAnyChild(CodexNodeMatcher.HasText(it.label))
                scrollToParentIndex = it.index
                +CodexNodeInteraction.AssertIsSelected(false)
            }
        }
    }

    fun clickFilter(filter: Filter, isNowOn: Boolean = true) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(FILTER)
            +CodexNodeMatcher.HasAnyChild(CodexNodeMatcher.HasText(filter.label))
            scrollToParentIndex = filter.index
            +CodexNodeInteraction.PerformClick
        }
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(FILTER)
            +CodexNodeMatcher.HasAnyChild(CodexNodeMatcher.HasText(filter.label))
            +CodexNodeInteraction.AssertIsSelected(isNowOn).waitFor()
        }
    }

    fun clickRoundDialogRound(displayName: String) {
        perform {
            useUnmergedTree = true
            displayName
                    .split(" ")
                    .forEach { +CodexNodeMatcher.HasAnyDescendant(CodexNodeMatcher.HasText(it)) }
            +CodexNodeMatcher.HasTestTag(ROUND_DIALOG_ITEM)
            +CodexNodeInteraction.PerformClick
        }
    }

    fun clickRoundDialogNoRound() {
        clickElement(SimpleDialogTestTag.POSITIVE_BUTTON)
    }

    fun checkSelectedRound(displayName: String) {
        composeTestRule.onNode(
                hasTestTag(SELECTED_ROUND_ROW.getTestTag()).and(hasAnyChild(hasText(displayName))),
                useUnmergedTree = true,
        ).assertIsDisplayed()
    }

    fun checkSelectedSubtype(displayName: String) {
        checkElementText(SELECTED_SUBTYPE_ROW, displayName)
    }

    fun clickSelectedSubtype() {
        composeTestRule.onNode(
                hasParent(hasTestTag(SELECTED_SUBTYPE_ROW.getTestTag())).and(hasClickAction()),
                useUnmergedTree = true,
        ).performClick()
        CustomConditionWaiter.waitForComposeCondition {
            composeTestRule.onNodeWithTag(SUBTYPE_DIALOG.getTestTag()).assertIsDisplayed()
        }
    }

    fun clickSubtypeDialogSubtype(displayName: String, index: Int = 0) {
        composeTestRule.onAllNodes(
                displayName.split(" ").map { hasAnyChild(hasText(it)) }.fold(
                        hasTestTag(ROUND_DIALOG_ITEM.getTestTag())
                ) { a, b -> a.and(b) },
                true,
        )[index].performClick()
    }

    enum class Filter(val label: String, val index: Int) {
        METRIC("Metric", 1),
        IMPERIAL("Imperial", 2),
        INDOOR("Indoor", 3),
        OUTDOOR("Outdoor", 4),
    }
}
