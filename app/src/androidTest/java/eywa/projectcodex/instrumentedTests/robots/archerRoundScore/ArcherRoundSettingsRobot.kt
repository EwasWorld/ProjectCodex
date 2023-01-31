package eywa.projectcodex.instrumentedTests.robots.archerRoundScore

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.archerRoundScore.settings.ArcherRoundSettingsScreen.TestTag
import eywa.projectcodex.components.mainActivity.MainActivity

class ArcherRoundSettingsRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ArcherRoundRobot(composeTestRule) {
    fun setInputEndSize(size: Int) {
        composeTestRule.onNodeWithTag(TestTag.INPUT_END_SIZE).performTextClearance()
        composeTestRule.onNodeWithTag(TestTag.INPUT_END_SIZE).performTextInput(size.toString())
    }

    fun setScorePadEndSize(size: Int) {
        composeTestRule.onNodeWithTag(TestTag.SCORE_PAD_END_SIZE).performTextClearance()
        composeTestRule.onNodeWithTag(TestTag.SCORE_PAD_END_SIZE).performTextInput(size.toString())
    }
}
