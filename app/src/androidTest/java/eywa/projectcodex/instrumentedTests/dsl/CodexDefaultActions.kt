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
}
