package eywa.projectcodex.instrumentedTests.dsl

import eywa.projectcodex.common.sharedUi.SimpleDialogTestTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.instrumentedTests.robots.common.PerformFnV2

object CodexDefaultActions {
    fun PerformFnV2.clickDialogOk() =
            clickDialog(SimpleDialogTestTag.POSITIVE_BUTTON)

    fun PerformFnV2.clickDialogCancel() =
            clickDialog(SimpleDialogTestTag.NEGATIVE_BUTTON)

    private fun PerformFnV2.clickDialog(buttonTag: CodexTestTag) {
        this {
            singleNode {
                +CodexNodeMatcher.HasTestTag(buttonTag)
                +CodexNodeInteraction.PerformClick()
            }
        }
    }

    fun PerformFnV2.checkDialogIsDisplayed(titleText: String) {
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

    fun TestActionDslV2.clickDataRow(testTag: CodexTestTag, expectedText: String?) {
        singleNode {
            matchDataRowValue(testTag)
            +CodexNodeInteraction.PerformClick()
            assertTextEqualsOrDoesntExist(expectedText)
        }
    }

    fun TestActionDslV2.setText(testTag: CodexTestTag, text: String, append: Boolean = false) {
        singleNode {
            matchTextBox(testTag)
            +CodexNodeInteraction.SetText(text, append)
        }
    }

    fun TestActionDslV2.checkInputtedText(testTag: CodexTestTag, text: String) {
        singleNode {
            matchTextBox(testTag)
            +CodexNodeInteraction.AssertTextEquals(text)
        }
    }
}
