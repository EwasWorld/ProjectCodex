package eywa.projectcodex.instrumentedTests.robots

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.about.AboutScreenTestTag
import eywa.projectcodex.components.mainActivity.MainActivity

class AboutRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : BaseRobot(composeTestRule, AboutScreenTestTag.SCREEN) {
    fun checkRoundStatusMessage(text: String) {
        checkElementText(AboutScreenTestTag.UPDATE_TASK_STATUS, "Round status: $text")
    }
}
