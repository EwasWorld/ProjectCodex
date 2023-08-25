package eywa.projectcodex.instrumentedTests.robots.common

import androidx.compose.ui.test.*
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogTestTag.*
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.robots.BaseRobot

class SelectFaceRobot(
        composeTestRule: ComposeTestRule<MainActivity>,
        screenTestTag: String,
) : BaseRobot(composeTestRule, screenTestTag) {
    constructor(
            composeTestRule: ComposeTestRule<MainActivity>,
            screenTestTag: CodexTestTag,
    ) : this(composeTestRule, screenTestTag.getTestTag())

    fun checkFaces(expectedFacesString: String) =
            checkElementText(ROW_TEXT, expectedFacesString, useUnmergedTree = true)

    fun openDialog(isSingleMode: Boolean = true) {
        clickElement(ROW_TEXT, useUnmergedTree = true)
        checkDialogIsDisplayed(if (isSingleMode) "Select a face" else "Select faces")
    }

    fun checkSwitchToMultiButtonIsShown() = checkElementIsDisplayed(SWITCH_TO_MULTI_BUTTON)
    fun checkSwitchToSingleButtonIsShown() = checkElementIsDisplayed(SWITCH_TO_SINGLE_BUTTON)
    fun checkSwitchToMultiButtonNotShown() = checkElementDoesNotExist(SWITCH_TO_MULTI_BUTTON)
    fun checkSwitchToSingleButtonNotShown() = checkElementDoesNotExist(SWITCH_TO_SINGLE_BUTTON)
    fun clickSwitchToMulti() = clickElement(SWITCH_TO_MULTI_BUTTON)
    fun clickSwitchToSingle() = clickElement(SWITCH_TO_SINGLE_BUTTON)

    fun clickSingleOption(option: String) = clickElement(SINGLE_OPTION, option)
    fun clickMultiOption(distanceIndex: Int, option: String) {
        composeTestRule.onAllNodes(
                hasParent(hasTestTag(MULTI_OPTION.getTestTag())).and(hasClickAction()),
                useUnmergedTree = true
        )[distanceIndex].performClick()
        clickElement(MULTI_DROPDOWN_OPTION, text = option, useUnmergedTree = true)
    }

    fun checkMultiOptions(options: List<String>) {
        options.forEachIndexed { index, option ->
            composeTestRule.onAllNodesWithTag(MULTI_OPTION.getTestTag())[index].assertContentDescriptionEquals(option)
        }
    }

    fun clickConfirm() = clickDialogOk("Select faces")
}
