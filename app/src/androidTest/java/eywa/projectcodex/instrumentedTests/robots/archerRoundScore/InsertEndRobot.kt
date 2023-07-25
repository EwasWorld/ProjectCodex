package eywa.projectcodex.instrumentedTests.robots.archerRoundScore

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.archerRoundScore.arrowInputs.ArrowInputsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity

class InsertEndRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ArrowInputsRobot(composeTestRule, ArrowInputsTestTag.INSERT_SCREEN) {
    fun clickCancel() = clickArrowInputsCancel()

    fun clickComplete() = clickArrowInputsSubmit()

    fun checkInsertEndBefore(endNumber: Int) {
        checkElementText(
                ArrowInputsTestTag.CONTENT_TEXT,
                (
                        if (endNumber == 1) "Inserting end\nat the beginning"
                        else "Inserting end\nbetween ends ${endNumber - 1} and ${endNumber}"
                        ),
        )
    }
}
