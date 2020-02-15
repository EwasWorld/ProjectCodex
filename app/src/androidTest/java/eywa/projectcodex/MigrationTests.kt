package eywa.projectcodex

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import eywa.projectcodex.database.ScoresRoomDatabase
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTests {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            ScoresRoomDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate2To3() {
        helper.createDatabase(TEST_DB, 2).apply {
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 3, true, ScoresRoomDatabase.MIGRATION_2_3)
    }

    // TODO It is recommended you make a test that goes through of all migrations (i.e. from version 1 through to the current versions)
    @Ignore("Reference for when a migration has data changes which need to be tested rather than just schema changes")
    @Test
    @Throws(IOException::class)
    fun migrate3To4() {
        var db = helper.createDatabase(TEST_DB, 3).apply {
            // execSQL(...)
            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, ScoresRoomDatabase.MIGRATION_2_3)

        // Verify data migrated properly
    }
}