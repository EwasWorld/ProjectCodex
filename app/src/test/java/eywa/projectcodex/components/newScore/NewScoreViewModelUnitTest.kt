package eywa.projectcodex.components.newScore

import androidx.lifecycle.MutableLiveData
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRoundDao
import eywa.projectcodex.database.arrowValue.ArrowValueDao
import eywa.projectcodex.database.rounds.RoundArrowCountDao
import eywa.projectcodex.database.rounds.RoundDao
import eywa.projectcodex.database.rounds.RoundDistanceDao
import eywa.projectcodex.database.rounds.RoundSubTypeDao
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class NewScoreViewModelUnitTest {
    private lateinit var sut: NewScoreViewModel
    private val dbMock = DatabaseMock()

    fun setup() {
        sut = NewScoreViewModel(dbMock.db)
    }

    @Test
    fun testClassInitialisation() {
//        setup()
        // TODO_CURRENT
    }
}

class DatabaseMock(val dbRoundsData: DbRoundsData? = null) {
    val archerRoundsDao: ArcherRoundDao = mock {}
    val arrowValuesDao: ArrowValueDao = mock {}

    /*
     * Rounds
     */
    val roundsDao: RoundDao = mock {
        if (dbRoundsData != null) {
            on { getAllRounds() } doReturn MutableLiveData(dbRoundsData.rounds)
        }
    }
    val roundArrowCountDao: RoundArrowCountDao = mock {
        if (dbRoundsData != null) {
            on { getAllArrowCounts() } doReturn MutableLiveData(dbRoundsData.arrowCounts)
        }
    }
    val roundSubTypeDao: RoundSubTypeDao = mock {
        if (dbRoundsData != null) {
            on { getAllSubTypes() } doReturn MutableLiveData(dbRoundsData.subTypes)
        }
    }
    val roundDistanceDao: RoundDistanceDao = mock {
        if (dbRoundsData != null) {
            on { getAllDistances() } doReturn MutableLiveData(dbRoundsData.distances)
        }
    }

    /*
     * Main DB
     */
    val db: ScoresRoomDatabase = mock {
        on { archerRoundDao() } doReturn archerRoundsDao
        on { roundDao() } doReturn roundsDao
        on { arrowValueDao() } doReturn arrowValuesDao
        on { roundArrowCountDao() } doReturn roundArrowCountDao
        on { roundSubTypeDao() } doReturn roundSubTypeDao
        on { roundDistanceDao() } doReturn roundDistanceDao
    }
}