package eywa.projectcodex.instrumentedTests.robots.archerRoundScore

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.mainActivity.MainActivity

class InsertEndRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ArrowInputsRobot(composeTestRule) {
    fun clickCancel() = clickArrowInputsCancel()

    fun clickComplete() = clickArrowInputsSubmit()
}
