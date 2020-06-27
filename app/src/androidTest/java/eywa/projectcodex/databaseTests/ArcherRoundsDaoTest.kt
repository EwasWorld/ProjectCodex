package eywa.projectcodex.databaseTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import eywa.projectcodex.TestData
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.daos.ArcherRoundDao
import eywa.projectcodex.database.daos.RoundDao
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.retrieveValue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArcherRoundsDaoTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: ScoresRoomDatabase
    private lateinit var archerRoundDao: ArcherRoundDao
    private lateinit var roundDao: RoundDao

    @Before
    fun createDb() {
        db = DatabaseSuite.createDatabase()
        archerRoundDao = db.archerRoundDao()
        roundDao = db.roundDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    /**
     * Check max ID value increases with each insertion // TODO Separate max id checks
     * Check inserted values are the same as retrieved
     */
    @Test
    fun basicTest() {
        val retrievedArcherRounds = archerRoundDao.getAllArcherRounds()
        val retrievedMax = archerRoundDao.getMaxId()

        /*
         * Add and retrieve
         */
        val archerRounds = TestData.generateArcherRounds(6, 2)
        var currentMax = -1
        for (archerRound in archerRounds) {
            runBlocking {
                archerRoundDao.insert(
                        // Remove archerRoundId
                        ArcherRound(
                                0,
                                archerRound.dateShot,
                                archerRound.archerId,
                                archerRound.countsTowardsHandicap
                        )
                )
            }

            val unpackedMax = retrievedMax.retrieveValue()!!
            assertEquals(
                    retrievedArcherRounds.retrieveValue()!!.maxBy { it.archerRoundId }?.archerRoundId ?: 0,
                    unpackedMax
            )
            assert(currentMax < unpackedMax)
            currentMax = unpackedMax
        }

        assertEquals(archerRounds.toSet(), retrievedArcherRounds.retrieveValue()!!.toSet())

        /*
         * Delete
         */
        runBlocking {
            archerRoundDao.deleteRound(1)
            archerRoundDao.deleteRound(2)
        }
        assertEquals(
                archerRounds.subList(2, archerRounds.size).toSet(),
                retrievedArcherRounds.retrieveValue()!!.toSet()
        )
    }

    /**
     * Check the correct round info is retrieved for the given archer round
     */
    @Test
    fun getRoundInfoTest() {
        /*
         * Create data and populate tables
         */
        val archerRounds = TestData.generateArcherRounds(6, 2, listOf(1, 2, null))
        val rounds = TestData.generateRounds(3)

        for (archerRound in archerRounds) {
            runBlocking {
                archerRoundDao.insert(archerRound)
            }
        }
        for (round in rounds) {
            runBlocking {
                roundDao.insert(round)
            }
        }

        /*
         * Check the correct round info is retrieved
         */
        for (archerRound in archerRounds) {
            val retrievedRoundInfo = archerRoundDao.getRoundInfo(archerRound.archerRoundId).retrieveValue()
            if (retrievedRoundInfo == null) {
                if (archerRound.roundId != null) {
                    fail("Could not find round info")
                }
            }
            else {
                assertEquals(archerRound.roundId, retrievedRoundInfo.roundId)
                assert(rounds[archerRound.roundId!! - 1] == retrievedRoundInfo)
            }
        }
    }
}