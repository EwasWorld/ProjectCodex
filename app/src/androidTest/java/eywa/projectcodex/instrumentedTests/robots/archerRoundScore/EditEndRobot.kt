package eywa.projectcodex.instrumentedTests.robots.archerRoundScore

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity

class EditEndRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ArrowInputsRobot(composeTestRule, ArrowInputsTestTag.EDIT_SCREEN) {
    fun clickCancel() = clickArrowInputsCancel()

    fun clickComplete() = clickArrowInputsSubmit()

    fun checkEditEnd(endNumber: Int) {
        checkElementText(ArrowInputsTestTag.CONTENT_TEXT, "Editing end $endNumber")
    }
}
