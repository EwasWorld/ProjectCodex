package eywa.projectcodex.databaseTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import eywa.projectcodex.TestData
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.daos.ArrowValueDao
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.database.repositories.ArrowValuesRepo
import eywa.projectcodex.retrieveValue
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ArrowValueTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: ScoresRoomDatabase
    private lateinit var arrowValueDao: ArrowValueDao

    @Before
    fun createDb() {
        db = DatabaseSuite.createDatabase()
        arrowValueDao = db.arrowValueDao()
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
    fun basicTest() {
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

        Assert.assertEquals(arrows1.toSet(), retrievedArrows1.toSet())
        Assert.assertEquals(arrows2.toSet(), retrievedArrows2.toSet())

        /*
         * Delete
         */
        runBlocking {
            arrowValueDao.deleteRoundsArrows(1)
        }

        retrievedArrows1 = arrowValueDao.getArrowValuesForRound(1).retrieveValue()!!
        retrievedArrows2 = arrowValueDao.getArrowValuesForRound(2).retrieveValue()!!
        assert(retrievedArrows1.isEmpty())
        Assert.assertEquals(arrows2.toSet(), retrievedArrows2.toSet())
    }

    @Test
    fun deleteSpecificArrowNumbersTest() {
        val arrows1 = TestData.generateArrowValues(18, 1)
        val arrows2 = TestData.generateArrowValues(18, 2)
        for (arrow in arrows1.plus(arrows2)) {
            runBlocking {
                arrowValueDao.insert(arrow)
            }
        }

        /*
         * Delete
         */
        val from = 7
        val count = 6
        runBlocking {
            arrowValueDao.deleteArrowsBetween(1, from, from + count)
        }

        val retrievedArrows1 = arrowValueDao.getArrowValuesForRound(1).retrieveValue()!!
        val retrievedArrows2 = arrowValueDao.getArrowValuesForRound(2).retrieveValue()!!
        Assert.assertEquals(
                arrows1.filter { it.arrowNumber < from || it.arrowNumber >= from + count }.toSet(),
                retrievedArrows1.toSet()
        )
        Assert.assertEquals(arrows2.toSet(), retrievedArrows2.toSet())
    }

    /**
     * For no apparent reason, I can't run this test on its own when its called `deleteEndRepoTest` -Ewa Oct 2020
     */
    @Test
    fun deleteEndRepoTst() {
        val archerRoundId = 1
        val arrowValuesRepo = ArrowValuesRepo(arrowValueDao, archerRoundId)

        for (arrowNumber in 1..24) {
            runBlocking {
                arrowValueDao.insert(
                        TestData.ARROWS[arrowNumber % TestData.ARROWS.size].toArrowValue(archerRoundId, arrowNumber)
                )
            }
        }

        /*
         * Delete
         */
        val from = 7
        val count = 6
        val originalArrows = arrowValueDao.getArrowValuesForRound(archerRoundId).retrieveValue()!!
        runBlocking {
            arrowValuesRepo.deleteEnd(originalArrows, from, count)
        }

        val expectedArrows = mutableListOf<ArrowValue>()
        for (arrowNumber in 1..(24 - count)) {
            val testDataIndex = (if (arrowNumber < from) arrowNumber else arrowNumber + count) % TestData.ARROWS.size
            expectedArrows.add(TestData.ARROWS[testDataIndex].toArrowValue(archerRoundId, arrowNumber))
        }
        val retrievedArrows = arrowValueDao.getArrowValuesForRound(archerRoundId).retrieveValue()!!
        Assert.assertEquals(expectedArrows.toSet(), retrievedArrows.toSet())
    }

    @Test
    fun insertEndRepoTest() {
        val archerRoundId = 1
        val arrowValuesRepo = ArrowValuesRepo(arrowValueDao, archerRoundId)

        for (arrowNumber in 1..24) {
            runBlocking {
                arrowValueDao.insert(
                        TestData.ARROWS[arrowNumber % TestData.ARROWS.size].toArrowValue(archerRoundId, arrowNumber)
                )
            }
        }

        /*
         * Insert
         */
        val originalArrows = arrowValueDao.getArrowValuesForRound(archerRoundId).retrieveValue()!!
        val at = 5
        var newArrowId = at
        val newArrows = (7 until 14).map {
            TestData.ARROWS[it % TestData.ARROWS.size].toArrowValue(
                    archerRoundId, newArrowId++
            )
        }
        runBlocking {
            arrowValuesRepo.insertEnd(originalArrows, newArrows)
        }

        val expectedArrows = newArrows.toMutableSet()
        for (arrow in originalArrows) {
            val newArrNum = if (arrow.arrowNumber < at) arrow.arrowNumber else arrow.arrowNumber + newArrows.size
            expectedArrows.add(ArrowValue(archerRoundId, newArrNum, arrow.score, arrow.isX))
        }

        val retrievedArrows = arrowValueDao.getArrowValuesForRound(archerRoundId).retrieveValue()!!
        Assert.assertEquals(expectedArrows, retrievedArrows.toSet())
    }
}