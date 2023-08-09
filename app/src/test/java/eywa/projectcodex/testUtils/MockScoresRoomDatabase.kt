package eywa.projectcodex.testUtils

import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.bow.BowDao
import eywa.projectcodex.database.bow.DEFAULT_BOW_ID
import eywa.projectcodex.database.bow.DatabaseBow
import eywa.projectcodex.database.arrows.ArrowScoreDao
import eywa.projectcodex.database.rounds.*
import eywa.projectcodex.database.sightMarks.SightMarkDao
import eywa.projectcodex.model.SightMark
import eywa.projectcodex.database.shootData.*
import eywa.projectcodex.testUtils.TestUtils.Companion.FLOW_EMIT_DELAY
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyList
import org.mockito.kotlin.*

class MockScoresRoomDatabase {
    val shootDao = MockShootDao()
    val arrowScoreDao: ArrowScoreDao = mock {}
    val roundDao = MockRoundDao()
    val roundArrowCountDao: RoundArrowCountDao = mock {}
    val roundSubTypeDao: RoundSubTypeDao = mock {}
    val roundDistanceDao: RoundDistanceDao = mock {}
    val shootRoundDao: ShootRoundDao = mock {}
    val shootDetailDao: ShootDetailDao = mock {}
    val sightMarksDao = MockSightMarksDao()
    val bowDao = MockBowDao()

    val mock: ScoresRoomDatabase = mock {
        on { shootDao() } doReturn shootDao.mock
        on { arrowScoreDao() } doReturn arrowScoreDao
        on { roundDao() } doReturn roundDao.mock
        on { roundArrowCountDao() } doReturn roundArrowCountDao
        on { roundSubTypeDao() } doReturn roundSubTypeDao
        on { roundDistanceDao() } doReturn roundDistanceDao
        on { shootRoundDao() } doReturn shootRoundDao
        on { shootDetailDao() } doReturn shootDetailDao
        on { shootsRepo() } doReturn ShootsRepo(shootDao.mock, shootDetailDao, shootRoundDao)
        on { sightMarkDao() } doReturn sightMarksDao.mock
        on { bowDao() } doReturn bowDao.mock
    }

    class MockShootDao {
        var fullShoots: List<DatabaseFullShootInfo> = listOf()
        var secondFullShoots: List<DatabaseFullShootInfo>? = null

        val mock: ShootDao = mock {
            on {
                getAllFullShootInfo(any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            } doReturn getShoots()
            on { getFullShootInfo(anyInt()) } doReturn getShoots().map { it.firstOrNull() }
            on { getFullShootInfo(anyList()) } doReturn getShoots()
        }

        private fun getShoots() = flow {
            emit(fullShoots)

            secondFullShoots.takeIf { !it.isNullOrEmpty() }?.let {
                delay(FLOW_EMIT_DELAY)
                emit(it)
            }
        }
    }

    class MockRoundDao {
        var fullRoundsInfo: List<FullRoundInfo> = listOf()
        var secondFullRoundsInfo: List<FullRoundInfo>? = null

        val mock: RoundDao = mock {
            on { getAllRoundsFullInfo() } doReturn getRoundsInfo()
        }

        private fun getRoundsInfo() = flow {
            emit(fullRoundsInfo)

            secondFullRoundsInfo.takeIf { !it.isNullOrEmpty() }?.let {
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

    class MockBowDao {
        var isHighestAtTop = false

        val mock: BowDao = mock {
            on { getDefaultBow() } doAnswer { flow { emit(DatabaseBow(DEFAULT_BOW_ID, isHighestAtTop)) } }
        }
    }
}
