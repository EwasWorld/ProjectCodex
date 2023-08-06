package eywa.projectcodex.testUtils

import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRoundDao
import eywa.projectcodex.database.archerRound.DatabaseFullArcherRoundInfo
import eywa.projectcodex.database.arrowValue.ArrowValueDao
import eywa.projectcodex.database.bow.BowDao
import eywa.projectcodex.database.bow.DEFAULT_BOW_ID
import eywa.projectcodex.database.bow.DatabaseBow
import eywa.projectcodex.database.rounds.*
import eywa.projectcodex.database.sightMarks.SightMarkDao
import eywa.projectcodex.model.SightMark
import eywa.projectcodex.testUtils.TestUtils.Companion.FLOW_EMIT_DELAY
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.kotlin.*

class MockScoresRoomDatabase {
    val archerRoundDao = MockArcherRoundDao()
    val arrowValueDao: ArrowValueDao = mock {}
    val roundDao = MockRoundDao()
    val roundArrowCountDao: RoundArrowCountDao = mock {}
    val roundSubTypeDao: RoundSubTypeDao = mock {}
    val roundDistanceDao: RoundDistanceDao = mock {}
    val sightMarksDao = MockSightMarksDao()
    val bowDao = MockBowDao()

    val mock: ScoresRoomDatabase = mock {
        on { archerRoundDao() } doReturn archerRoundDao.mock
        on { arrowValueDao() } doReturn arrowValueDao
        on { roundDao() } doReturn roundDao.mock
        on { roundArrowCountDao() } doReturn roundArrowCountDao
        on { roundSubTypeDao() } doReturn roundSubTypeDao
        on { roundDistanceDao() } doReturn roundDistanceDao
        on { sightMarkDao() } doReturn sightMarksDao.mock
        on { bowDao() } doReturn bowDao.mock
    }

    class MockArcherRoundDao {
        var fullArcherRounds: List<DatabaseFullArcherRoundInfo> = listOf()
        var secondFullArcherRounds: List<DatabaseFullArcherRoundInfo>? = null

        val mock: ArcherRoundDao = mock {
            on {
                getAllFullArcherRoundInfo(any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            } doReturn getArcherRounds()
            on { getFullArcherRoundInfo(anyInt()) } doReturn getArcherRounds().map { it.firstOrNull() }
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
