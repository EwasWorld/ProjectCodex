package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsTestTag
import eywa.projectcodex.components.shootDetails.insertEnd.InsertEndTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher

class InsertEndRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ArrowInputsRobot(composeTestRule, InsertEndTestTag.SCREEN) {
    fun clickCancel() = clickArrowInputsCancel()

    fun clickComplete() = clickArrowInputsSubmit()

    fun checkInsertEndBefore(endNumber: Int) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(ArrowInputsTestTag.CONTENT_TEXT)
            +CodexNodeInteraction.AssertTextEquals(
                    if (endNumber == 1) "Inserting end\nat the beginning"
                    else "Inserting end\nbetween ends ${endNumber - 1} and $endNumber"
            )
        }
    }
}
