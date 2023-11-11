package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.assertTextEqualsOrNotExist
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.selectFace.SelectFaceBaseRobot

class ShootDetailsStatsRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, StatsTestTag.SCREEN) {
    val facesRobot = SelectFaceBaseRobot(::perform)

    fun checkDate(text: String) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.DATE_TEXT)
            +CodexNodeInteraction.AssertTextEquals(text)
        }
    }

    fun checkRound(text: String?) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.ROUND_TEXT)
            +CodexNodeInteraction.AssertTextEquals(text ?: "N/A")
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

    fun checkRemainingArrows(text: Int?) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.REMAINING_ARROWS_TEXT)
            assertTextEqualsOrNotExist(text?.toString())
        }
    }

    fun checkHandicap(text: Int?) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.HANDICAP_TEXT)
            assertTextEqualsOrNotExist(text?.toString())
        }
    }

    fun checkPredictedScore(text: Int?) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.PREDICTED_SCORE_TEXT)
            assertTextEqualsOrNotExist(text?.toString())
        }
    }

    fun checkPb(isPb: Boolean = true, isTiedPb: Boolean = false) {
        val text = when {
            !isPb -> null
            isTiedPb -> "Tied personal best"
            else -> "Personal best!"
        }
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.PB_TEXT)
            assertTextEqualsOrNotExist(text)
        }
    }

    fun checkAllowance(text: Int?) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.ALLOWANCE_TEXT)
            assertTextEqualsOrNotExist(text?.toString())
        }
    }

    fun checkArcherHandicap(text: Int?) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.ARCHER_HANDICAP_TEXT)
            assertTextEqualsOrNotExist(text?.toString())
        }
    }

    fun checkAdjustedScore(text: Int?) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.ADJUSTED_SCORE_TEXT)
            assertTextEqualsOrNotExist(text?.toString())
        }
    }

    fun checkPastRecordsTextNotShown() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.PAST_RECORDS_LINK_TEXT)
            +CodexNodeInteraction.AssertDoesNotExist()
        }
    }

    fun clickPastRecordsText() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.PAST_RECORDS_LINK_TEXT)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun checkPastRecordsDialogItems(items: List<PastRecordsDialogItem>) {
        perform {
            useUnmergedTree = true
            allNodes(CodexNodeMatcher.HasTestTag(StatsTestTag.PAST_RECORDS_DIALOG_ITEM))
            +CodexNodeGroupInteraction.ForEach(
                    listOf(items.map { CodexNodeInteraction.AssertContentDescriptionEquals(it.semanticText) })
            )
        }
    }

    @JvmInline
    value class PastRecordsDialogItem private constructor(private val data: Triple<String, Int, String?>) {
        constructor(date: String, score: Int, pbSemanticText: String? = null)
                : this(Triple(date, score, pbSemanticText?.takeIf { it.isNotBlank() }))

        private val date
            get() = data.first
        private val score
            get() = data.second

        /**
         * - Is pb
         * - Is current round
         */
        private val semanticTextExtra
            get() = data.third?.let { " - $it" } ?: ""

        val semanticText
            get() = "$date - $score$semanticTextExtra"
    }
}
