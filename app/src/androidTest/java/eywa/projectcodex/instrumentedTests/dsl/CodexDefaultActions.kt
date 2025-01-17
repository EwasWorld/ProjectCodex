package eywa.projectcodex.instrumentedTests.dsl

import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.instrumentedTests.robots.common.PerformFn

object CodexDefaultActions {
    fun PerformFn.clickDialogOk() = clickDialog(SimpleDialogTestTag.POSITIVE_BUTTON)

    fun PerformFn.clickDialogCancel() = clickDialog(SimpleDialogTestTag.NEGATIVE_BUTTON)

    private fun PerformFn.clickDialog(buttonTag: CodexTestTag) {
        this {
            singleNode {
                +CodexNodeMatcher.HasTestTag(buttonTag)
                +CodexNodeInteraction.PerformClick()
            }
        }
    }

    fun PerformFn.checkDialogIsDisplayed(titleText: String) {
        this {
            singleNode {
                +CodexNodeMatcher.HasTestTag(SimpleDialogTestTag.TITLE)
                +CodexNodeMatcher.HasText(titleText)
                +CodexNodeInteraction.AssertIsDisplayed().waitFor()
            }
        }
    }

    fun TestActionDslSingleNode.First.matchTextBox(testTag: CodexTestTag) {
        useUnmergedTree()
        +CodexNodeMatcher.HasAnyAncestor(CodexNodeMatcher.HasTestTag(testTag))
        +CodexNodeMatcher.HasSetTextAction
    }

    fun TestActionDslGroupNode.First.matchTextBox(testTag: CodexTestTag) {
        useUnmergedTree()
        +CodexNodeMatcher.HasAnyAncestor(CodexNodeMatcher.HasTestTag(testTag))
        +CodexNodeMatcher.HasSetTextAction
    }

    fun TestActionDslSingleNode.First.matchDataRowValue(testTag: CodexTestTag) {
        useUnmergedTree()
        +CodexNodeMatcher.HasAnyAncestor(CodexNodeMatcher.HasTestTag(testTag))
        +CodexNodeMatcher.HasClickAction
    }

    fun TestActionDslRoot.clickDataRow(testTag: CodexTestTag) {
        singleNode {
            matchDataRowValue(testTag)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun TestActionDslRoot.clickDataRow(testTag: CodexTestTag, expectedText: String?) {
        singleNode {
            matchDataRowValue(testTag)
            +CodexNodeInteraction.PerformClick()
            assertTextEqualsOrDoesntExist(expectedText)
        }
    }

    fun TestActionDslRoot.setText(testTag: CodexTestTag, text: String, append: Boolean = false) {
        singleNode {
            matchTextBox(testTag)
            +CodexNodeInteraction.SetText(text, append)
            if (!append) +CodexNodeInteraction.AssertTextEquals(text).waitFor()
            else +CodexNodeInteraction.AssertTextContains(text).waitFor()
        }
    }

    fun TestActionDslRoot.checkInputtedText(testTag: CodexTestTag, text: String) {
        singleNode {
            matchTextBox(testTag)
            +CodexNodeInteraction.AssertTextEquals(text)
        }
    }
}
