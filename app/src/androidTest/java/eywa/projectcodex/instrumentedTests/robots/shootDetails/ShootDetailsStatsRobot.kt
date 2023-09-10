package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.common.SelectFaceRobot

class ShootDetailsStatsRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, StatsTestTag.SCREEN) {
    val facesRobot = SelectFaceRobot(composeTestRule, StatsTestTag.SCREEN)

    fun checkDate(text: String) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.DATE_TEXT)
            +CodexNodeInteraction.AssertTextEquals(text)
        }
    }

    fun checkRound(text: String) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.ROUND_TEXT)
            +CodexNodeInteraction.AssertTextEquals(text)
        }
    }

    fun checkNoRound() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.ROUND_TEXT)
            +CodexNodeInteraction.AssertTextEquals("N/A")
        }
    }

    fun checkHits(text: String) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.HITS_TEXT)
            +CodexNodeInteraction.AssertTextEquals(text)
        }
    }

    fun checkScore(text: Int) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.SCORE_TEXT)
            +CodexNodeInteraction.AssertTextEquals(text.toString())
        }
    }

    fun checkGolds(text: Int) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.GOLDS_TEXT)
            +CodexNodeInteraction.AssertTextEquals(text.toString())
        }
    }

    fun checkRemainingArrows(text: Int) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.REMAINING_ARROWS_TEXT)
            +CodexNodeInteraction.AssertTextEquals(text.toString())
        }
    }

    fun checkNoRemainingArrows() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.REMAINING_ARROWS_TEXT)
            +CodexNodeInteraction.AssertDoesNotExist
        }
    }

    fun checkHandicap(text: Int) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.HANDICAP_TEXT)
            +CodexNodeInteraction.AssertTextEquals(text.toString())
        }
    }

    fun checkNoHandicap() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.HANDICAP_TEXT)
            +CodexNodeInteraction.AssertDoesNotExist
        }
    }

    fun checkPredictedScore(text: Int) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.PREDICTED_SCORE_TEXT)
            +CodexNodeInteraction.AssertTextEquals(text.toString())
        }
    }

    fun checkNoPredictedScore() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.PREDICTED_SCORE_TEXT)
            +CodexNodeInteraction.AssertDoesNotExist
        }
    }
}
