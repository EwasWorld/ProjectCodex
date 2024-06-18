package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.settings.SettingsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity

class ShootDetailsSettingsRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, SettingsTestTag.SCREEN) {
    fun setAddEndSize(size: Int) {
        setText(SettingsTestTag.ADD_END_SIZE, size.toString())
    }

    fun setScorePadEndSize(size: Int) {
        setText(SettingsTestTag.SCORE_PAD_END_SIZE, size.toString())
    }
}
