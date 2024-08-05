package eywa.projectcodex.database

import android.content.res.Resources
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.testUtils.TestData
import eywa.projectcodex.testUtils.TestUtils
import org.junit.Assert
import org.junit.Test
import java.util.Calendar

class FullShootInfoUnitTest {
    private val resources: Resources = setUpResources()

    private val fullRoundInfo = FullRoundInfo(
            round = RoundPreviewHelper.indoorMetricRoundData.round.copy(roundId = 1),
            roundSubTypes = null,
            roundArrowCounts = listOf(
                    RoundArrowCount(1, 1, 122.0, 18),
                    RoundArrowCount(1, 2, 122.0, 18),
            ),
            roundDistances = listOf(
                    RoundDistance(1, 1, 1, 20),
                    RoundDistance(1, 2, 1, 10),
            ),
    )

    private val date = Calendar.Builder().setDate(2022, 5, 20).build()

    // TODO_CURRENT

    @Test
    fun testScoreSummary_HasRoundWithArrows() {
        val entry = ShootPreviewHelperDsl.create {
            shoot = shoot.copy(dateShot = date)
            round = RoundPreviewHelper.wa70RoundData
            addFullSetOfArrows()
        }

        Assert.assertEquals(
                "WA 70 - 20/06/22\nHits: 11, Score: 65, ten_long: 2, x_long: 1",
                entry.getScoreSummary(resources)
        )
    }

    @Test
    fun testScoreSummary_NoRoundWithArrows() {
        val entry = ShootPreviewHelperDsl.create {
            shoot = shoot.copy(dateShot = date)
            addFullSetOfArrows()
        }

        Assert.assertEquals(
                "No Round - 20/06/22\nHits: 11, Score: 65, ten_long: 2",
                entry.getScoreSummary(resources)
        )
    }

    @Test
    fun testScoreSummary_NoRoundNoArrows() {
        val entry = ShootPreviewHelperDsl.create {
            shoot = shoot.copy(dateShot = date)
        }

        Assert.assertEquals(
                "No Round - 20/06/22\nNo arrows entered",
                entry.getScoreSummary(resources)
        )
    }

    @Test
    fun testScoreSummary_CounterNoRoundNoArrows() {
        val entry = ShootPreviewHelperDsl.create {
            shoot = shoot.copy(dateShot = date)
            addArrowCounter(0)
        }

        Assert.assertEquals(
                "No Round - 20/06/22\n0 arrows shot",
                entry.getScoreSummary(resources)
        )
    }

    @Test
    fun testScoreSummary_CounterNoRoundWithArrows() {
        val entry = ShootPreviewHelperDsl.create {
            shoot = shoot.copy(dateShot = date)
            addArrowCounter(6)
        }

        Assert.assertEquals(
                "No Round - 20/06/22\n6 arrows shot",
                entry.getScoreSummary(resources)
        )
    }

    @Test
    fun testScoreSummary_CounterWithRoundWithArrows() {
        val entry = ShootPreviewHelperDsl.create {
            shoot = shoot.copy(dateShot = date)
            round = fullRoundInfo
            addArrowCounter(6)
        }

        Assert.assertEquals(
                "WA - 20/06/22\n6 arrows shot",
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
                    Pair(R.string.email_round_summary, "%1\$s - %2\$s\nHits: %3\$d, Score: %4\$d"),
                    Pair(R.string.email_round_summary_golds, ", %1\$s: %2\$d"),
                    Pair(R.string.email_round_summary_no_arrows, "%1\$s - %2\$s\nNo arrows entered"),
                    Pair(R.string.email_round_summary_count, "%1\$s - %2\$s\n%3\$s arrows shot"),
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
