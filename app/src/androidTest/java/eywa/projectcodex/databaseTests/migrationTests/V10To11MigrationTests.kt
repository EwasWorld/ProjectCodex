package eywa.projectcodex.databaseTests.migrationTests

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.room.migration.AutoMigrationSpec
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.migrations.MIGRATION_10_11
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class V10To11MigrationTests {
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            ScoresRoomDatabase::class.java,
            listOf<AutoMigrationSpec>(),
            FrameworkSQLiteOpenHelperFactory(),
    )

    /**
     * Creates the version 11 database and runs the [setup] on it,
     * then runs the migration to version 12 and returns the result
     */
    private fun createAndRunMigrations(setup: SupportSQLiteDatabase.() -> Unit): SupportSQLiteDatabase {
        helper.createDatabase(GeneralMigrationTests.TEST_DB_NAME, 10).apply {
            setup()
            close()
        }

        return helper.runMigrationsAndValidate(
                GeneralMigrationTests.TEST_DB_NAME,
                11,
                true,
                MIGRATION_10_11,
        )
    }

    @Test
    fun testMigrateBow() {
        val oldValues = ContentValues().apply {
            put("id", 1)
            put("isSightMarkDiagramHighestAtTop", 0)
        }

        val db = createAndRunMigrations {
            insert("bows", SQLiteDatabase.CONFLICT_ABORT, oldValues)
        }

        val response = db.query("SELECT * FROM bows")
        assertEquals(1, response.count)
        response.moveToFirst()

        val newValues = mapOf(
                "id" to 1,
                "isSightMarkDiagramHighestAtTop" to 0,
                "name" to "Default",
                "description" to null,
                "type" to 0, // Recurve
        )

        checkValues(response, newValues)
    }

    @Test
    fun testMigrateArchers() {
        val oldValues = ContentValues().apply {
            put("archerId", 1)
            put("name", "Hello")
        }

        val db = createAndRunMigrations {
            insert("archers", SQLiteDatabase.CONFLICT_ABORT, oldValues)
        }

        val response = db.query("SELECT * FROM archers")
        assertEquals(1, response.count)
        response.moveToFirst()

        val newValues = mapOf(
                "archerId" to 1,
                "name" to "Hello",
                "isGent" to 1,
                "age" to 1, // Senior
        )

        checkValues(response, newValues)
    }

    private fun checkValues(cursor: Cursor, expectedValues: Map<String, Any?>) {
        expectedValues.forEach { (columnName, expectedValue) ->
            checkValue(cursor, columnName, expectedValue)
        }
    }

    private fun checkValue(cursor: Cursor, columnName: String, expectedValue: Any?) {
        when (expectedValue) {
            is Int -> assertEquals(expectedValue, cursor.getIntOrNull(cursor.getColumnIndex(columnName)))
            is String -> assertEquals(expectedValue, cursor.getStringOrNull(cursor.getColumnIndex(columnName)))
        }
    }
}
