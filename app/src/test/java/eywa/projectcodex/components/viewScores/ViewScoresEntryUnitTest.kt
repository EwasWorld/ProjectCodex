package eywa.projectcodex.components.viewScores

import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.common.sharedUi.previewHelpers.ArrowScoresPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addFullSetOfArrows
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addRound
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.completeRound
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.model.GoldsType
import eywa.projectcodex.model.Handicap
import eywa.projectcodex.model.roundHandicap
import eywa.projectcodex.testUtils.TestData
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock

class ViewScoresEntryUnitTest {
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

    private val customLogger: CustomLogger = mock { }

    @Test
    fun testGolds_Tens() {
        val entry = ViewScoresEntry(
                info = ShootPreviewHelper.newFullShootInfo().addRound(fullRoundInfo).addFullSetOfArrows(),
                customLogger = customLogger,
        )
        assertEquals("2", entry.hitsScoreGolds?.split("/")?.last())
    }

    @Test
    fun testGolds_Nines() {
        val entry = ViewScoresEntry(
                info = ShootPreviewHelper.newFullShootInfo()
                        .addRound(RoundPreviewHelper.yorkRoundData).addFullSetOfArrows(),
                customLogger = customLogger,
        )
        assertEquals("3", entry.hitsScoreGolds?.split("/")?.last())
    }

    @Test
    fun testGolds_Custom() {
        val entry = ViewScoresEntry(
                info = ShootPreviewHelper.newFullShootInfo().addFullSetOfArrows(),
                customLogger = customLogger,
        )
        assertEquals(1, entry.golds(GoldsType.XS))
    }

    @Test
    fun testScore() {
        val entry = ViewScoresEntry(
                info = ShootPreviewHelper.newFullShootInfo().addFullSetOfArrows(),
                customLogger = customLogger,
        )
        assertEquals(
                ArrowScoresPreviewHelper.ARROWS.sumOf { it.score }.toString(),
                entry.hitsScoreGolds?.split("/")?.get(1)
        )
    }

    @Test
    fun testHits() {
        val entry = ViewScoresEntry(
                info = ShootPreviewHelper.newFullShootInfo().addFullSetOfArrows(),
                customLogger = customLogger,
        )
        assertEquals(
                (ArrowScoresPreviewHelper.ARROWS.count() - 1).toString(),
                entry.hitsScoreGolds?.split("/")?.get(0)
        )
    }

    @Test
    fun testHandicap() {
        val entry = ViewScoresEntry(
                info = ShootPreviewHelper.newFullShootInfo().addRound(fullRoundInfo).addFullSetOfArrows(),
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
                )!!.roundHandicap(),
                entry.handicap
        )
    }

    @Test
    fun testRoundComplete() {
        val entry = ViewScoresEntry(
                info = ShootPreviewHelper.newFullShootInfo()
                        .addRound(fullRoundInfo)
                        .completeRound(10, false),
                customLogger = customLogger,
        )

        assertEquals(true, entry.isRoundComplete())
    }

    @Test
    fun testRoundIncomplete() {
        val entry = ViewScoresEntry(
                info = ShootPreviewHelper.newFullShootInfo().addRound(fullRoundInfo).addFullSetOfArrows(),
                customLogger = customLogger,
        )

        assertEquals(false, entry.isRoundComplete())
    }

    @Test
    fun testRoundComplete_NoRound() {
        val entry = ViewScoresEntry(
                info = ShootPreviewHelper.newFullShootInfo().addFullSetOfArrows(),
                customLogger = customLogger,
        )

        assertEquals(false, entry.isRoundComplete())
    }
}
