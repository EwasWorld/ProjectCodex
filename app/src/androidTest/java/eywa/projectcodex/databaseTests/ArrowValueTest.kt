package eywa.projectcodex.databaseTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRoundDao
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.arrowValue.ArrowValueDao
import eywa.projectcodex.database.arrowValue.ArrowValuesRepo
import eywa.projectcodex.databaseTests.DatabaseTestUtils.brokenTransactionMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ArrowValueTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: ScoresRoomDatabase
    private lateinit var arrowValueDao: ArrowValueDao
    private lateinit var archerRoundDao: ArcherRoundDao

    @Before
    fun createDb() {
        db = DatabaseTestUtils.createDatabase()
        archerRoundDao = db.archerRoundDao()
        arrowValueDao = db.arrowValueDao()

        runBlocking {
            TestUtils.generateArcherRounds(2).forEach { archerRoundDao.insert(it) }
        }
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
    fun basicTest() = runTest {
        val arrows1 = TestUtils.generateArrowValues(1, 6)
        val arrows2 = TestUtils.generateArrowValues(2, 12)

        /*
         * Add and retrieve
         */
        for (arrow in arrows1.plus(arrows2)) {
            arrowValueDao.insert(arrow)
        }
        var retrievedArrows1 = archerRoundDao.getFullArcherRoundInfo(1).map { it?.arrows }.first()!!
        var retrievedArrows2 = archerRoundDao.getFullArcherRoundInfo(2).map { it?.arrows }.first()!!

        Assert.assertEquals(arrows1.toSet(), retrievedArrows1.toSet())
        Assert.assertEquals(arrows2.toSet(), retrievedArrows2.toSet())

        /*
         * Delete
         */
        arrowValueDao.deleteRoundsArrows(1)

        retrievedArrows1 = archerRoundDao.getFullArcherRoundInfo(1).map { it?.arrows }.first()!!
        retrievedArrows2 = archerRoundDao.getFullArcherRoundInfo(2).map { it?.arrows }.first()!!
        assert(retrievedArrows1.isEmpty())
        Assert.assertEquals(arrows2.toSet(), retrievedArrows2.toSet())
    }

    @Test
    fun deleteSpecificArrowNumbersTest() = runTest {
        val arrows1 = TestUtils.generateArrowValues(1, 18)
        val arrows2 = TestUtils.generateArrowValues(2, 18)
        for (arrow in arrows1.plus(arrows2)) {
            arrowValueDao.insert(arrow)
        }

        /*
         * Delete
         */
        val from = 7
        val count = 6
        arrowValueDao.deleteArrowsBetween(1, from, from + count)

        val retrievedArrows1 = archerRoundDao.getFullArcherRoundInfo(1).map { it?.arrows }.first()!!
        val retrievedArrows2 = archerRoundDao.getFullArcherRoundInfo(2).map { it?.arrows }.first()!!
        Assert.assertEquals(
                arrows1.filter { it.arrowNumber < from || it.arrowNumber >= from + count }.toSet(),
                retrievedArrows1.toSet()
        )
        Assert.assertEquals(arrows2.toSet(), retrievedArrows2.toSet())
    }

    /**
     * For no apparent reason, I can't run this test on its own when its called `deleteEndRepoTest` -Ewa Oct 2020
     */
    @Ignore(brokenTransactionMessage)
    @Test
    fun deleteEndRepoTst() = runTest {
        val archerRoundId = 1
        val arrowValuesRepo = ArrowValuesRepo(arrowValueDao)

        for (arrowNumber in 1..24) {
            runBlocking {
                arrowValueDao.insert(
                        TestUtils.ARROWS[arrowNumber % TestUtils.ARROWS.size].toArrowValue(archerRoundId, arrowNumber)
                )
            }
        }

        /*
         * Delete
         */
        val from = 7
        val count = 6
        val originalArrows = archerRoundDao.getFullArcherRoundInfo(archerRoundId).map { it?.arrows }.first()!!
        runBlocking {
            arrowValuesRepo.deleteEnd(originalArrows, from, count)
        }

        /*
         * Check
         */
        val expectedArrows = mutableListOf<ArrowValue>()
        for (arrowNumber in 1..(24 - count)) {
            val testDataIndex = (if (arrowNumber < from) arrowNumber else arrowNumber + count) % TestUtils.ARROWS.size
            expectedArrows.add(TestUtils.ARROWS[testDataIndex].toArrowValue(archerRoundId, arrowNumber))
        }
        val retrievedArrows = archerRoundDao.getFullArcherRoundInfo(archerRoundId).map { it?.arrows }.first()!!
        Assert.assertEquals(expectedArrows.toSet(), retrievedArrows.toSet())
    }

    // TODO Unignore
    @Ignore(brokenTransactionMessage)
    @Test
    fun insertEndRepoTest() = runTest {
        val archerRoundId = 1
        val arrowValuesRepo = ArrowValuesRepo(arrowValueDao)

        for (arrowNumber in 1..24) {
            arrowValueDao.insert(
                    TestUtils.ARROWS[arrowNumber % TestUtils.ARROWS.size].toArrowValue(archerRoundId, arrowNumber)
            )
        }

        /*
         * Insert
         */
        val originalArrows = archerRoundDao.getFullArcherRoundInfo(archerRoundId).map { it?.arrows }.first()!!
        val at = 5
        var newArrowId = at
        val newArrows = (7 until 14).map {
            TestUtils.ARROWS[it % TestUtils.ARROWS.size].toArrowValue(
                    archerRoundId, newArrowId++
            )
        }
        arrowValuesRepo.insertEnd(originalArrows, newArrows)

        /*
         * Check
         */
        val expectedArrows = newArrows.toMutableSet()
        for (arrow in originalArrows) {
            val newArrNum = if (arrow.arrowNumber < at) arrow.arrowNumber else arrow.arrowNumber + newArrows.size
            expectedArrows.add(ArrowValue(archerRoundId, newArrNum, arrow.score, arrow.isX))
        }

        val retrievedArrows = archerRoundDao.getFullArcherRoundInfo(archerRoundId).map { it?.arrows }.first()!!
        Assert.assertEquals(expectedArrows, retrievedArrows.toSet())
    }
}
