package eywa.projectcodex.instrumentedTests.robots.selectFace

import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogTestTag
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.checkDialogIsDisplayed
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.common.PerformFnV2

@SelectFaceDsl
class SingleSelectFaceRobot internal constructor(val perform: PerformFnV2) {
    init {
        perform.checkDialogIsDisplayed("Select a face")
    }

    fun checkSwitchButton(visible: Boolean = true) {
        perform {
            singleNode {
                +CodexNodeMatcher.HasTestTag(SelectRoundFaceDialogTestTag.SWITCH_TO_MULTI_BUTTON)
                if (visible) +CodexNodeInteraction.AssertIsDisplayed().waitFor()
                else +CodexNodeInteraction.AssertDoesNotExist().waitFor()
            }
        }
        perform {
            singleNode {
                +CodexNodeMatcher.HasTestTag(SelectRoundFaceDialogTestTag.SWITCH_TO_SINGLE_BUTTON)
                +CodexNodeInteraction.AssertDoesNotExist()
            }
        }
    }

    fun clickSwitchButton(block: MultiSelectFaceRobot.() -> Unit) {
        perform {
            singleNode {
                +CodexNodeMatcher.HasTestTag(SelectRoundFaceDialogTestTag.SWITCH_TO_MULTI_BUTTON)
                +CodexNodeInteraction.PerformClick()
            }
        }
        MultiSelectFaceRobot(perform).apply(block)
    }

    fun clickOption(option: String) {
        perform {
            singleNode {
                +CodexNodeMatcher.HasTestTag(SelectRoundFaceDialogTestTag.SINGLE_OPTION)
                +CodexNodeMatcher.HasText(option)
                +CodexNodeInteraction.PerformClick()
            }
        }
    }
}
