package eywa.projectcodex

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.daos.*
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.database.entities.Round
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
 */
@RunWith(AndroidJUnit4::class)
class DatabaseTests {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: ScoresRoomDatabase

    private lateinit var archerDao: ArcherDao
    private lateinit var archerRoundDao: ArcherRoundDao
    private lateinit var arrowValueDao: ArrowValueDao
    private lateinit var roundDao: RoundDao
    private lateinit var roundArrowCountDao: RoundArrowCountDao
    private lateinit var roundSubTypeDao: RoundSubTypeDao
    private lateinit var roundDistanceDao: RoundDistanceDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(testDatabaseName)
        db = Room.inMemoryDatabaseBuilder(context, ScoresRoomDatabase::class.java).allowMainThreadQueries().build()
        archerDao = db.archerDao()
        archerRoundDao = db.archerRoundDao()
        arrowValueDao = db.arrowValueDao()
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
     * Check getArrowsForRound returns only that round's arrows
     * Check inserted values are the same as retrieved
     */
    @Test
    @Throws(Exception::class)
    fun arrowValuesTest() {
        val arrows1 = TestData.generateArrowValues(6, 1)
        val arrows2 = TestData.generateArrowValues(12, 2)

        /*
         * Add and retrieve
         */
        for (arrow in arrows1.plus(arrows2)) {
            runBlocking {
                arrowValueDao.insert(arrow)
            }
        }
        var retrievedArrows1 = arrowValueDao.getArrowValuesForRound(1).retrieveValue()!!
        var retrievedArrows2 = arrowValueDao.getArrowValuesForRound(2).retrieveValue()!!

        assertEquals(arrows1.toSet(), retrievedArrows1.toSet())
        assertEquals(arrows2.toSet(), retrievedArrows2.toSet())

        /*
         * Delete
         */
        runBlocking {
            arrowValueDao.deleteRoundsArrows(1)
        }

        retrievedArrows1 = arrowValueDao.getArrowValuesForRound(1).retrieveValue()!!
        retrievedArrows2 = arrowValueDao.getArrowValuesForRound(2).retrieveValue()!!
        assert(retrievedArrows1.isEmpty())
        assertEquals(arrows2.toSet(), retrievedArrows2.toSet())
    }

    /**
     * Check max ID value increases with each insertion
     * Check inserted values are the same as retrieved
     */
    @Test
    @Throws(Exception::class)
    fun archerRoundsTest() {
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
     * Test add, retrieve, update (existent and non-existent record), and remove
     */
    @Test
    @Throws(Exception::class)
    fun roundsTest() {
        val retrievedRounds = roundDao.getAllRounds()

        /*
         * Add and retrieve
         */
        val rounds = TestData.generateRounds().toMutableList()

        for (round in rounds) {
            runBlocking {
                roundDao.insert(round)
            }
        }
        // Not sure why this one isn't working with the normal assertEquals (the expected and actuals look the same)
        TestData.assertEquals(rounds, retrievedRounds.retrieveValue()!!)

        /*
         * Update (existing)
         */
        val round1 = rounds[0]
        val updatedRound1 = Round(
                round1.roundId, round1.name, round1.displayName, !round1.isOutdoor, round1.isMetric,
                round1.permittedFaces, round1.isDefaultRound, round1.fiveArrowEnd
        )
        val round2 = rounds[1]
        val updatedRound2 = Round(
                round2.roundId, round2.name + "cheese", round2.displayName + "cheese",
                round2.isOutdoor, round2.isMetric, round2.permittedFaces,
                round2.isDefaultRound, round2.fiveArrowEnd
        )
        rounds.removeAt(0)
        rounds.removeAt(0)
        rounds.add(0, updatedRound2)
        rounds.add(0, updatedRound1)

        runBlocking {
            roundDao.update(round1)
        }
        runBlocking {
            roundDao.update(round2)
        }
        TestData.assertEquals(rounds, retrievedRounds.retrieveValue()!!)

        /*
         * Update (doesn't exist)
         */
        val nonExistentUpdatedRound = Round(
                rounds.map { it.roundId }.max()!! + 1, "bananas", "bananas", round1.isOutdoor, round1.isMetric,
                round1.permittedFaces, round1.isDefaultRound, round1.fiveArrowEnd
        )
        runBlocking {
            roundDao.update(nonExistentUpdatedRound)
        }
        TestData.assertEquals(rounds, retrievedRounds.retrieveValue()!!)

        /*
         * Delete
         */
        rounds.removeAt(0)
        runBlocking {
            roundDao.delete(round1.roundId)
        }
        TestData.assertEquals(rounds, retrievedRounds.retrieveValue()!!)
    }

    /**
     * Tests adding, retrieving, deleting one entry, and deleting a round's entries
     */
    @Test
    @Throws(Exception::class)
    fun roundArrowCountsTest() {
        val retrievedArrowCounts = roundArrowCountDao.getAllArrowCounts()

        /*
         * Add and retrieve
         */
        val arrowCounts = TestData.generateArrowCounts(5).toMutableList()
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
        arrowCounts.removeIf { it.roundId == 2 }
        assertEquals(arrowCounts.toSet(), retrievedArrowCounts.retrieveValue()!!.toSet())
    }

    /**
     * Tests adding, retrieving, deleting one entry, and deleting a round's entries
     */
    @Test
    @Throws(Exception::class)
    fun roundSubTypesTest() {
        val retrievedSubTypes = roundSubTypeDao.getAllSubTypes()

        /*
         * Add and retrieve
         */
        val subTypes = TestData.generateSubTypes(5).toMutableList()
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
        subTypes.removeIf { it.roundId == 2 }
        assertEquals(subTypes.toSet(), retrievedSubTypes.retrieveValue()!!.toSet())
    }

    /**
     * Tests adding, retrieving, deleting one entry, and deleting a round's entries
     */
    @Test
    @Throws(Exception::class)
    fun roundDistancesTest() {
        val retrievedDistances = roundDistanceDao.getAllDistances()

        /*
         * Add and retrieve
         */
        val distances = TestData.generateDistances(5).toMutableList()
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
        distances.removeIf { it.roundId == 2 }
        assertEquals(distances.toSet(), retrievedDistances.retrieveValue()!!.toSet())
    }

}