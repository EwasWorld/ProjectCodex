package eywa.projectcodex.instrumentedTests.dsl

import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.common.utils.CodexTestTag

object CodexDefaultActions {
    fun TestActionDsl.matchDataRowValue(testTag: CodexTestTag) {
        useUnmergedTree = true
        +CodexNodeMatcher.HasAnyAncestor(CodexNodeMatcher.HasTestTag(testTag))
        +CodexNodeMatcher.HasClickAction
    }

    fun TestActionDsl.clickDataRow(testTag: CodexTestTag) {
        matchDataRowValue(testTag)
        +CodexNodeInteraction.PerformClick()
    }

    fun TestActionDsl.matchTextBox(testTag: CodexTestTag) {
        useUnmergedTree = true
        +CodexNodeMatcher.HasAnyAncestor(CodexNodeMatcher.HasTestTag(testTag))
        +CodexNodeMatcher.HasSetTextAction
    }

    fun TestActionDsl.setText(testTag: CodexTestTag, text: String, append: Boolean = false) {
        matchTextBox(testTag)
        +CodexNodeInteraction.SetText(text, append)
    }

    fun TestActionDsl.checkInputtedText(testTag: CodexTestTag, text: String) {
        matchTextBox(testTag)
        +CodexNodeInteraction.AssertTextEquals(text)
    }

    fun TestActionDsl.assertTextEqualsOrNotExist(text: String?) =
            if (text != null) +CodexNodeInteraction.AssertTextEquals(text)
            else +CodexNodeInteraction.AssertDoesNotExist()

    fun TestActionDsl.checkDialogIsDisplayed(titleText: String) {
        +CodexNodeMatcher.HasTestTag(SimpleDialogTestTag.TITLE)
        +CodexNodeMatcher.HasText(titleText)
        +CodexNodeInteraction.AssertIsDisplayed().waitFor()
    }

    fun TestActionDsl.clickDialogOk(titleText: String) =
            clickDialog(titleText, SimpleDialogTestTag.POSITIVE_BUTTON)

    fun TestActionDsl.clickDialogCancel(titleText: String) =
            clickDialog(titleText, SimpleDialogTestTag.NEGATIVE_BUTTON)

    private fun TestActionDsl.clickDialog(titleText: String, buttonTag: CodexTestTag) {
        +CodexNodeMatcher.HasTestTag(buttonTag)
        +CodexNodeInteraction.PerformClick()
    }

    fun TestActionDslV2.clickDialogOk() =
            clickDialog(SimpleDialogTestTag.POSITIVE_BUTTON)

    fun TestActionDslV2.clickDialogCancel() =
            clickDialog(SimpleDialogTestTag.NEGATIVE_BUTTON)

    private fun TestActionDslV2.clickDialog(buttonTag: CodexTestTag) {
        singleNode {
            +CodexNodeMatcher.HasTestTag(buttonTag)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun TestActionDslV2.checkDialogIsDisplayed(titleText: String) {
        singleNode {
            +CodexNodeMatcher.HasTestTag(SimpleDialogTestTag.TITLE)
            +CodexNodeMatcher.HasText(titleText)
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
    }

    fun TestActionDslV2.click(testTag: CodexTestTag) {
        singleNode {
            +CodexNodeMatcher.HasTestTag(testTag)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun TestActionDslSingleNode.First.matchTextBox(testTag: CodexTestTag) {
        useUnmergedTree()
        +CodexNodeMatcher.HasAnyAncestor(CodexNodeMatcher.HasTestTag(testTag))
        +CodexNodeMatcher.HasSetTextAction
    }

    fun TestActionDslSingleNode.First.matchDataRowValue(testTag: CodexTestTag) {
        useUnmergedTree()
        +CodexNodeMatcher.HasAnyAncestor(CodexNodeMatcher.HasTestTag(testTag))
        +CodexNodeMatcher.HasClickAction
    }

    fun TestActionDslV2.clickDataRow(testTag: CodexTestTag) {
        singleNode {
            matchDataRowValue(testTag)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun TestActionDslV2.setText(testTag: CodexTestTag, text: String, append: Boolean = false) {
        singleNode {
            matchTextBox(testTag)
            +CodexNodeInteraction.SetText(text, append)
        }
    }
}
