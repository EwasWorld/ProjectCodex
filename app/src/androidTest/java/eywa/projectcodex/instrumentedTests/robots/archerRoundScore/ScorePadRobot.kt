package eywa.projectcodex.instrumentedTests.robots.archerRoundScore

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.CustomConditionWaiter
import eywa.projectcodex.components.archerRoundScore.scorePad.ScorePadScreen
import eywa.projectcodex.components.mainActivity.MainActivity

class ScorePadRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ArcherRoundRobot(composeTestRule) {
    fun waitForLoad() {
        CustomConditionWaiter.waitForComposeCondition {
            composeTestRule
                    .onAllNodesWithTag(ScorePadScreen.TestTag.CELL)
                    .onFirst()
                    .assertIsDisplayed()
        }
    }
}