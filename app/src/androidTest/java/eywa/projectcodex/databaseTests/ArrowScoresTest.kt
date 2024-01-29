package eywa.projectcodex.databaseTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.utils.ListUtils.plusAtIndex
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.hiltModules.LocalDatabaseModule.Companion.add
import eywa.projectcodex.model.Arrow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ArrowScoresTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: ScoresRoomDatabase

    private val arrowSet = (TestUtils.ARROWS + TestUtils.ARROWS)
    private fun List<Arrow>.asArrowScores(firstArrowIndex: Int = 1) =
            mapIndexed { index, arrow -> arrow.asArrowScore(1, index + firstArrowIndex) }

    @Before
    fun createDb() {
        db = DatabaseTestUtils.createDatabase()
        runBlocking {
            db.add(ShootPreviewHelperDsl.create { })
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testDeleteEnd_beginning() = runTest {
        val arrowScoresRepo = db.arrowScoresRepo()
        arrowScoresRepo.insert(*arrowSet.asArrowScores().toTypedArray())

        arrowScoresRepo.deleteEnd(arrowSet.asArrowScores(), 1, 3)
        assertEquals(
                arrowSet.drop(3).asArrowScores().toSet(),
                db.arrowScoreDao().getAllArrows().first().toSet(),
        )
    }

    @Test
    fun testDeleteEnd_middle() = runTest {
        val arrowScoresRepo = db.arrowScoresRepo()
        arrowScoresRepo.insert(*arrowSet.asArrowScores().toTypedArray())

        arrowScoresRepo.deleteEnd(arrowSet.asArrowScores(), 7, 3)
        assertEquals(
                arrowSet.filterIndexed { index, _ -> index !in listOf(6, 7, 8) }.asArrowScores().toSet(),
                db.arrowScoreDao().getAllArrows().first().toSet(),
        )
    }

    @Test
    fun testDeleteEnd_end() = runTest {
        val arrowScoresRepo = db.arrowScoresRepo()
        arrowScoresRepo.insert(*arrowSet.asArrowScores().toTypedArray())

        arrowScoresRepo.deleteEnd(arrowSet.asArrowScores(), 22, 3)
        assertEquals(
                arrowSet.dropLast(3).asArrowScores().toSet(),
                db.arrowScoreDao().getAllArrows().first().toSet(),
        )
    }

    @Test
    fun testDeleteEnd_deleteTooMany() = runTest {
        val arrowScoresRepo = db.arrowScoresRepo()
        arrowScoresRepo.insert(*arrowSet.asArrowScores().toTypedArray())

        arrowScoresRepo.deleteEnd(arrowSet.asArrowScores(), 22, 6)
        assertEquals(
                arrowSet.dropLast(3).asArrowScores().toSet(),
                db.arrowScoreDao().getAllArrows().first().toSet(),
        )
    }

    @Test
    fun insertEndRepoTest_beginning() = runTest {
        val arrowScoresRepo = db.arrowScoresRepo()
        arrowScoresRepo.insert(*arrowSet.asArrowScores().toTypedArray())

        arrowScoresRepo.insertEnd(
                arrowSet.asArrowScores(),
                TestUtils.ARROWS.asArrowScores(),
        )
        assertEquals(
                (TestUtils.ARROWS + arrowSet).asArrowScores().toSet(),
                db.arrowScoreDao().getAllArrows().first().toSet(),
        )
    }

    @Test
    fun insertEndRepoTest_middle() = runTest {
        val arrowScoresRepo = db.arrowScoresRepo()
        arrowScoresRepo.insert(*arrowSet.asArrowScores().toTypedArray())

        arrowScoresRepo.insertEnd(
                arrowSet.asArrowScores(),
                TestUtils.ARROWS.asArrowScores(7),
        )
        assertEquals(
                arrowSet.plusAtIndex(TestUtils.ARROWS, 6).asArrowScores().toSet(),
                db.arrowScoreDao().getAllArrows().first().toSet(),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun insertEndRepoTest_end() = runTest {
        val arrowScoresRepo = db.arrowScoresRepo()
        arrowScoresRepo.insert(*arrowSet.asArrowScores().toTypedArray())

        arrowScoresRepo.insertEnd(
                arrowSet.asArrowScores(),
                TestUtils.ARROWS.asArrowScores(25),
        )
    }
}
