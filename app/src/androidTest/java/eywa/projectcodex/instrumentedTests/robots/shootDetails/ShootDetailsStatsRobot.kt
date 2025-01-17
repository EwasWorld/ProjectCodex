package eywa.projectcodex.instrumentedTests.robots.shootDetails

import eywa.projectcodex.common.ComposeTestRule
import eywa.projectcodex.common.sharedUi.TabSwitcherTestTag
import eywa.projectcodex.components.shootDetails.stats.ui.StatsTestTag
import eywa.projectcodex.core.mainActivity.MainActivity
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeGroupInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeInteraction
import eywa.projectcodex.instrumentedTests.dsl.CodexNodeMatcher
import eywa.projectcodex.instrumentedTests.robots.ArcherHandicapRobot
import eywa.projectcodex.instrumentedTests.robots.NewScoreRobot
import eywa.projectcodex.instrumentedTests.robots.selectFace.SelectFaceBaseRobot
import eywa.projectcodex.instrumentedTests.robots.shootDetails.common.HandicapAndClassificationSectionRobot

class ShootDetailsStatsRobot(
        composeTestRule: ComposeTestRule<MainActivity>
) : ShootDetailsRobot(composeTestRule, StatsTestTag.SCREEN) {
    private val facesRobot = SelectFaceBaseRobot(::perform)
    val handicapAndClassificationRobot = HandicapAndClassificationSectionRobot(composeTestRule, screenTestTag)

    fun checkFaces(expectedFacesString: String) {
        facesRobot.checkFaces(expectedFacesString)
    }

    fun checkDate(text: String) {
        checkElementText(StatsTestTag.DATE_TEXT, text, true)
    }

    fun checkRound(text: String?) {
        checkElementText(StatsTestTag.ROUND_TEXT, text ?: "N/A", true)
    }

    fun clickEditRoundData(block: NewScoreRobot.() -> Unit) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(StatsTestTag.EDIT_SHOOT_INFO)
            +CodexNodeMatcher.HasAnyAncestor(CodexNodeMatcher.HasTestTag(StatsTestTag.SHOOT_DETAIL_SECTION))
            +CodexNodeInteraction.PerformClick()
        }

        createRobot(NewScoreRobot::class, block)
    }

    fun checkHits(hits: Int, totalShot: Int? = null) {
        performSingle {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(StatsTestTag.HITS_TEXT)
            +CodexNodeInteraction.AssertContentDescriptionEquals(
                    "$hits hits" + (" of $totalShot".takeIf { totalShot != null } ?: "")
            )
        }
        performSingle {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(StatsTestTag.HITS_OF_TEXT)
            if (totalShot != null) +CodexNodeInteraction.AssertIsDisplayed()
            else +CodexNodeInteraction.AssertDoesNotExist()
        }
    }

    fun checkScore(text: Int) {
        checkElementText(StatsTestTag.SCORE_TEXT, text.toString(), true)
    }

    fun checkGolds(text: Int) {
        checkElementText(StatsTestTag.GOLDS_TEXT, text.toString(), true)
    }

    fun checkRemainingArrows(text: Int?) {
        checkElementTextOrDoesNotExist(StatsTestTag.REMAINING_ARROWS_TEXT, text?.toString(), true)
    }

    fun checkPredictedScore(text: Int?) {
        checkElementTextOrDoesNotExist(StatsTestTag.PREDICTED_SCORE_TEXT, text?.toString(), true)
    }

    fun checkPb(isPb: Boolean = true, isTiedPb: Boolean = false) {
        val text = when {
            !isPb -> null
            isTiedPb -> "Tied personal best"
            else -> "Personal best!"
        }
        checkElementTextOrDoesNotExist(StatsTestTag.PB_TEXT, text, true)
    }

    fun checkAllowance(text: Int?) {
        checkElementTextOrDoesNotExist(StatsTestTag.ALLOWANCE_TEXT, text?.toString(), true)
    }

    fun checkArcherHandicap(text: Int?) {
        checkElementTextOrDoesNotExist(StatsTestTag.ARCHER_HANDICAP_TEXT, text?.toString(), true)
    }

    fun checkArcherHandicapDoesNotExist() {
        checkElementDoesNotExist(StatsTestTag.ARCHER_HANDICAP_TEXT, true)
    }

    fun checkAdjustedScore(text: Int?) {
        checkElementTextOrDoesNotExist(StatsTestTag.ADJUSTED_SCORE_TEXT, text?.toString(), true)
    }

    fun checkPastRecordsTextShown(isShown: Boolean = true) {
        checkElementIsDisplayedOrDoesNotExist(StatsTestTag.PAST_RECORDS_LINK_TEXT, isShown)
    }

    fun clickPastRecordsText() {
        clickElement(StatsTestTag.PAST_RECORDS_LINK_TEXT)
    }

    fun clickPastRecordsBestTab() {
        clickPastRecordsTab("Best")
    }

    fun clickPastRecordsRecentTab() {
        clickPastRecordsTab("Recent")
    }

    private fun clickPastRecordsTab(tab: String) {
        performSingle {
            +CodexNodeMatcher.HasAnyAncestor(CodexNodeMatcher.HasTestTag(StatsTestTag.PAST_RECORDS_DIALOG_TAB))
            +CodexNodeMatcher.HasTestTag(TabSwitcherTestTag.ITEM)
            +CodexNodeMatcher.HasText(tab)
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun checkPastRecordsDialogItems(items: List<PastRecordsDialogItem>) {
        performGroup {
            useUnmergedTree()
            +CodexNodeMatcher.HasTestTag(StatsTestTag.PAST_RECORDS_DIALOG_ITEM)
            +CodexNodeGroupInteraction.ForEach(
                    items.map { listOf(CodexNodeInteraction.AssertContentDescriptionEquals(it.semanticText)) }
            )
        }
    }

    fun clickSwitchToSimpleOrAdvanced() {
        performSingle {
            +CodexNodeMatcher.HasTestTag(StatsTestTag.SIMPLE_ADVANCED_SWITCH)
            +CodexNodeInteraction.PerformScrollTo()
            +CodexNodeInteraction.PerformClick()
        }
    }

    fun checkNumbersBreakdownShown(isShown: Boolean = true) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(StatsTestTag.NUMBERS_BREAKDOWN)
            if (isShown) {
                +CodexNodeInteraction.PerformScrollTo()
                +CodexNodeInteraction.AssertIsDisplayed()
            }
            else {
                +CodexNodeInteraction.AssertDoesNotExist()
            }
        }
    }

    fun checkNumbersBreakdown(vararg distancesToHandicaps: Pair<Int?, Float>) {
        performGroup {
            +CodexNodeMatcher.HasTestTag(StatsTestTag.NUMBERS_BREAKDOWN_DISTANCE)
            +CodexNodeGroupInteraction.ForEach(
                    distancesToHandicaps.map {
                        val distance = it.first
                        listOf(CodexNodeInteraction.AssertTextEquals(distance?.toString() ?: "Round"))
                    }
            )
        }
        performGroup {
            +CodexNodeMatcher.HasTestTag(StatsTestTag.NUMBERS_BREAKDOWN_HANDICAP)
            +CodexNodeGroupInteraction.ForEach(
                    distancesToHandicaps.map {
                        listOf(CodexNodeInteraction.AssertTextEquals("%.1f".format(it.second)))
                    }
            )
        }
    }

    fun openEditArcherHandicaps(block: ArcherHandicapRobot.() -> Unit) {
        performSingle {
            +CodexNodeMatcher.HasTestTag(StatsTestTag.ARCHER_HANDICAP_TEXT)
            +CodexNodeInteraction.PerformScrollTo()
            +CodexNodeInteraction.PerformClick()
        }
        createRobot(ArcherHandicapRobot::class, block)
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
