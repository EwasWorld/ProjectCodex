package eywa.projectcodex.instrumentedTests.robots.selectFace

import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogTestTag
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.common.PerformFn

@SelectFaceDsl
class SelectFaceBaseRobot(val perform: PerformFn) {
    fun checkFaces(expectedFacesString: String) {
        perform {
            singleNode {
                useUnmergedTree()
                +CodexNodeMatcher.HasTestTag(SelectRoundFaceDialogTestTag.ROW_TEXT)
                +CodexNodeInteraction.AssertTextEquals(expectedFacesString)
            }
        }
    }

    private fun openDialog() {
        perform {
            singleNode {
                useUnmergedTree()
                +CodexNodeMatcher.HasTestTag(SelectRoundFaceDialogTestTag.ROW_TEXT)
                +CodexNodeInteraction.PerformClick()
            }
        }
    }

    fun openMultiSelectDialog(block: MultiSelectFaceRobot.() -> Unit) {
        openDialog()
        MultiSelectFaceRobot(perform).apply(block)
    }

    fun openSingleSelectDialog(block: SingleSelectFaceRobot.() -> Unit) {
        openDialog()
        SingleSelectFaceRobot(perform).apply(block)
    }
}
