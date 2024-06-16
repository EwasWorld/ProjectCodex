package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.sharedUi.TabSwitcherTestTag
import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.common.utils.classificationTables.model.Classification.*
import eywa.projectcodex.components.shootDetails.stats.ui.StatsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexDefaultActions.assertTextEqualsOrNotExist
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.ArcherHandicapRobot
import eywa.projectcodex.instrumentedTests.robots.ArcherInfoRobot
import eywa.projectcodex.instrumentedTests.robots.ClassificationTablesRobot
import eywa.projectcodex.instrumentedTests.robots.HandicapTablesRobot
import eywa.projectcodex.instrumentedTests.robots.NewScoreRobot
import eywa.projectcodex.instrumentedTests.robots.selectFace.SelectFaceBaseRobot

class ShootDetailsStatsRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, StatsTestTag.SCREEN) {
    val facesRobot = SelectFaceBaseRobot(::performV2)

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

    fun clickEditRoundData(block: NewScoreRobot.() -> Unit) {
        perform {
            +CodexNodeMatcher.HasTestTag(StatsTestTag.EDIT_SHOOT_INFO)
            +CodexNodeMatcher.HasAnyAncestor(CodexNodeMatcher.HasTestTag(StatsTestTag.SHOOT_DETAIL_SECTION))
            +CodexNodeInteraction.PerformClick()
        }

        createRobot(NewScoreRobot::class, block)
    }

    fun checkHits(hits: Int, totalShot: Int?) {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.HITS_TEXT)
            +CodexNodeInteraction.AssertTextEquals(hits.toString())
        }
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.HITS_OF_TEXT)
            if (totalShot != null) +CodexNodeInteraction.AssertTextEquals("(of $totalShot)")
            else +CodexNodeInteraction.AssertDoesNotExist()
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
            +CodexNodeInteraction.AssertTextEquals(text?.toString() ?: "--")
        }
    }

    fun checkHandicapDoesNotExist() {
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.HANDICAP_TEXT)
            +CodexNodeInteraction.AssertDoesNotExist()
        }
        perform {
            useUnmergedTree = true
            +CodexNodeMatcher.HasTestTag(StatsTestTag.HANDICAP_TABLES)
            +CodexNodeInteraction.AssertDoesNotExist()
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

    fun clickPastRecordsBestTab() {
        clickPastRecordsTab("Best")
    }

    fun clickPastRecordsRecentTab() {
        clickPastRecordsTab("Recent")
    }

    private fun clickPastRecordsTab(tab: String) {
        perform {
            +CodexNodeMatcher.HasAnyAncestor(CodexNodeMatcher.HasTestTag(StatsTestTag.PAST_RECORDS_DIALOG_TAB))
            +CodexNodeMatcher.HasTestTag(TabSwitcherTestTag.ITEM)
            +CodexNodeMatcher.HasText(tab)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun checkPastRecordsDialogItems(items: List<PastRecordsDialogItem>) {
        perform {
            useUnmergedTree = true
            allNodes(CodexNodeMatcher.HasTestTag(StatsTestTag.PAST_RECORDS_DIALOG_ITEM))
            +CodexNodeGroupInteraction.ForEach(
                    items.map { listOf(CodexNodeInteraction.AssertContentDescriptionEquals(it.semanticText)) }
            )
        }
    }

    fun checkClassificationCategory(value: String) {
        checkElementText(StatsTestTag.CLASSIFICATION_CATEGORY, value, useUnmergedTree = true)
    }

    fun checkClassification(
            classification: Classification?,
            isOfficial: Boolean,
            isPredicted: Boolean,
    ) {
        performV2 {
            singleNode {
                useUnmergedTree()
                +CodexNodeMatcher.HasTestTag(StatsTestTag.CLASSIFICATION_TITLE)
                +CodexNodeInteraction.AssertTextEquals(
                        if (isOfficial) "Classification"
                        else "Classification (unofficial)"
                )
            }
        }

        val expectedValue = classification?.classificationString() ?: "No classification"
        checkElementText(StatsTestTag.CLASSIFICATION, expectedValue, useUnmergedTree = true)
    }

    fun checkClassificationDoesNotExist() {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(StatsTestTag.CLASSIFICATION)
            +CodexNodeInteraction.AssertDoesNotExist()
        }
        performV2Single {
            +CodexNodeMatcher.HasTestTag(StatsTestTag.CLASSIFICATION_CATEGORY)
            +CodexNodeInteraction.AssertDoesNotExist()
        }
        performV2Single {
            +CodexNodeMatcher.HasTestTag(StatsTestTag.CLASSIFICATION_TABLES)
            +CodexNodeInteraction.AssertDoesNotExist()
        }
    }

    fun openHandicapTablesInFull(block: HandicapTablesRobot.() -> Unit) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(StatsTestTag.HANDICAP_TABLES)
            +CodexNodeInteraction.PerformScrollTo()
            +CodexNodeInteraction.PerformClick()
        }
        createRobot(HandicapTablesRobot::class, block)
    }

    fun openClassificationTablesInFull(block: ClassificationTablesRobot.() -> Unit) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(StatsTestTag.CLASSIFICATION_TABLES)
            +CodexNodeInteraction.PerformScrollTo()
            +CodexNodeInteraction.PerformClick()
        }
        createRobot(ClassificationTablesRobot::class, block)
    }

    fun openEditArcherInfo(block: ArcherInfoRobot.() -> Unit) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(StatsTestTag.CLASSIFICATION_CATEGORY)
            +CodexNodeInteraction.PerformScrollTo()
            +CodexNodeInteraction.PerformClick()
        }
        createRobot(ArcherInfoRobot::class, block)
    }

    fun openEditArcherHandicaps(block: ArcherHandicapRobot.() -> Unit) {
        performV2Single {
            +CodexNodeMatcher.HasTestTag(StatsTestTag.ARCHER_HANDICAP_TEXT)
            +CodexNodeInteraction.PerformScrollTo()
            +CodexNodeInteraction.PerformClick()
        }
        createRobot(ArcherHandicapRobot::class, block)
    }

    private fun Classification.classificationString() = when (this) {
        ARCHER_3RD_CLASS -> "Archer 3rd"
        ARCHER_2ND_CLASS -> "Archer 2nd"
        ARCHER_1ST_CLASS -> "Archer 1st"
        BOWMAN_3RD_CLASS -> "Bowman 3rd"
        BOWMAN_2ND_CLASS -> "Bowman 2nd"
        BOWMAN_1ST_CLASS -> "Bowman 1st"
        MASTER_BOWMAN -> "Master Bowman"
        GRAND_MASTER_BOWMAN -> "Grand MB"
        ELITE_MASTER_BOWMAN -> "Elite GMB"
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
            get() = data.third?.let { ", $it" } ?: ""

        val semanticText
            get() = "$date, $score$semanticTextExtra"
    }
}
