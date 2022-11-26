package eywa.projectcodex.databaseTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.retrieveValue
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.ArcherRoundDao
import eywa.projectcodex.database.rounds.RoundDao
import eywa.projectcodex.database.rounds.RoundSubTypeDao
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArcherRoundsTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: ScoresRoomDatabase
    private lateinit var archerRoundDao: ArcherRoundDao
    private lateinit var roundDao: RoundDao
    private lateinit var roundSubTypeDao: RoundSubTypeDao

    @Before
    fun createDb() {
        db = DatabaseSuite.createDatabase()
        archerRoundDao = db.archerRoundDao()
        roundDao = db.roundDao()
        roundSubTypeDao = db.roundSubTypeDao()
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
        val archerRounds = listOf(
                ArcherRound(1, TestUtils.generateDate(), 1, false),
                ArcherRound(2, TestUtils.generateDate(), 2, false),
                ArcherRound(3, TestUtils.generateDate(), 1, false),
                ArcherRound(4, TestUtils.generateDate(), 1, false),
                ArcherRound(5, TestUtils.generateDate(), 2, false),
                ArcherRound(6, TestUtils.generateDate(), 1, false),
        )
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
                    retrievedArcherRounds.retrieveValue()!!.maxByOrNull { it.archerRoundId }?.archerRoundId ?: 0,
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
        val archerRounds = listOf(
                ArcherRound(1, TestUtils.generateDate(), 1, false, roundId = 1),
                ArcherRound(2, TestUtils.generateDate(), 2, false, roundId = 2),
                ArcherRound(3, TestUtils.generateDate(), 1, false),
                ArcherRound(4, TestUtils.generateDate(), 1, false, roundId = 1),
                ArcherRound(5, TestUtils.generateDate(), 2, false, roundId = 2),
                ArcherRound(6, TestUtils.generateDate(), 1, false),
        )
        val rounds = TestUtils.ROUNDS.take(3)

        for (round in rounds) {
            runBlocking {
                roundDao.insert(round)
            }
        }
        for (archerRound in archerRounds) {
            runBlocking {
                archerRoundDao.insert(archerRound)
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

    /**
     * Check the correct round name info is retrieved for the given archer round
     */
    @Test
    fun getArcherRoundsWithNamesTest() {
        /*
         * Create data and populate tables
         */
        val archerRounds = listOf(
                ArcherRound(1, TestUtils.generateDate(), 1, false, roundId = 1, roundSubTypeId = 1),
                ArcherRound(2, TestUtils.generateDate(), 2, false, roundId = 2),
                ArcherRound(3, TestUtils.generateDate(), 1, false),
                ArcherRound(4, TestUtils.generateDate(), 1, false, roundId = 1, roundSubTypeId = 1),
                ArcherRound(5, TestUtils.generateDate(), 2, false, roundId = 2),
                ArcherRound(6, TestUtils.generateDate(), 1, false),
        )
        val rounds = TestUtils.ROUNDS.take(3)
        val roundSubTypes = TestUtils.ROUND_SUB_TYPES

        for (round in rounds) {
            runBlocking {
                roundDao.insert(round)
            }
        }
        for (roundSubType in roundSubTypes) {
            runBlocking {
                roundSubTypeDao.insert(roundSubType)
            }
        }
        for (archerRound in archerRounds) {
            runBlocking {
                archerRoundDao.insert(archerRound)
            }
        }

        /*
         * Check the correct round info is retrieved
         */
        val retrievedRoundInfo = archerRoundDao.getAllArcherRoundsWithRoundInfoAndName().retrieveValue()!!
        for (i in retrievedRoundInfo.indices) {
            assertEquals(archerRounds[i], retrievedRoundInfo[i].archerRound)

            val expectedRoundName = rounds.find { it.roundId == archerRounds[i].roundId }
            assert(expectedRoundName == retrievedRoundInfo[i].round)

            val expectedRoundSubTypeName =
                    roundSubTypes.find {
                        it.roundId == archerRounds[i].roundId && it.subTypeId == archerRounds[i].roundSubTypeId
                    }?.name
            assertEquals(expectedRoundSubTypeName, retrievedRoundInfo[i].roundSubType?.name)
        }
    }
}