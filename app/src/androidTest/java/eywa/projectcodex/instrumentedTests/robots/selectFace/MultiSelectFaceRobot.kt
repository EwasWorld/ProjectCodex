package eywa.projectcodex.instrumentedTests.robots.selectFace

import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogTestTag
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.checkDialogIsDisplayed
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.clickDialogOk
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupToOne
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.common.PerformFn

@SelectFaceDsl
class MultiSelectFaceRobot internal constructor(val perform: PerformFn) {
    init {
        perform {
            checkDialogIsDisplayed("Select faces")
        }
    }

    fun checkSwitchButton(visible: Boolean = true) {
        perform {
            +CodexNodeMatcher.HasTestTag(SelectRoundFaceDialogTestTag.SWITCH_TO_SINGLE_BUTTON)
            if (visible) +CodexNodeInteraction.AssertIsDisplayed().waitFor()
            else +CodexNodeInteraction.AssertDoesNotExist().waitFor()
        }
        perform {
            +CodexNodeMatcher.HasTestTag(SelectRoundFaceDialogTestTag.SWITCH_TO_MULTI_BUTTON)
            +CodexNodeInteraction.AssertDoesNotExist()
        }
    }

    fun clickSwitchButton(block: SingleSelectFaceRobot.() -> Unit) {
        perform {
            +CodexNodeMatcher.HasTestTag(SelectRoundFaceDialogTestTag.SWITCH_TO_SINGLE_BUTTON)
            +CodexNodeInteraction.PerformClick()
        }
        SingleSelectFaceRobot(perform).apply(block)
    }

    fun clickOption(distanceIndex: Int, option: String) {
        perform {
            useUnmergedTree = true
            allNodes(
                    CodexNodeMatcher.HasClickAction,
                    CodexNodeMatcher.HasAnyAncestor(CodexNodeMatcher.HasTestTag(SelectRoundFaceDialogTestTag.MULTI_OPTION))
            )
            +CodexNodeGroupToOne.Index(distanceIndex)
            +CodexNodeInteraction.PerformClick()
        }
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(SelectRoundFaceDialogTestTag.MULTI_DROPDOWN_OPTION)
            +CodexNodeMatcher.HasText(option)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun checkOptions(options: List<String>) {
        options.forEachIndexed { index, option ->
            perform {
                allNodes(CodexNodeMatcher.HasTestTag(SelectRoundFaceDialogTestTag.MULTI_OPTION))
                +CodexNodeGroupToOne.Index(index)
                +CodexNodeInteraction.AssertContentDescriptionEquals(option)
            }
        }
    }

    fun clickConfirm() {
        perform {
            clickDialogOk("Select faces")
        }
    }
}