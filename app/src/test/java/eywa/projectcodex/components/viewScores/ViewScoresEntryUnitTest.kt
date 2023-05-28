package eywa.projectcodex.components.viewScores

import android.content.res.Resources
import eywa.projectcodex.R
import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.addFullSetOfArrows
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.addRound
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.completeRound
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.newArcherRound
import eywa.projectcodex.common.sharedUi.previewHelpers.ArrowValuesPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.components.archerRoundScore.Handicap
import eywa.projectcodex.components.archerRoundScore.roundHandicap
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.testUtils.TestData
import eywa.projectcodex.testUtils.TestUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import java.util.*

class ViewScoresEntryUnitTest {
    private val resources: Resources = setUpResources()

    private val fullRoundInfo = FullRoundInfo(
            round = RoundPreviewHelper.indoorMetricRoundData.round.copy(roundId = 1),
            roundSubTypes = null,
            roundArrowCounts = listOf(
                    RoundArrowCount(1, 1, 122f, 18),
                    RoundArrowCount(1, 2, 122f, 18),
            ),
            roundDistances = listOf(
                    RoundDistance(1, 1, 1, 20),
                    RoundDistance(1, 2, 1, 10),
            ),
    )

    private val customLogger: CustomLogger = mock { }

    @Test
    fun testGolds_Tens() {
        val entry = ViewScoresEntry(
                info = ArcherRoundPreviewHelper.newFullArcherRoundInfo().addRound(fullRoundInfo).addFullSetOfArrows(),
                customLogger = customLogger,
        )
        assertEquals("2", entry.hitsScoreGolds?.split("/")?.last())
    }

    @Test
    fun testGolds_Nines() {
        val entry = ViewScoresEntry(
                info = ArcherRoundPreviewHelper.newFullArcherRoundInfo().addFullSetOfArrows(),
                customLogger = customLogger,
        )
        assertEquals("3", entry.hitsScoreGolds?.split("/")?.last())
    }

    @Test
    fun testScore() {
        val entry = ViewScoresEntry(
                info = ArcherRoundPreviewHelper.newFullArcherRoundInfo().addFullSetOfArrows(),
                customLogger = customLogger,
        )
        assertEquals(
                ArrowValuesPreviewHelper.ARROWS.sumOf { it.score }.toString(),
                entry.hitsScoreGolds?.split("/")?.get(1)
        )
    }

    @Test
    fun testHits() {
        val entry = ViewScoresEntry(
                info = ArcherRoundPreviewHelper.newFullArcherRoundInfo().addFullSetOfArrows(),
                customLogger = customLogger,
        )
        assertEquals(
                (ArrowValuesPreviewHelper.ARROWS.count() - 1).toString(),
                entry.hitsScoreGolds?.split("/")?.get(0)
        )
    }

    @Test
    fun testHandicap() {
        val entry = ViewScoresEntry(
                info = ArcherRoundPreviewHelper.newFullArcherRoundInfo().addRound(fullRoundInfo).addFullSetOfArrows(),
                customLogger = customLogger,
        )

        assertEquals(
                Handicap.getHandicapForRound(
                        fullRoundInfo.round,
                        fullRoundInfo.roundArrowCounts!!,
                        fullRoundInfo.roundDistances!!,
                        TestData.ARROWS.sumOf { it.score },
                        false,
                        TestData.ARROWS.size,
                        use2023Handicaps = true,
                ).roundHandicap(),
                entry.handicap
        )
    }

    @Test
    fun testRoundComplete() {
        val entry = ViewScoresEntry(
                info = ArcherRoundPreviewHelper.newFullArcherRoundInfo()
                        .addRound(fullRoundInfo)
                        .completeRound(10, false),
                customLogger = customLogger,
        )

        assertEquals(true, entry.isRoundComplete())
    }

    @Test
    fun testRoundIncomplete() {
        val entry = ViewScoresEntry(
                info = ArcherRoundPreviewHelper.newFullArcherRoundInfo().addRound(fullRoundInfo).addFullSetOfArrows(),
                customLogger = customLogger,
        )

        assertEquals(false, entry.isRoundComplete())
    }

