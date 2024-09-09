package eywa.projectcodex.databaseTests.migrationTests

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.ScoresRoomDatabaseImpl
import eywa.projectcodex.database.migrations.DatabaseMigrations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Note: there are no migration tests from version 1 because there is no exported schema of version 1
 *
 * Note: It is recommended you make a test that goes through of all migrations
 * (i.e. from version 1 through to the current versions)
 */
@RunWith(AndroidJUnit4::class)
class GeneralMigrationTests {
    companion object {
        internal const val TEST_DB_NAME = "migration-test"
    }

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            ScoresRoomDatabaseImpl::class.java,
            listOf(),
            FrameworkSQLiteOpenHelperFactory(),
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

    @Test
    fun migrate4To6() {
        val rounds = listOf(
                // No subtypes
                ContentValues().apply {
                    put("roundId", 1)
                    put("name", "nosub")
                    put("displayName", "no sub")
                    put("isOutdoor", 0)
                    put("isMetric", 0)
                    put("permittedFaces", "")
                    put("isDefaultRound", 0)
                    put("fiveArrowEnd", 0)
                },
                // Single subtype
                ContentValues().apply {
                    put("roundId", 2)
                    put("name", "onesub")
                    put("displayName", "one sub")
                    put("isOutdoor", 1)
                    put("isMetric", 0)
                    put("permittedFaces", "")
                    put("isDefaultRound", 0)
                    put("fiveArrowEnd", 0)
                },
                // Has subtypes
                ContentValues().apply {
                    put("roundId", 3)
                    put("name", "hassub")
                    put("displayName", "has sub")
                    put("isOutdoor", 1)
                    put("isMetric", 1)
                    put("permittedFaces", "")
                    put("isDefaultRound", 0)
                    put("fiveArrowEnd", 0)
                },
        )

        val roundSubtypes = listOf(
                // Valid roundId - one subtypes
                ContentValues().apply {
                    put("roundId", 2)
                    put("subTypeId", 1)
                    put("name", "single subtype")
                },
                // Valid roundId - multiple subtypes
                ContentValues().apply {
                    put("roundId", 3)
                    put("subTypeId", 1)
                    put("name", "subtype 1")
                },
                ContentValues().apply {
                    put("roundId", 3)
                    put("subTypeId", 2)
                    put("name", "subtype 2")
                },
                // Invalid roundId
                ContentValues().apply {
                    put("roundId", 15)
                    put("subTypeId", 1)
                    put("name", "invalid subtype")
                },
        )

        val roundArrowCounts = listOf(
                // Valid roundId
                ContentValues().apply {
                    put("roundId", 1)
                    put("distanceNumber", 1)
                    put("faceSizeInCm", 122.0)
                    put("arrowCount", 10)
                },
                ContentValues().apply {
                    put("roundId", 2)
                    put("distanceNumber", 1)
                    put("faceSizeInCm", 122.0)
                    put("arrowCount", 20)
                },
                ContentValues().apply {
                    put("roundId", 3)
                    put("distanceNumber", 1)
                    put("faceSizeInCm", 122.0)
                    put("arrowCount", 30)
                },
                // Invalid roundId
                ContentValues().apply {
                    put("roundId", 15)
                    put("distanceNumber", 1)
                    put("faceSizeInCm", 122.0)
                    put("arrowCount", 40)
                },
        )

        val roundDistances = listOf(
                // Valid roundId
                ContentValues().apply {
                    put("roundId", 1)
                    put("distanceNumber", 1)
                    put("subTypeId", 1)
                    put("distance", 10)
                },
                ContentValues().apply {
                    put("roundId", 2)
                    put("distanceNumber", 1)
                    put("subTypeId", 1)
                    put("distance", 20)
                },
                ContentValues().apply {
                    put("roundId", 3)
                    put("distanceNumber", 1)
                    put("subTypeId", 1)
                    put("distance", 30)
                },
                ContentValues().apply {
                    put("roundId", 3)
                    put("distanceNumber", 2)
                    put("subTypeId", 1)
                    put("distance", 40)
                },
                // Invalid roundId
                ContentValues().apply {
                    put("roundId", 15)
                    put("distanceNumber", 1)
                    put("subTypeId", 1)
                    put("distance", 50)
                },
        )

        val archerRounds = listOf(
                // No round
                ContentValues().apply {
                    put("archerRoundId", 1)
                    put("dateShot", 1)
                    put("archerId", 1)
                    put("countsTowardsHandicap", 0)
                },
                // Valid round, no sub type
                ContentValues().apply {
                    put("archerRoundId", 2)
                    put("dateShot", 2)
                    put("archerId", 1)
                    put("countsTowardsHandicap", 0)
                    put("roundId", 1)
                },
                // Valid round, no sub type (but set to 1 anyway)
                ContentValues().apply {
                    put("archerRoundId", 3)
                    put("dateShot", 3)
                    put("archerId", 1)
                    put("countsTowardsHandicap", 0)
                    put("roundId", 1)
                    put("roundSubTypeId", 1)
                },
                // Valid round, valid one sub type
                ContentValues().apply {
                    put("archerRoundId", 4)
                    put("dateShot", 4)
                    put("archerId", 1)
                    put("countsTowardsHandicap", 0)
                    put("roundId", 2)
                    put("roundSubTypeId", 1)
                },
                ContentValues().apply {
                    put("archerRoundId", 5)
                    put("dateShot", 5)
                    put("archerId", 1)
                    put("countsTowardsHandicap", 0)
                    put("roundId", 2)
                },
                // Valid round, valid multiple sub type
                ContentValues().apply {
                    put("archerRoundId", 6)
                    put("dateShot", 6)
                    put("archerId", 1)
                    put("countsTowardsHandicap", 0)
                    put("roundId", 3)
                    put("roundSubTypeId", 2)
                },
                // Valid round, invalid sub type
                ContentValues().apply {
                    put("archerRoundId", 7)
                    put("dateShot", 7)
                    put("archerId", 1)
                    put("countsTowardsHandicap", 0)
                    put("roundId", 3)
                    put("roundSubTypeId", 15)
                },
                // Invalid round
                ContentValues().apply {
                    put("archerRoundId", 8)
                    put("dateShot", 8)
                    put("archerId", 1)
                    put("countsTowardsHandicap", 0)
                    put("roundId", 15)
                },
        )

        val arrowValues = listOf(
                // Valid archerRoundId
                ContentValues().apply {
                    put("archerRoundId", 1)
                    put("arrowNumber", 1)
                    put("score", 1)
                    put("isX", 0)
                },
                ContentValues().apply {
                    put("archerRoundId", 1)
                    put("arrowNumber", 2)
                    put("score", 2)
                    put("isX", 0)
                },
                // Invalid archerRoundId
                ContentValues().apply {
                    put("archerRoundId", 15)
                    put("arrowNumber", 1)
                    put("score", 3)
                    put("isX", 0)
                },
                ContentValues().apply {
                    put("archerRoundId", 15)
                    put("arrowNumber", 2)
                    put("score", 4)
                    put("isX", 0)
                },
        )

        helper.createDatabase(TEST_DB_NAME, 4).apply {
            rounds.forEach {
                insert("rounds", SQLiteDatabase.CONFLICT_ABORT, it)
            }
            roundSubtypes.forEach {
                insert("round_sub_types", SQLiteDatabase.CONFLICT_ABORT, it)
            }
            roundArrowCounts.forEach {
                insert("round_arrow_counts", SQLiteDatabase.CONFLICT_ABORT, it)
            }
            roundDistances.forEach {
                insert("round_distances", SQLiteDatabase.CONFLICT_ABORT, it)
            }
            archerRounds.forEach {
                insert("archer_rounds", SQLiteDatabase.CONFLICT_ABORT, it)
            }
            arrowValues.forEach {
                insert("arrow_values", SQLiteDatabase.CONFLICT_ABORT, it)
            }
            close()
        }

        val db = helper.runMigrationsAndValidate(
                TEST_DB_NAME,
                6,
                true,
                DatabaseMigrations.MIGRATION_4_5,
        )

        /*
         * Verify data migrated properly
         */

        // rounds - no change
        var response = db.query("SELECT name FROM rounds")
        assertEquals(3, response.count)
        response.moveToFirst()
        for (name in rounds.map { it.getAsString("name") }) {
            assertEquals(name, response.getString(0))
            response.moveToNext()
        }

        // roundSubtypes - delete invalid
        response = db.query("SELECT name FROM round_sub_types")
        assertEquals(3, response.count)
        response.moveToFirst()
        for (name in roundSubtypes.dropLast(1).map { it.getAsString("name") }) {
            assertEquals(name, response.getString(0))
            response.moveToNext()
        }

        // roundArrowCounts - delete invalid
        response = db.query("SELECT arrowCount FROM round_arrow_counts")
        assertEquals(3, response.count)
        response.moveToFirst()
        for (arrowCount in roundArrowCounts.dropLast(1).map { it.getAsInteger("arrowCount") }) {
            assertEquals(arrowCount, response.getInt(0))
            response.moveToNext()
        }

        // roundDistances - delete invalid
        response = db.query("SELECT distance FROM round_distances")
        assertEquals(4, response.count)
        response.moveToFirst()
        for (distance in roundDistances.dropLast(1).map { it.getAsInteger("distance") }) {
            assertEquals(distance, response.getInt(0))
            response.moveToNext()
        }

        // arrow_values - delete invalid
        response = db.query("SELECT score FROM arrow_values")
        assertEquals(2, response.count)
        response.moveToFirst()
        for (score in arrowValues.dropLast(2).map { it.getAsInteger("score") }) {
            assertEquals(score, response.getInt(0))
            response.moveToNext()
        }

        // archer_rounds - remove round and/or roundSubTypeId if invalid
        response = db.query("SELECT archerRoundId, roundId, roundSubTypeId FROM archer_rounds")
        assertEquals(8, response.count)
        response.moveToFirst()
        for ((id, roundId, subtypeId) in archerRounds
                .map {
                    Triple(
                            it.getAsInteger("archerRoundId"),
                            it.getAsInteger("roundId"),
                            it.getAsInteger("roundSubTypeId"),
                    )
                }
        ) {
            assertEquals("id: $id", id, response.getInt(0))

            val expectedRoundId = roundId?.takeIf { it != 15 && id != 7 }
            if (expectedRoundId != null) {
                assertEquals("id: $id", expectedRoundId, response.getInt(1))
            }
            else {
                assertTrue("id: $id, ${response.getInt(1)}", response.isNull(1))
            }

            val expectedSubTypeId = subtypeId?.takeIf { it != 15 && id != 3 && id != 7 }
            if (expectedSubTypeId != null) {
                assertEquals("id: $id", expectedSubTypeId, response.getInt(2))
            }
            else {
                assertTrue("id: $id, ${response.getInt(2)}", response.isNull(2))
            }

            response.moveToNext()
        }
    }
}
