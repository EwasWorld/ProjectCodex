package eywa.projectcodex.testUtils

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.database.Filters
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archer.ArcherRepo
import eywa.projectcodex.database.archer.DEFAULT_ARCHER_ID
import eywa.projectcodex.database.archer.DatabaseArcher
import eywa.projectcodex.database.archer.DatabaseArcherHandicap
import eywa.projectcodex.database.arrows.ArrowCounterRepo
import eywa.projectcodex.database.arrows.ArrowScoreDao
import eywa.projectcodex.database.arrows.ArrowScoresRepo
import eywa.projectcodex.database.bow.BowDao
import eywa.projectcodex.database.bow.BowRepo
import eywa.projectcodex.database.bow.DEFAULT_BOW_ID
import eywa.projectcodex.database.bow.DatabaseBow
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.RoundArrowCountDao
import eywa.projectcodex.database.rounds.RoundDao
import eywa.projectcodex.database.rounds.RoundDistanceDao
import eywa.projectcodex.database.rounds.RoundRepo
import eywa.projectcodex.database.rounds.RoundSubTypeDao
import eywa.projectcodex.database.shootData.DatabaseFullShootInfo
import eywa.projectcodex.database.shootData.DatabaseShootShortRecord
import eywa.projectcodex.database.shootData.ShootDao
import eywa.projectcodex.database.shootData.ShootDetailDao
import eywa.projectcodex.database.shootData.ShootFilter
import eywa.projectcodex.database.shootData.ShootRoundDao
import eywa.projectcodex.database.shootData.ShootsRepo
import eywa.projectcodex.database.sightMarks.SightMarkDao
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
    val shootDao = MockShoot()
    val arrowScoresRepo: ArrowScoresRepo = mock {}
    val arrowScoreDao: ArrowScoreDao = mock {}
    val roundArrowCountDao: RoundArrowCountDao = mock {}
    val roundSubTypeDao: RoundSubTypeDao = mock {}
    val roundDistanceDao: RoundDistanceDao = mock {}
    val shootRoundDao: ShootRoundDao = mock {}
    val shootDetailDao: ShootDetailDao = mock {}
    val sightMarksDao = MockSightMarksDao()

    val rounds = MockRounds()
    val archerRepo = MockArcherRepo()
    val bow = MockBow()
    val arrowCounterRepo = mock<ArrowCounterRepo>()

    val mock: ScoresRoomDatabase = mock {
        on { shootDao() } doReturn shootDao.mock
        on { arrowScoreDao() } doReturn arrowScoreDao
        on { roundDao() } doReturn rounds.mock
        on { roundArrowCountDao() } doReturn roundArrowCountDao
        on { roundSubTypeDao() } doReturn roundSubTypeDao
        on { roundDistanceDao() } doReturn roundDistanceDao
        on { shootRoundDao() } doReturn shootRoundDao
        on { shootDetailDao() } doReturn shootDetailDao
        on { shootsRepo() } doReturn shootDao.mockRepo
        on { sightMarkDao() } doReturn sightMarksDao.mock
        on { bowDao() } doReturn bow.mock
        on { bowRepo() } doReturn bow.mockRepo
        on { archerRepo() } doReturn archerRepo.mock
        on { roundsRepo() } doReturn rounds.mockRepo
        on { arrowScoresRepo() } doReturn arrowScoresRepo
    }

    class MockShoot {
        var fullShoots: List<DatabaseFullShootInfo> = listOf()
        var secondFullShoots: List<DatabaseFullShootInfo>? = null

        val mock: ShootDao = mock {
            on { getAllFullShootInfo(any()) } doReturn getShoots()
            on { getFullShootInfo(anyInt()) } doReturn getShoots().map { it.firstOrNull() }
            on { getFullShootInfo(anyList()) } doReturn getShoots()
        }

        val mockRepo: ShootsRepo = mock {
            on { getFullShootInfo(anyMatcher<Filters<ShootFilter>>()) } doReturn getShoots()
            on { getFullShootInfo(anyInt()) } doReturn getShoots().map { it.firstOrNull() }
            on { getFullShootInfo(anyList()) } doReturn getShoots()
            on { getMostRecentShootsForRound(any(), any(), any()) } doReturn getShortShoots()
            on { getHighestScoreShootsForRound(any(), any(), any()) } doReturn getShortShoots()
        }

        private fun getShoots() = flow {
            emit(fullShoots)

            secondFullShoots.takeIf { it != null }?.let {
                delay(FLOW_EMIT_DELAY)
                emit(it)
            }
        }

        private fun getShortShoots() = getShoots().map { data ->
            data.map {
                val roundArrowCount = it.roundArrowCounts?.sumOf { a -> a.arrowCount }
                val shotCount = it.arrowCounter?.shotCount ?: it.arrows.orEmpty().count()
                DatabaseShootShortRecord(
                        shootId = it.shoot.shootId,
                        dateShot = it.shoot.dateShot,
                        score = it.arrows.orEmpty().sumOf { a -> a.score },
                        isComplete = roundArrowCount == shotCount,
                )
            }
        }
    }

    class MockRounds {
        var fullRoundsInfo: List<FullRoundInfo> = listOf()
        var secondFullRoundsInfo: List<FullRoundInfo>? = null

        val mock: RoundDao = mock {
            on { getAllRoundsFullInfo() } doReturn getRoundsInfo()
        }

        val mockRepo: RoundRepo = mock {
            on { fullRoundsInfo(any()) } doReturn getRoundsInfo()
            on { fullRoundsInfo } doReturn getRoundsInfo()
            on { wa1440FullRoundInfo } doReturn flow { emit(RoundPreviewHelper.wa1440RoundData) }
        }

        private fun getRoundsInfo() = flow {
            emit(fullRoundsInfo)

            secondFullRoundsInfo.takeIf { it != null }?.let {
                delay(FLOW_EMIT_DELAY)
                emit(it)
            }
        }
    }

    class MockSightMarksDao {
        var sightMarks: List<SightMark> = listOf()

        val mock: SightMarkDao = mock {
            on { getAllSightMarks() } doAnswer { flow { emit(sightMarks.map { it.asDatabaseSightMark() }) } }
        }
    }

    class MockBow {
        var isHighestAtTop = false

        val mock: BowDao = mock {
            on { getDefaultBow() } doAnswer {
                flow {
                    emit(
                            DatabaseBow(DEFAULT_BOW_ID, "Default", isSightMarkDiagramHighestAtTop = isHighestAtTop)
                    )
                }
            }
        }

        val mockRepo: BowRepo = mock {
            on { defaultBow } doAnswer {
                flow {
                    emit(
                            DatabaseBow(DEFAULT_BOW_ID, "Default", isSightMarkDiagramHighestAtTop = isHighestAtTop)
                    )
                }
            }
        }
    }

    class MockArcherRepo {
        var handicaps = emptyList<DatabaseArcherHandicap>()
        var defaultArcher = DatabaseArcher(DEFAULT_ARCHER_ID, "Default Archer", true, ClassificationAge.SENIOR)

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
