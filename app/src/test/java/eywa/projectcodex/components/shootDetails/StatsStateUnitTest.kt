package eywa.projectcodex.components.shootDetails

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.common.utils.standardDeviation
import eywa.projectcodex.components.shootDetails.stats.DistanceBreakdownRow
import eywa.projectcodex.components.shootDetails.stats.GrandTotalBreakdownRow
import eywa.projectcodex.components.shootDetails.stats.NumbersBreakdownRowStats
import eywa.projectcodex.components.shootDetails.stats.StatsExtras
import eywa.projectcodex.components.shootDetails.stats.StatsState
import eywa.projectcodex.database.archer.DatabaseArcherPreviewHelper
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.bow.DatabaseBowPreviewHelper
import eywa.projectcodex.testUtils.RawResourcesHelper
import junit.framework.TestCase.assertEquals
import org.junit.Test

class StatsStateUnitTest {
    private val classificationTablesUseCase = RawResourcesHelper.classificationTables

    @Test
    fun testNumbersBreakdownRows() {
        val round = RoundPreviewHelper.outdoorImperialRoundData
        val detailsState = ShootDetailsState(
                shootId = 1,
                fullShootInfo = ShootPreviewHelperDsl.create {
                    this.round = round
                    completeRound(arrowScore = 7)
                },
        )
        val state = StatsState(detailsState, StatsExtras(), classificationTablesUseCase)

        assertEquals(
                DistanceBreakdownRow(
                        distance = round.roundDistances!![0],
                        roundArrowCount = round.roundArrowCounts!![0],
                        arrows = state.fullShootInfo.arrows!!.take(36),
                        endSize = 6,
                        calculateHandicap = { _, _, _ -> 29.0 },
                ),
                state.numbersBreakdownRowStats!![0] as DistanceBreakdownRow,
        )
        assertEquals(
                DistanceBreakdownRow(
                        distance = round.roundDistances!![1],
                        roundArrowCount = round.roundArrowCounts!![1],
                        arrows = state.fullShootInfo.arrows!!.take(24),
                        endSize = 6,
                        calculateHandicap = { _, _, _ -> 37.0 },
                ),
                state.numbersBreakdownRowStats!![1] as DistanceBreakdownRow,
        )
        assertEquals(
                GrandTotalBreakdownRow(
                        arrows = state.fullShootInfo.arrows!!,
                        endSize = 6,
                        handicap = 32.0,
                ),
                state.numbersBreakdownRowStats!![2] as GrandTotalBreakdownRow,
        )
    }

    @Test
    fun testNumbersBreakdownClasses() {
        val arrows = (0..10).plus(0)
        val stats = NumbersBreakdownRowStats(
                arrows.mapIndexed { index, score ->
                    DatabaseArrowScore(
                            shootId = 1,
                            arrowNumber = index + 1,
                            score = score,
                    )
                },
                endSize = 6,
                handicap = 32.0,
        )
        val ends = arrows.chunked(6).map { it.sum() }

        assertEquals(32.0, stats.handicap)

        assertEquals(12, arrows.size)
        assertEquals(arrows.sum().toFloat().div(12), stats.averageArrow)
        assertEquals(ends.sum().toFloat().div(2), stats.averageEnd)

        assertEquals(arrows.standardDeviation(), stats.arrowStdDev)
        assertEquals(ends.standardDeviation(), stats.endStDev)
    }

    @Test
    fun testClassification_York() {
        val detailsState = ShootDetailsState(
                shootId = 1,
                fullShootInfo = ShootPreviewHelperDsl.create {
                    this.round = RoundPreviewHelper.yorkRoundData
                    roundSubTypeId = 1
                    completeRoundWithFinalScore(400)
                },
                archerInfo = DatabaseArcherPreviewHelper.default,
                bow = DatabaseBowPreviewHelper.default,
                wa1440FullRoundInfo = RoundPreviewHelper.wa1440RoundData,
        )
        val state = StatsState(detailsState, StatsExtras(), RawResourcesHelper.classificationTables)

        assertEquals(
                Classification.ARCHER_2ND_CLASS to true,
                state.classification,
        )
    }

