package eywa.projectcodex.testUtils

import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.arrows.ArrowScoreDao
import eywa.projectcodex.database.rounds.*
import eywa.projectcodex.database.shootData.*
import eywa.projectcodex.testUtils.TestUtils.Companion.FLOW_EMIT_DELAY
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyList
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class MockScoresRoomDatabase {
    val archerRoundDao = MockArcherRoundDao()
    val arrowScoreDao: ArrowScoreDao = mock {}
    val roundDao = MockRoundDao()
    val roundArrowCountDao: RoundArrowCountDao = mock {}
    val roundSubTypeDao: RoundSubTypeDao = mock {}
    val roundDistanceDao: RoundDistanceDao = mock {}
    val shootRoundDao: ShootRoundDao = mock {}
    val shootDetailDao: ShootDetailDao = mock {}

    val mock: ScoresRoomDatabase = mock {
        on { shootDao() } doReturn archerRoundDao.mock
        on { arrowScoreDao() } doReturn arrowScoreDao
        on { roundDao() } doReturn roundDao.mock
        on { roundArrowCountDao() } doReturn roundArrowCountDao
        on { roundSubTypeDao() } doReturn roundSubTypeDao
        on { roundDistanceDao() } doReturn roundDistanceDao
        on { shootRoundDao() } doReturn shootRoundDao
        on { shootDetailDao() } doReturn shootDetailDao
        on { shootsRepo() } doReturn ShootsRepo(archerRoundDao.mock, shootDetailDao, shootRoundDao)
    }

    class MockArcherRoundDao {
        var fullArcherRounds: List<DatabaseFullShootInfo> = listOf()
        var secondFullArcherRounds: List<DatabaseFullShootInfo>? = null

        val mock: ShootDao = mock {
            on {
                getAllFullShootInfo(any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            } doReturn getArcherRounds()
            on { getFullShootInfo(anyInt()) } doReturn getArcherRounds().map { it.firstOrNull() }
            on { getFullShootInfo(anyList()) } doReturn getArcherRounds()
        }

        private fun getArcherRounds() = flow {
            emit(fullArcherRounds)

            secondFullArcherRounds.takeIf { !it.isNullOrEmpty() }?.let {
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
}
