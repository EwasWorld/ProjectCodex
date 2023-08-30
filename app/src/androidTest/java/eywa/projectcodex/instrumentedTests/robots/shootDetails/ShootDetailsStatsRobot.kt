package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeAction
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
            +CodexNodeAction.AssertTextEquals(text)
        }
    }

    fun checkRound(text: String) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.ROUND_TEXT)
            +CodexNodeAction.AssertTextEquals(text)
        }
    }

    fun checkNoRound() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.ROUND_TEXT)
            +CodexNodeAction.AssertTextEquals("N/A")
        }
    }

    fun checkHits(text: String) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.HITS_TEXT)
            +CodexNodeAction.AssertTextEquals(text)
        }
    }

    fun checkScore(text: Int) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.SCORE_TEXT)
            +CodexNodeAction.AssertTextEquals(text.toString())
        }
    }

    fun checkGolds(text: Int) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.GOLDS_TEXT)
            +CodexNodeAction.AssertTextEquals(text.toString())
        }
    }

    fun checkRemainingArrows(text: Int) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.REMAINING_ARROWS_TEXT)
            +CodexNodeAction.AssertTextEquals(text.toString())
        }
    }

    fun checkNoRemainingArrows() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.REMAINING_ARROWS_TEXT)
            +CodexNodeAction.AssertDoesNotExist
        }
    }

    fun checkHandicap(text: Int) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.HANDICAP_TEXT)
            +CodexNodeAction.AssertTextEquals(text.toString())
        }
    }

    fun checkNoHandicap() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.HANDICAP_TEXT)
            +CodexNodeAction.AssertDoesNotExist
        }
    }

    fun checkPredictedScore(text: Int) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.PREDICTED_SCORE_TEXT)
            +CodexNodeAction.AssertTextEquals(text.toString())
        }
    }

    fun checkNoPredictedScore() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.PREDICTED_SCORE_TEXT)
            +CodexNodeAction.AssertDoesNotExist
        }
    }
}
