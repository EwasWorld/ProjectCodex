package eywa.projectcodex.instrumentedTests.robots.archerRoundScore

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.stats.ArcherRoundStatsScreen.TestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.robots.subComponents.SelectFaceRobot

class ArcherRoundStatsRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ArcherRoundRobot(composeTestRule, TestTag.SCREEN) {
    val facesRobot = SelectFaceRobot(composeTestRule, TestTag.SCREEN)

    fun checkDate(text: String) {
        composeTestRule
                .onNodeWithTag(TestTag.DATE_TEXT)
                .assertTextEquals(text)
    }

    fun checkRound(text: String) {
        composeTestRule
                .onNodeWithTag(TestTag.ROUND_TEXT)
                .assertTextEquals(text)
    }

    fun checkNoRound() {
        composeTestRule
                .onNodeWithTag(TestTag.ROUND_TEXT)
                .assertTextEquals("N/A")
    }

    fun checkHits(text: String) {
        composeTestRule
                .onNodeWithTag(TestTag.HITS_TEXT)
                .assertTextEquals(text)
    }

    fun checkScore(text: Int) {
        composeTestRule
                .onNodeWithTag(TestTag.SCORE_TEXT)
                .assertTextEquals(text.toString())
    }

    fun checkGolds(text: Int) {
        composeTestRule
                .onNodeWithTag(TestTag.GOLDS_TEXT)
                .assertTextEquals(text.toString())
    }

    fun checkRemainingArrows(text: Int) {
        composeTestRule
                .onNodeWithTag(TestTag.REMAINING_ARROWS_TEXT)
                .assertTextEquals(text.toString())
    }

    fun checkNoRemainingArrows() {
        composeTestRule
                .onNodeWithTag(TestTag.REMAINING_ARROWS_TEXT)
                .assertDoesNotExist()
    }

    fun checkHandicap(text: Int) {
        composeTestRule
                .onNodeWithTag(TestTag.HANDICAP_TEXT)
                .assertTextEquals(text.toString())
    }

    fun checkNoHandicap() {
        composeTestRule
                .onNodeWithTag(TestTag.HANDICAP_TEXT)
                .assertDoesNotExist()
    }

    fun checkPredictedScore(text: Int) {
        composeTestRule
                .onNodeWithTag(TestTag.PREDICTED_SCORE_TEXT)
                .assertTextEquals(text.toString())
    }

    fun checkNoPredictedScore() {
        composeTestRule
                .onNodeWithTag(TestTag.PREDICTED_SCORE_TEXT)
                .assertDoesNotExist()
    }
}