    @Test
    fun testRoundComplete_NoRound() {
        val entry = ViewScoresEntry(
                info = ArcherRoundPreviewHelper.newFullArcherRoundInfo().addFullSetOfArrows(),
                customLogger = customLogger,
        )

        assertEquals(false, entry.isRoundComplete())
    }

    @Test
    fun testScoreSummary_HasRoundWithArrows() {
        val entry = ViewScoresEntry(
                info = ArcherRoundPreviewHelper
                        .newFullArcherRoundInfo(archerRound = newArcherRound(1, Date(2022, 5, 20)))
                        .addRound(fullRoundInfo)
                        .addFullSetOfArrows(),
                customLogger = customLogger,
        )

        assertEquals(
                "WA - 20/06/22\nHits: 11, Score: 65, Golds (ten_long): 2",
                entry.getScoreSummary(resources)
        )
    }

    @Test
    fun testScoreSummary_NoRoundWithArrows() {
        val entry = ViewScoresEntry(
                info = ArcherRoundPreviewHelper
                        .newFullArcherRoundInfo(archerRound = newArcherRound(1, Date(2022, 5, 20)))
                        .addFullSetOfArrows(),
                customLogger = customLogger,
        )

        assertEquals(
                "No Round - 20/06/22\nHits: 11, Score: 65, Golds (nine_long): 3",
                entry.getScoreSummary(resources)
        )
    }

    @Test
    fun testScoreSummary_NoRoundNoArrows() {
        val entry = ViewScoresEntry(
                info = ArcherRoundPreviewHelper
                        .newFullArcherRoundInfo(archerRound = newArcherRound(1, Date(2022, 5, 20))),
                customLogger = customLogger,
        )

        assertEquals(
                "No Round - 20/06/22\nNo arrows entered",
                entry.getScoreSummary(resources)
        )
    }

    private fun setUpResources() = TestUtils.createResourceMock(
            mapOf(
                    Pair(R.string.end_to_string_arrow_placeholder, TestData.ARROW_PLACEHOLDER),
                    Pair(R.string.end_to_string_arrow_deliminator, TestData.ARROW_DELIMINATOR),
                    Pair(R.string.score_pad__grand_total, "Grand Total"),
                    Pair(R.string.score_pad__running_total_placeholder, "-"),
                    Pair(R.string.score_pad__distance_total, "Total at %1\$d%2\$s"),
                    Pair(
                            R.string.email_round_summary,
                            "%1\$s - %2\$s\nHits: %3\$d, Score: %4\$d, Golds (%5\$s): %6\$d",
                    ),
                    Pair(R.string.email_round_summary_no_arrows, "%1\$s - %2\$s\nNo arrows entered"),
                    Pair(R.string.table_golds_nines_full, "nine_long"),
                    Pair(R.string.table_golds_tens_full, "ten_long"),
                    Pair(R.string.table_golds_xs_full, "x_long"),
                    Pair(R.string.create_round__no_round, "No Round"),
                    Pair(R.string.score_pad__surplus_total, "Surplus Total"),
                    Pair(R.string.score_pad__end_string_header, "End"),
                    Pair(R.string.table_hits_header, "H"),
                    Pair(R.string.table_score_header, "S"),
                    Pair(R.string.table_golds_tens_header, "9"),
                    Pair(R.string.score_pad__running_total_header, "RT"),
                    Pair(R.string.arrow_value_m, "m"),
                    Pair(R.string.arrow_value_1, "1"),
                    Pair(R.string.arrow_value_2, "2"),
                    Pair(R.string.arrow_value_3, "3"),
                    Pair(R.string.arrow_value_4, "4"),
                    Pair(R.string.arrow_value_5, "5"),
                    Pair(R.string.arrow_value_6, "6"),
                    Pair(R.string.arrow_value_7, "7"),
                    Pair(R.string.arrow_value_8, "8"),
                    Pair(R.string.arrow_value_9, "9"),
                    Pair(R.string.arrow_value_10, "10"),
                    Pair(R.string.arrow_value_x, "X"),
                    Pair(R.string.units_meters_short, "m"),
                    Pair(R.string.units_yards_short, "yd"),
            )
    )
}
