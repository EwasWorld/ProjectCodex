package eywa.projectcodex.instrumentedTests.robots.selectRound

import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogTestTag
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.dsl.TestActionDsl

@SelectRoundBaseDsl
class SelectRoundSubTypeRobot internal constructor(val perform: (TestActionDsl.() -> Unit) -> Unit) {
    init {
        perform {
            +CodexNodeMatcher.HasTestTag(SelectRoundDialogTestTag.SUBTYPE_DIALOG)
            +CodexNodeInteraction.AssertIsDisplayed().waitFor()
        }
    }

    fun clickSubtypeDialogSubtype(displayName: String, index: Int = 0) {
        perform {
            useUnmergedTree = true
            allNodes(
                    CodexNodeMatcher.HasTestTag(SelectRoundDialogTestTag.ROUND_DIALOG_ITEM),
                    *displayName
                            .split(" ")
                            .map { CodexNodeMatcher.HasAnyChild(CodexNodeMatcher.HasText(it)) }
                            .toTypedArray()
            )
            +CodexNodeGroupToOne.Index(index)
            +CodexNodeInteraction.PerformClick().waitFor()
        }
    }
}