    @Test
    fun testClassification_Hereford() {
        val detailsState = ShootDetailsState(
                shootId = 1,
                fullShootInfo = ShootPreviewHelperDsl.create {
                    this.round = RoundPreviewHelper.yorkRoundData
                    roundSubTypeId = 2
                    completeRoundWithFinalScore(500)
                },
                archerInfo = DatabaseArcherPreviewHelper.default,
                bow = DatabaseBowPreviewHelper.default,
                wa1440FullRoundInfo = RoundPreviewHelper.wa1440RoundData,
        )
        val state = StatsState(detailsState, StatsExtras(), RawResourcesHelper.classificationTables)

        assertEquals(
                Classification.ARCHER_3RD_CLASS to true,
                state.classification,
        )
    }

    @Test
    fun testClassification_NoArrowsShot() {
        val detailsState = ShootDetailsState(
                shootId = 1,
                fullShootInfo = ShootPreviewHelperDsl.create {
                    this.round = RoundPreviewHelper.yorkRoundData
                    roundSubTypeId = 2
                },
                archerInfo = DatabaseArcherPreviewHelper.default,
                bow = DatabaseBowPreviewHelper.default,
                wa1440FullRoundInfo = RoundPreviewHelper.wa1440RoundData,
        )
        val state = StatsState(detailsState, StatsExtras(), RawResourcesHelper.classificationTables)

        assertEquals(
                null,
                state.classification,
        )
    }

    @Test
    fun testClassification_NoClassification_ScoreTooLow() {
        val detailsState = ShootDetailsState(
                shootId = 1,
                fullShootInfo = ShootPreviewHelperDsl.create {
                    this.round = RoundPreviewHelper.yorkRoundData
                    completeRound(arrowScore = 1)
                },
                archerInfo = DatabaseArcherPreviewHelper.default,
                bow = DatabaseBowPreviewHelper.default,
                wa1440FullRoundInfo = RoundPreviewHelper.wa1440RoundData,
        )
        val state = StatsState(detailsState, StatsExtras(), RawResourcesHelper.classificationTables)

        assertEquals(
                null,
                state.classification,
        )
    }

    @Test
    fun testClassification_RoughClassification() {
        val detailsState = ShootDetailsState(
                shootId = 1,
                fullShootInfo = ShootPreviewHelperDsl.create {
                    this.round = RoundPreviewHelper.outdoorImperialRoundData
                    roundSubTypeId = 2
                    completeRoundWithFinalScore(400)
                },
                archerInfo = DatabaseArcherPreviewHelper.default,
                bow = DatabaseBowPreviewHelper.default,
                wa1440FullRoundInfo = RoundPreviewHelper.wa1440RoundData,
        )
        val state = StatsState(detailsState, StatsExtras(), RawResourcesHelper.classificationTables)

        assertEquals(
                Classification.BOWMAN_2ND_CLASS to false,
                state.classification,
        )
    }

    @Test
    fun testClassification_RoughAndTrueMix_UseRough() {
        val detailsState = ShootDetailsState(
                shootId = 1,
                fullShootInfo = ShootPreviewHelperDsl.create {
                    this.round = RoundPreviewHelper.yorkRoundData
                    roundSubTypeId = 2
                    completeRound(10)
                },
                archerInfo = DatabaseArcherPreviewHelper.default,
                bow = DatabaseBowPreviewHelper.default,
                wa1440FullRoundInfo = RoundPreviewHelper.wa1440RoundData,
        )
        val state = StatsState(detailsState, StatsExtras(), RawResourcesHelper.classificationTables)

        assertEquals(
                Classification.ELITE_MASTER_BOWMAN to false,
                state.classification,
        )
    }

    @Test
    fun testClassification_RoughAndTrueMix_UseReal() {
        val detailsState = ShootDetailsState(
                shootId = 1,
                fullShootInfo = ShootPreviewHelperDsl.create {
                    this.round = RoundPreviewHelper.yorkRoundData
                    roundSubTypeId = 2
                    completeRoundWithFinalScore(900)
                },
                archerInfo = DatabaseArcherPreviewHelper.default,
                bow = DatabaseBowPreviewHelper.default,
                wa1440FullRoundInfo = RoundPreviewHelper.wa1440RoundData,
        )
        val state = StatsState(detailsState, StatsExtras(), RawResourcesHelper.classificationTables)

        assertEquals(
                Classification.BOWMAN_3RD_CLASS to true,
                state.classification,
        )
    }
}
