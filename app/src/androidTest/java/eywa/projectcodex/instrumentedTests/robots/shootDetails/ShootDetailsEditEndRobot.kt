package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsTestTag
import eywa.projectcodex.components.shootDetails.editEnd.EditEndTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher

class ShootDetailsEditEndRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ArrowInputsRobot(composeTestRule, EditEndTestTag.SCREEN) {
    fun clickCancel() = clickArrowInputsCancel()

    fun clickComplete() = clickArrowInputsSubmit()

    fun checkEditEnd(endNumber: Int) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(ArrowInputsTestTag.CONTENT_TEXT)
            +CodexNodeInteraction.AssertTextEquals("Editing end $endNumber")
        }
    }
}
