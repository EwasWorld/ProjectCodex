package eywa.projectcodex.databaseTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.retrieveValue
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.rounds.RoundArrowCountDao
import eywa.projectcodex.database.rounds.RoundDao
import eywa.projectcodex.database.rounds.RoundDistanceDao
import eywa.projectcodex.database.rounds.RoundSubTypeDao
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


/**
 * Test DAOs
 * TODO Split these into separate classes
 */
@RunWith(AndroidJUnit4::class)
class GeneralDatabaseTests {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: ScoresRoomDatabase

    private lateinit var roundDao: RoundDao
    private lateinit var roundArrowCountDao: RoundArrowCountDao
    private lateinit var roundSubTypeDao: RoundSubTypeDao
    private lateinit var roundDistanceDao: RoundDistanceDao

    @Before
    fun createDb() {
        db = DatabaseTestUtils.createDatabase()
        roundDao = db.roundDao()
        roundArrowCountDao = db.roundArrowCountDao()
        roundSubTypeDao = db.roundSubTypeDao()
        roundDistanceDao = db.roundDistanceDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    /**
     * Test add, retrieve, update (existent and non-existent record), and remove
     */
    @Test
    fun roundsTest() {
        val retrievedRounds = roundDao.getAllRounds()

        /*
         * Add and retrieve
         */
        val rounds = TestUtils.ROUNDS.toMutableList()

        for (round in rounds) {
            runBlocking {
                roundDao.insert(round)
            }
        }
        assertEquals(rounds.toSet(), retrievedRounds.retrieveValue()!!.toSet())

        /*
         * Update (existing)
         */
        val round1 = rounds[0]
        val updatedRound1 = round1.copy(isOutdoor = !round1.isOutdoor)
        val round2 = rounds[1]
        val updatedRound2 = round2.copy(
                name = round2.name + "cheese",
                displayName = round2.displayName + "cheese",
        )
        rounds.removeAt(0)
        rounds.removeAt(0)
        rounds.add(0, updatedRound2)
        rounds.add(0, updatedRound1)

        runBlocking {
            roundDao.update(updatedRound1)
        }
        runBlocking {
            roundDao.update(updatedRound2)
        }
        assertEquals(rounds.toSet(), retrievedRounds.retrieveValue()!!.toSet())

        /*
         * Update (doesn't exist)
         */
        val nonExistentUpdatedRound = round1.copy(
                roundId = rounds.maxOf { it.roundId } + 1,
                name = "bananas",
                displayName = "bananas",
        )
        runBlocking {
            roundDao.update(nonExistentUpdatedRound)
        }
        assertEquals(rounds.toSet(), retrievedRounds.retrieveValue()!!.toSet())

        /*
         * Delete
         */
        rounds.removeAt(0)
        runBlocking {
            roundDao.delete(round1.roundId)
        }
        assertEquals(rounds.toSet(), retrievedRounds.retrieveValue()!!.toSet())
    }

    /**
     * Tests adding, retrieving, deleting one entry, and deleting a round's entries
     */
    @Test
    fun roundArrowCountsTest() {
        runBlocking {
            TestUtils.ROUNDS.forEach { roundDao.insert(it) }
        }

        val retrievedArrowCounts = roundArrowCountDao.getAllArrowCounts()

        /*
         * Add and retrieve
         */
        val arrowCounts = TestUtils.ROUND_ARROW_COUNTS.toMutableList()
        for (arrowCount in arrowCounts) {
            runBlocking {
                roundArrowCountDao.insert(arrowCount)
            }
        }
        assertEquals(arrowCounts.toSet(), retrievedArrowCounts.retrieveValue()!!.toSet())

        /*
         * Delete
         */
        val deleteArrowCount = arrowCounts[0]
        arrowCounts.removeAt(0)
        runBlocking {
            roundArrowCountDao.delete(deleteArrowCount.roundId, deleteArrowCount.distanceNumber)
        }
        assertEquals(arrowCounts.toSet(), retrievedArrowCounts.retrieveValue()!!.toSet())

        /*
         * Delete all
         */
        runBlocking {
            roundArrowCountDao.deleteAll(2)
        }
        assertEquals(arrowCounts.filterNot { it.roundId == 2 }.toSet(), retrievedArrowCounts.retrieveValue()!!.toSet())
    }

    /**
     * Tests adding, retrieving, deleting one entry, and deleting a round's entries
     */
    @Test
    fun roundSubTypesTest() {
        runBlocking {
            TestUtils.ROUNDS.forEach { roundDao.insert(it) }
        }

        val retrievedSubTypes = roundSubTypeDao.getAllSubTypes()

        /*
         * Add and retrieve
         */
        val subTypes = TestUtils.ROUND_SUB_TYPES.toMutableList()
        for (subType in subTypes) {
            runBlocking {
                roundSubTypeDao.insert(subType)
            }
        }
        assertEquals(subTypes.toSet(), retrievedSubTypes.retrieveValue()!!.toSet())

        /*
         * Delete
         */
        val deleteSubType = subTypes[0]
        subTypes.removeAt(0)
        runBlocking {
            roundSubTypeDao.delete(deleteSubType.roundId, deleteSubType.subTypeId)
        }
        assertEquals(subTypes.toSet(), retrievedSubTypes.retrieveValue()!!.toSet())

        /*
         * Delete all
         */
        runBlocking {
            roundSubTypeDao.deleteAll(2)
        }
        assertEquals(subTypes.filterNot { it.roundId == 2 }.toSet(), retrievedSubTypes.retrieveValue()!!.toSet())
    }

    /**
     * Tests adding, retrieving, deleting one entry, and deleting a round's entries
     */
    @Test
    fun roundDistancesTest() {
        runBlocking {
            TestUtils.ROUNDS.forEach { roundDao.insert(it) }
        }

        val retrievedDistances = roundDistanceDao.getAllDistances()

        /*
         * Add and retrieve
         */
        val distances = TestUtils.ROUND_DISTANCES.toMutableList()
        for (distance in distances) {
            runBlocking {
                roundDistanceDao.insert(distance)
            }
        }
        assertEquals(distances.toSet(), retrievedDistances.retrieveValue()!!.toSet())

        /*
         * Delete
         */
        val deleteDistance = distances[0]
        distances.removeAt(0)
        runBlocking {
            roundDistanceDao.delete(
                    deleteDistance.roundId, deleteDistance.distanceNumber, deleteDistance.subTypeId
            )
        }
        assertEquals(distances.toSet(), retrievedDistances.retrieveValue()!!.toSet())

        /*
         * Delete all
         */
        runBlocking {
            roundDistanceDao.deleteAll(2)
        }
        assertEquals(distances.filterNot { it.roundId == 2 }.toSet(), retrievedDistances.retrieveValue()!!.toSet())
    }
}
