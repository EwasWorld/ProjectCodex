package eywa.projectcodex

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.daos.*
import eywa.projectcodex.database.entities.ArcherRound
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
    private lateinit var roundDistanceDao: RoundDistanceDao
    private lateinit var roundReferenceDao: RoundReferenceDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(testDatabaseName)
        db = Room.inMemoryDatabaseBuilder(context, ScoresRoomDatabase::class.java).allowMainThreadQueries().build()
        archerDao = db.archerDao()
        archerRoundDao = db.archerRoundDao()
        arrowValueDao = db.arrowValueDao()
        roundDistanceDao = db.roundDistanceDao()
        roundReferenceDao = db.roundReferenceDao()
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

        for (arrow in arrows1.plus(arrows2)) {
            runBlocking {
                arrowValueDao.insert(arrow)
            }
        }
        val retrievedArrows1 = arrowValueDao.getArrowValuesForRound(1).retrieveValue()!!
        val retrievedArrows2 = arrowValueDao.getArrowValuesForRound(2).retrieveValue()!!

        assertEquals(arrows1.toSet(), retrievedArrows1.toSet())
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
    }

}