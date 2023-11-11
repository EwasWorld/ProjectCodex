package eywa.projectcodex.instrumentedTests.robots.selectRound

import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogTestTag
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.clickDataRow
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.dsl.TestActionDsl

@SelectRoundBaseDsl
class SelectRoundBaseRobot(val perform: (TestActionDsl.() -> Unit) -> Unit) {
    fun checkNoDialogShown() {
        perform {
            perform {
                +CodexNodeMatcher.HasTestTag(SelectRoundDialogTestTag.ROUND_DIALOG)
                +CodexNodeInteraction.AssertDoesNotExist().waitFor()
            }
            perform {
                +CodexNodeMatcher.HasTestTag(SelectRoundDialogTestTag.SUBTYPE_DIALOG)
                +CodexNodeInteraction.AssertDoesNotExist().waitFor()
            }
        }
    }

    fun clickSelectedRound(block: SelectRoundRobot.() -> Unit) {
        perform {
            clickDataRow(SelectRoundDialogTestTag.SELECTED_ROUND_ROW)
        }
        SelectRoundRobot(perform).apply(block)
    }

    fun checkSelectedRound(displayName: String) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(SelectRoundDialogTestTag.SELECTED_ROUND_ROW)
            +CodexNodeMatcher.HasAnyChild(CodexNodeMatcher.HasText(displayName))
            +CodexNodeInteraction.AssertIsDisplayed()
        }
    }

    fun clickSelectedSubtype(block: SelectRoundSubTypeRobot.() -> Unit) {
        perform {
            clickDataRow(SelectRoundDialogTestTag.SELECTED_SUBTYPE_ROW)
        }
        SelectRoundSubTypeRobot(perform).apply(block)
    }

    fun checkSelectedSubtype(displayName: String) {
        perform {
            +CodexNodeMatcher.HasTestTag(SelectRoundDialogTestTag.SELECTED_SUBTYPE_ROW)
            +CodexNodeInteraction.AssertTextEquals(displayName)
        }
    }
}
