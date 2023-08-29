package eywa.projectcodex.databaseTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsPreviewHelper
import eywa.projectcodex.database.ScoresRoomDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ArcherTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: ScoresRoomDatabase

    @Before
    fun createDb() {
        db = DatabaseTestUtils.createDatabase()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testGetLatestHandicapsForDefaultArcher() = runTest {
        db.insertDefaults()
        val handicaps = ArcherHandicapsPreviewHelper.handicaps
        handicaps.forEach {
            db.archerHandicapDao().insert(it)
        }

        assertEquals(
                handicaps.take(3).toSet(),
                db.archerRepo().latestHandicapsForDefaultArcher.first().toSet(),
        )
    }
}
