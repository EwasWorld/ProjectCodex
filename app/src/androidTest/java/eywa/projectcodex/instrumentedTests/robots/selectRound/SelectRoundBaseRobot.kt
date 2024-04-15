package eywa.projectcodex.instrumentedTests.robots.selectRound

import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogTestTag
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.clickDataRow
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.matchDataRowValue
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.common.PerformFnV2

@SelectRoundDsl
class SelectRoundBaseRobot(val perform: PerformFnV2) {
    fun checkNoDialogShown() {
        perform {
            singleNode {
                +CodexNodeMatcher.HasTestTag(SelectRoundDialogTestTag.ROUND_DIALOG)
                +CodexNodeInteraction.AssertDoesNotExist().waitFor()
            }
        }
        perform {
            singleNode {
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
            singleNode {
                matchDataRowValue(SelectRoundDialogTestTag.SELECTED_ROUND_ROW)
                +CodexNodeInteraction.AssertTextEquals(displayName)
            }
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
            singleNode {
                matchDataRowValue(SelectRoundDialogTestTag.SELECTED_SUBTYPE_ROW)
                +CodexNodeInteraction.AssertTextEquals(displayName)
            }
        }
    }
}
