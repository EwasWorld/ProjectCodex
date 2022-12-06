package eywa.projectcodex.instrumentedTests.robots.archerRoundScore

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.archerRoundScore.stats.ArcherRoundStatsScreen
import eywa.projectcodex.components.mainActivity.MainActivity

class ArcherRoundStatsRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ArcherRoundRobot(composeTestRule) {
    fun checkDate(text: String) {
        composeTestRule
                .onNodeWithTag(ArcherRoundStatsScreen.TestTag.DATE_TEXT)
                .assertTextEquals(text)
    }

    fun checkRound(text: String) {
        composeTestRule
                .onNodeWithTag(ArcherRoundStatsScreen.TestTag.ROUND_TEXT)
                .assertTextEquals(text)
    }

    fun checkNoRound() {
        composeTestRule
                .onNodeWithTag(ArcherRoundStatsScreen.TestTag.ROUND_TEXT)
                .assertTextEquals("N/A")
    }

    fun checkHits(text: String) {
        composeTestRule
                .onNodeWithTag(ArcherRoundStatsScreen.TestTag.HITS_TEXT)
                .assertTextEquals(text)
    }

    fun checkScore(text: Int) {
        composeTestRule
                .onNodeWithTag(ArcherRoundStatsScreen.TestTag.SCORE_TEXT)
                .assertTextEquals(text.toString())
    }

    fun checkGolds(text: Int) {
        composeTestRule
                .onNodeWithTag(ArcherRoundStatsScreen.TestTag.GOLDS_TEXT)
                .assertTextEquals(text.toString())
    }

    fun checkRemainingArrows(text: Int) {
        composeTestRule
                .onNodeWithTag(ArcherRoundStatsScreen.TestTag.REMAINING_ARROWS_TEXT)
                .assertTextEquals(text.toString())
    }

    fun checkNoRemainingArrows() {
        composeTestRule
                .onNodeWithTag(ArcherRoundStatsScreen.TestTag.REMAINING_ARROWS_TEXT)
                .assertDoesNotExist()
    }

    fun checkHandicap(text: Int) {
        composeTestRule
                .onNodeWithTag(ArcherRoundStatsScreen.TestTag.HANDICAP_TEXT)
                .assertTextEquals(text.toString())
    }

    fun checkNoHandicap() {
        composeTestRule
                .onNodeWithTag(ArcherRoundStatsScreen.TestTag.HANDICAP_TEXT)
                .assertDoesNotExist()
    }

    fun checkPredictedScore(text: Int) {
        composeTestRule
                .onNodeWithTag(ArcherRoundStatsScreen.TestTag.PREDICTED_SCORE_TEXT)
                .assertTextEquals(text.toString())
    }

    fun checkNoPredictedScore() {
        composeTestRule
                .onNodeWithTag(ArcherRoundStatsScreen.TestTag.PREDICTED_SCORE_TEXT)
                .assertDoesNotExist()
    }
}