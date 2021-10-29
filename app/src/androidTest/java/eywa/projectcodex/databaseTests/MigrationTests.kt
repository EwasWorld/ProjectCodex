package eywa.projectcodex.databaseTests

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import eywa.projectcodex.database.DatabaseMigrations
import eywa.projectcodex.database.ScoresRoomDatabase
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// TODO It is recommended you make a test that goes through of all migrations (i.e. from version 1 through to the current versions)
/**
 * Note: there are no migration tests from version 1 because there is no exported schema of version 1
 */
@RunWith(AndroidJUnit4::class)
class MigrationTests {
    companion object {
        private const val TEST_DB_NAME = "migration-test"
    }

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            ScoresRoomDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
    )

    @Ignore("Version 3 appears to be inconsistent and not working")
    fun migrate2To3() {
        helper.createDatabase(TEST_DB_NAME, 2).apply {
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB_NAME, 3, true, DatabaseMigrations.MIGRATION_2_3)
    }

    @Test
    fun migrate2To4() {
        val values = ContentValues().apply {
            put("archerRoundId", 1)
            put("dateShot", 2)
            put("archerId", 3)
            put("bowId", 4)
            put("roundReferenceId", 5)
            put("roundDistanceId", 6)
            put("goalScore", 7)
            put("shootStatus", "practice")
            put("countsTowardsHandicap", 0)
        }

        helper.createDatabase(TEST_DB_NAME, 2).apply {
            insert("archer_rounds", SQLiteDatabase.CONFLICT_IGNORE, values)
            close()
        }

        val db = helper.runMigrationsAndValidate(
                TEST_DB_NAME, 4, true, DatabaseMigrations.MIGRATION_2_3,
                DatabaseMigrations.MIGRATION_3_4
        )

        // Verify data migrated properly
        val response = db.query("SELECT * FROM archer_rounds")
        assertEquals(1, response.count)
        response.moveToFirst()
        for (key in values.keySet()) {
            val newColumnName = when (key) {
                "roundReferenceId" -> "roundId"
                "roundDistanceId" -> "roundSubTypeId"
                else -> key
            }
            val index = response.getColumnIndex(newColumnName)
            when (response.getType(index)) {
                Cursor.FIELD_TYPE_INTEGER -> assertEquals(values.getAsInteger(key), response.getInt(index))
                Cursor.FIELD_TYPE_STRING -> assertEquals(values.getAsString(key), response.getString(index))
            }
        }
    }
}