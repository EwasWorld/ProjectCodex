package eywa.projectcodex.components.shootDetails

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.components.shootDetails.stats.DistanceExtra
import eywa.projectcodex.components.shootDetails.stats.GrandTotalExtra
import eywa.projectcodex.components.shootDetails.stats.StatsExtras
import eywa.projectcodex.components.shootDetails.stats.StatsState
import eywa.projectcodex.database.archer.DatabaseArcherPreviewHelper
import eywa.projectcodex.database.bow.DatabaseBowPreviewHelper
import eywa.projectcodex.testUtils.RawResourcesHelper
import junit.framework.TestCase.assertEquals
import org.junit.Test

class StatsStateUnitTest {
    private val classificationTablesUseCase = RawResourcesHelper.classificationTables

    @Test
    fun testExtras() {
        val round = RoundPreviewHelper.outdoorImperialRoundData
        val detailsState = ShootDetailsState(
                fullShootInfo = ShootPreviewHelperDsl.create {
                    this.round = round
                    completeRound(arrowScore = 7)
                },
        )
        val state = StatsState(detailsState, StatsExtras(), classificationTablesUseCase)

        assertEquals(
                DistanceExtra(
                        round.roundDistances!![0],
                        round.roundArrowCounts!![0],
                        state.fullShootInfo.arrows!!.take(36),
                        6,
                        calculateHandicap = { _, _, _ -> 29.0 },
                ),
                state.extras!![0] as DistanceExtra,
        )
        assertEquals(
                DistanceExtra(
                        round.roundDistances!![1],
                        round.roundArrowCounts!![1],
                        state.fullShootInfo.arrows!!.take(24),
                        6,
                        calculateHandicap = { _, _, _ -> 37.0 },
                ),
                state.extras!![1] as DistanceExtra,
        )
        assertEquals(
                GrandTotalExtra(
                        state.fullShootInfo.arrows!!,
                        6,
                        handicap = 32.0,
                ),
                state.extras!![2] as GrandTotalExtra,
        )
    }

    @Test
    fun testClassification_York() {
        val detailsState = ShootDetailsState(
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
