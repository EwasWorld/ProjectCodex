package eywa.projectcodex.model

import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import org.junit.Assert
import org.junit.Test
import java.util.Calendar

private val NO_ROUND = ResOrActual.StringResource(R.string.create_round__no_round)

class FullShootInfoUnitTest {

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
                ResOrActual.JoinToStringResource(
                        listOf(
                                ResOrActual.StringResource(
                                        R.string.email_round_summary,
                                        listOf("WA 70", "20/06/22", /* hits */ 11, /* score */ 65),
                                ),
                                ResOrActual.StringResource(
                                        R.string.email_round_summary_golds,
                                        listOf(ResOrActual.StringResource(R.string.table_golds_tens_full), 2),
                                ),
                                ResOrActual.StringResource(
                                        R.string.email_round_summary_golds,
                                        listOf(ResOrActual.StringResource(R.string.table_golds_xs_full), 1),
                                ),
                        ),
                        ResOrActual.Blank,
                ),
                entry.getScoreSummary()
        )
    }

    @Test
    fun testScoreSummary_NoRoundWithArrows() {
        val entry = ShootPreviewHelperDsl.create {
            shoot = shoot.copy(dateShot = date)
            addFullSetOfArrows()
        }

        Assert.assertEquals(
                ResOrActual.JoinToStringResource(
                        listOf(
                                ResOrActual.StringResource(
                                        R.string.email_round_summary,
                                        listOf(NO_ROUND, "20/06/22", /* hits */ 11, /* score */ 65),
                                ),
                                ResOrActual.StringResource(
                                        R.string.email_round_summary_golds,
                                        listOf(ResOrActual.StringResource(R.string.table_golds_tens_full), 2),
                                ),
                        ),
                        ResOrActual.Blank,
                ),
                entry.getScoreSummary()
        )
    }

    @Test
    fun testScoreSummary_NoRoundNoArrows() {
        val entry = ShootPreviewHelperDsl.create {
            shoot = shoot.copy(dateShot = date)
        }

        Assert.assertEquals(
                ResOrActual.StringResource(
                        R.string.email_round_summary_no_arrows,
                        listOf(NO_ROUND, "20/06/22"),
                ),
                entry.getScoreSummary()
        )
    }

    @Test
    fun testScoreSummary_CounterNoRoundNoArrows() {
        val entry = ShootPreviewHelperDsl.create {
            shoot = shoot.copy(dateShot = date)
            addArrowCounter(0)
        }

        Assert.assertEquals(
                ResOrActual.StringResource(
                        R.string.email_round_summary_count,
                        listOf(NO_ROUND, "20/06/22", "0"),
                ),
                entry.getScoreSummary()
        )
    }

    @Test
    fun testScoreSummary_CounterNoRoundWithArrows() {
        val entry = ShootPreviewHelperDsl.create {
            shoot = shoot.copy(dateShot = date)
            addArrowCounter(6)
        }

        Assert.assertEquals(
                ResOrActual.StringResource(
                        R.string.email_round_summary_count,
                        listOf(NO_ROUND, "20/06/22", "6"),
                ),
                entry.getScoreSummary()
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
                ResOrActual.StringResource(
                        R.string.email_round_summary_count,
                        listOf("WA", "20/06/22", "6"),
                ),
                entry.getScoreSummary()
        )
    }
}
