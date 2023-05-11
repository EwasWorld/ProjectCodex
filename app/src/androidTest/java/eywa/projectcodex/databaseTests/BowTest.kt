package eywa.projectcodex.databaseTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.bow.DatabaseBow
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

    @Before
    fun createDb() {
        db = DatabaseTestUtils.createDatabase()

        runBlocking {
            db.insertDefaults()
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testGetDefaultBow() = runBlocking {
        val defaultBowId = -1
        val bows = listOf(
                DatabaseBow(defaultBowId, false),
                DatabaseBow(1, true),
                DatabaseBow(2, false),
        )

        bows.forEach {
            if (it.id != defaultBowId) {
                db.bowDao().insert(it.copy(id = 0))
            }
        }

        assertEquals(bows.toSet(), db.bowDao().getAllBows().first().toSet())
    }
}
