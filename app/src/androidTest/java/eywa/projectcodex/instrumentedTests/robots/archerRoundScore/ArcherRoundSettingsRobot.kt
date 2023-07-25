package eywa.projectcodex.instrumentedTests.robots.archerRoundScore

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.archerRoundScore.settings.ArcherRoundSettingsScreen.TestTag
import eywa.projectcodex.core.mainActivity.MainActivity

class ArcherRoundSettingsRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ArcherRoundRobot(composeTestRule, TestTag.SCREEN) {
    fun setInputEndSize(size: Int) {
        setText(TestTag.INPUT_END_SIZE, size.toString())
    }

    fun setScorePadEndSize(size: Int) {
        setText(TestTag.SCORE_PAD_END_SIZE, size.toString())
    }
}
