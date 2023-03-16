package eywa.projectcodex.testUtils

import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRoundDao
import eywa.projectcodex.database.archerRound.DatabaseFullArcherRoundInfo
import eywa.projectcodex.database.arrowValue.ArrowValueDao
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class MockScoresRoomDatabase {
    val archerRoundDao = MockArcherRoundDao()
    val arrowValueDao: ArrowValueDao = mock {}

    val mock: ScoresRoomDatabase = mock {
        on { archerRoundDao() } doReturn archerRoundDao.mock
        on { arrowValueDao() } doReturn arrowValueDao
    }

    class MockArcherRoundDao {
        var personalBests: List<Int> = listOf()
        var fullArcherRounds: List<DatabaseFullArcherRoundInfo> = listOf()
        var secondFullArcherRounds: List<DatabaseFullArcherRoundInfo>? = null

        val mock: ArcherRoundDao = mock {
            on { getPersonalBests() } doReturn flow {
                emit(personalBests.map { ArcherRoundDao.ArcherRoundIdWrapper(it) })
            }

            on { getAllFullArcherRoundInfo() } doReturn flow {
                emit(fullArcherRounds)

                if (!secondFullArcherRounds.isNullOrEmpty()) {
                    delay(FLOW_EMIT_DELAY)
                    emit(secondFullArcherRounds!!)
                }
            }
        }
    }

    companion object {
        const val FLOW_EMIT_DELAY = 50L
    }
}
