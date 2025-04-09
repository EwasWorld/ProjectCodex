package eywa.projectcodex.testUtils

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.database.Filters
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archer.ArcherRepo
import eywa.projectcodex.database.archer.DatabaseArcherHandicap
import eywa.projectcodex.database.archer.DatabaseArcherPreviewHelper
import eywa.projectcodex.database.arrows.ArrowScoresRepo
import eywa.projectcodex.database.bow.BowRepo
import eywa.projectcodex.database.bow.DatabaseBowPreviewHelper
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.RoundRepo
import eywa.projectcodex.database.shootData.DatabaseFullShootInfo
import eywa.projectcodex.database.shootData.DatabaseShootShortRecord
import eywa.projectcodex.database.shootData.ShootFilter
import eywa.projectcodex.database.shootData.ShootsRepo
import eywa.projectcodex.database.sightMarks.SightMarkRepo
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.model.SightMark
import eywa.projectcodex.testUtils.TestUtils.Companion.FLOW_EMIT_DELAY
import eywa.projectcodex.testUtils.TestUtils.Companion.anyMatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyList
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class MockScoresRoomDatabase {
    val shootRepo = MockShootRepo()
    val arrowScoresRepo: ArrowScoresRepo = mock {}
    val sightMarksRepo = MockSightMarksRepo()
    val roundsRepo = MockRoundsRepo()
    val archerRepo = MockArcherRepo()
    val bowRepo = MockBowRepo()

    val mock: ScoresRoomDatabase = mock {
        on { shootsRepo() } doReturn shootRepo.mock
        on { bowRepo() } doReturn bowRepo.mock
        on { archerRepo() } doReturn archerRepo.mock
        on { roundsRepo() } doReturn roundsRepo.mock
        on { arrowScoresRepo() } doReturn arrowScoresRepo
        on { sightMarkRepo() } doReturn sightMarksRepo.mock
    }

    class MockShootRepo {
        var fullShoots: List<DatabaseFullShootInfo> = listOf()
        var secondFullShoots: List<DatabaseFullShootInfo>? = null
        var mostRecentForRound: List<FullShootInfo>? = null
        var highestScoreForRound: List<FullShootInfo>? = null

        val mock: ShootsRepo = mock {
            on { getFullShootInfo(anyMatcher<Filters<ShootFilter>>()) } doAnswer { getShoots() }
            on { getFullShootInfo(anyInt()) } doAnswer { getShoots().map { it.firstOrNull() } }
            on { getFullShootInfo(anyList()) } doAnswer { getShoots() }
            on { getMostRecentShootsForRound(any(), any(), any()) } doAnswer { getShortShoots(mostRecentForRound) }
            on { getHighestScoreShootsForRound(any(), any(), any()) } doAnswer { getShortShoots(highestScoreForRound) }
            on { mostRecentRoundShot } doAnswer { getShoots().map { it.firstOrNull()?.shootRound } }
        }

        private fun getShoots() = flow {
            emit(fullShoots)

            secondFullShoots.takeIf { it != null }?.let {
                delay(FLOW_EMIT_DELAY)
                emit(it)
            }
        }

        private fun getShortShoots(data: List<FullShootInfo>?) = flow {
            val shoots = data?.map {
                val roundArrowCount = it.roundArrowCounts?.sumOf { a -> a.arrowCount }
                val shotCount = it.arrowCounter?.shotCount ?: it.arrows.orEmpty().count()
                DatabaseShootShortRecord(
                        shootId = it.shoot.shootId,
                        dateShot = it.shoot.dateShot,
                        score = it.arrows.orEmpty().sumOf { a -> a.score },
                        isComplete = roundArrowCount == shotCount,
                )
            }
            emit(shoots ?: listOf())
        }
    }

    class MockRoundsRepo {
        var fullRoundsInfo: List<FullRoundInfo> = listOf()
        var secondFullRoundsInfo: List<FullRoundInfo>? = null

        val mock: RoundRepo = mock {
            on { fullRoundsInfo(any()) } doAnswer { getRoundsInfo() }
            on { fullRoundsInfo } doAnswer { getRoundsInfo() }
            on { wa1440FullRoundInfo } doAnswer { flow { emit(RoundPreviewHelper.wa1440RoundData) } }
            on { wa18FullRoundInfo } doAnswer { flow { emit(RoundPreviewHelper.wa18RoundData) } }
        }

        private fun getRoundsInfo() = flow {
            emit(fullRoundsInfo)

            secondFullRoundsInfo.takeIf { it != null }?.let {
                delay(FLOW_EMIT_DELAY)
                emit(it)
            }
        }
    }

    class MockSightMarksRepo {
        var sightMarks: List<SightMark> = listOf()

        val mock: SightMarkRepo = mock {
            on { allSightMarks } doAnswer { flow { emit(sightMarks.map { it.asDatabaseSightMark() }) } }
            on { getSightMarkForDistance(any(), any()) } doAnswer {
                flow {
                    emit(sightMarks.first().asDatabaseSightMark())
                }
            }
        }
    }

    class MockBowRepo {
        var defaultBow = DatabaseBowPreviewHelper.default

        val mock: BowRepo = mock {
            on { defaultBow } doAnswer { getDefaultBow() }
        }

        private fun getDefaultBow() = flow { emit(defaultBow) }
    }

    class MockArcherRepo {
        var handicaps = emptyList<DatabaseArcherHandicap>()
        var defaultArcher = DatabaseArcherPreviewHelper.default

        val mock = mock<ArcherRepo> {
            on { latestHandicapsForDefaultArcher } doAnswer {
                handicaps
                        .groupBy { it.handicapType }
                        .mapNotNull { it.value.maxByOrNull { hc -> hc.dateSet } }
                        .let { flow { emit(it) } }
            }
            on { allHandicapsForDefaultArcher } doAnswer { flow { emit(handicaps) } }
            on { getLatestHandicaps(any()) } doAnswer { flow { emit(handicaps) } }
            on { defaultArcher } doAnswer { flow { emit(defaultArcher) } }
        }
    }
}
