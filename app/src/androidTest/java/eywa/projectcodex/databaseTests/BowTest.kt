package eywa.projectcodex.databaseTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.bow.BowRepo
import eywa.projectcodex.database.bow.DatabaseBow
import eywa.projectcodex.database.bow.DatabaseBowPreviewHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class BowTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: ScoresRoomDatabase
    private lateinit var bowRepo: BowRepo

    @Before
    fun createDb() {
        db = DatabaseTestUtils.createDatabase()
        bowRepo = db.bowRepo()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testGetDefaultBow() = runBlocking {
        assertEquals(
                emptyList<DatabaseBow>(),
                db.bowDao().getAllBows().first(),
        )

        bowRepo.insertDefaultBowIfNotExist()
        assertEquals(
                listOf(DatabaseBowPreviewHelper.default),
                db.bowDao().getAllBows().first(),
        )

        bowRepo.insertDefaultBowIfNotExist()
        assertEquals(
                listOf(DatabaseBowPreviewHelper.default),
                db.bowDao().getAllBows().first(),
        )

        val bows = listOf(
                DatabaseBowPreviewHelper.default,
                DatabaseBow(bowId = 1, name = "One", isSightMarkDiagramHighestAtTop = true),
                DatabaseBow(bowId = 2, name = "Two", isSightMarkDiagramHighestAtTop = false),
        )

        bows.drop(1).forEach {
            db.bowDao().insert(it.copy(bowId = 0))
        }

        assertEquals(bows.toSet(), db.bowDao().getAllBows().first().toSet())
    }
}
