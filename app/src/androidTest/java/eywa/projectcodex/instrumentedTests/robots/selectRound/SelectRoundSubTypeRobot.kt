package eywa.projectcodex.instrumentedTests.robots.selectRound

import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogTestTag
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.common.PerformFn

@SelectRoundDsl
class SelectRoundSubTypeRobot internal constructor(val perform: PerformFn) {
    init {
        perform {
            singleNode {
                +CodexNodeMatcher.HasTestTag(SelectRoundDialogTestTag.SUBTYPE_DIALOG)
                +CodexNodeInteraction.AssertIsDisplayed().waitFor()
            }
        }
    }

    fun clickSubtypeDialogSubtype(displayName: String, index: Int = 0) {
        perform {
            allNodes {
                useUnmergedTree()
                +CodexNodeMatcher.HasTestTag(SelectRoundDialogTestTag.ROUND_DIALOG_ITEM)
                displayName.split(" ")
                        .forEach { +CodexNodeMatcher.HasAnyChild(CodexNodeMatcher.HasText(it)) }
                toSingle(CodexNodeGroupToOne.Index(index)) {
                    +CodexNodeInteraction.PerformClick().waitFor()
                }
            }
        }
    }
}
