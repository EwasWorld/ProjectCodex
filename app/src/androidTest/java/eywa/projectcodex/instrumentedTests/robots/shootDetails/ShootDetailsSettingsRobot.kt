package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.settings.SettingsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeAction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher

class ShootDetailsSettingsRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, SettingsTestTag.SCREEN) {
    fun setAddEndSize(size: Int) {
        perform {
            +CodexNodeMatcher.HasTestTag(SettingsTestTag.ADD_END_SIZE)
            +CodexNodeAction.SetText(size.toString())
        }
    }

    fun setScorePadEndSize(size: Int) {
        perform {
            +CodexNodeMatcher.HasTestTag(SettingsTestTag.SCORE_PAD_END_SIZE)
            +CodexNodeAction.SetText(size.toString())
        }
    }
}
