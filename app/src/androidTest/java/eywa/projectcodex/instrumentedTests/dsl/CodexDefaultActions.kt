package eywa.projectcodex.instrumentedTests.dsl

import eywa.projectcodex.common.utils.CodexTestTag

object CodexDefaultActions {
    fun TestActionDsl.clickDataRow(testTag: CodexTestTag) {
        useUnmergedTree = true
        +CodexNodeMatcher.HasTestTag(testTag)
        +CodexNodeInteraction.PerformClick
    }

    fun TestActionDsl.setText(testTag: CodexTestTag, text: String) {
        +CodexNodeMatcher.HasTestTag(testTag)
        +CodexNodeInteraction.SetText(text)
    }
}
