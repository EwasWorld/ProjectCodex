package eywa.projectcodex.databaseTests.migrationTests

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import eywa.projectcodex.database.ScoresRoomDatabaseImpl
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
            ScoresRoomDatabaseImpl::class.java,
            listOf(),
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
    fun testMigrateSightMarks() {
        val oldValues = ContentValues().apply {
            put("id", 2)
            put("bowId", 3)
            put("distance", 4)
            put("isMetric", 0)
            put("dateSet", 5)
            put("sightMark", 6f)
            put("note", "Hi")
            put("isMarked", 1)
            put("isArchived", 0)
            put("useInPredictions", 1)
        }

        val db = createAndRunMigrations {
            insert("sight_marks", SQLiteDatabase.CONFLICT_ABORT, oldValues)
        }

        //language=RoomSql
        val response = db.query("SELECT * FROM sight_marks")
        assertEquals(1, response.count)
        response.moveToFirst()

        val newValues = mapOf(
                "sightMarkId" to 2,
                "bowId" to 3,
                "distance" to 4,
                "isMetric" to 0,
                "dateSet" to 5,
                "sightMark" to 6f,
                "note" to "Hi",
                "isMarked" to 1,
                "isArchived" to 0,
                "useInPredictions" to 1,
        )

        MigrationTestHelpers.checkValues(response, newValues)
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

        //language=RoomSql
        val response = db.query("SELECT * FROM bows")
        assertEquals(1, response.count)
        response.moveToFirst()

        val newValues = mapOf(
                "bowId" to 1,
                "isSightMarkDiagramHighestAtTop" to 0,
                "name" to "Default",
                "description" to null,
                "type" to 0, // Recurve
        )

        MigrationTestHelpers.checkValues(response, newValues)
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

        //language=RoomSql
        val response = db.query("SELECT * FROM archers")
        assertEquals(1, response.count)
        response.moveToFirst()

        val newValues = mapOf(
                "archerId" to 1,
                "name" to "Hello",
                "isGent" to 1,
                "age" to 1, // Senior
        )

        MigrationTestHelpers.checkValues(response, newValues)
    }

    @Test
    fun testMigrateArrows() {
        val oldValues1 = ContentValues().apply {
            put("archerRoundId", 1)
            put("arrowNumber", 1)
            put("score", 1)
            put("isX", 0)
        }
        val oldValues2 = ContentValues().apply {
            put("archerRoundId", 1)
            put("arrowNumber", 2)
            put("score", 10)
            put("isX", 1)
        }

        val db = createAndRunMigrations {
            insert("arrow_values", SQLiteDatabase.CONFLICT_ABORT, oldValues1)
            insert("arrow_values", SQLiteDatabase.CONFLICT_ABORT, oldValues2)
        }

        //language=RoomSql
        val response = db.query("SELECT * FROM arrow_scores ORDER BY arrowNumber")
        assertEquals(2, response.count)

        response.moveToFirst()
        MigrationTestHelpers.checkValues(
                response,
                mapOf(
                        "shootId" to 1,
                        "arrowNumber" to 1,
                        "score" to 1,
                        "isX" to 0,
                ),
        )

        response.moveToNext()
        MigrationTestHelpers.checkValues(
                response,
                mapOf(
                        "shootId" to 1,
                        "arrowNumber" to 2,
                        "score" to 10,
                        "isX" to 1,
                ),
        )
    }

    @Test
    fun testMigrateShootSubTables() {
        val archerRound1NoRound = ContentValues().apply {
            put("archerRoundId", 1)
            put("dateShot", 1)
            put("archerId", 1)
            put("countsTowardsHandicap", 1)
            put("joinWithPrevious", 0)
        }
        val archerRound2NoRoundWithFace = ContentValues().apply {
            putAll(archerRound1NoRound)

            put("archerRoundId", 2)
            put("faces", "1")
        }
        val archerRound3Round = ContentValues().apply {
            putAll(archerRound1NoRound)

            put("archerRoundId", 3)
            put("roundId", 1)
        }
        val archerRound4RoundAndSubtype = ContentValues().apply {
            putAll(archerRound1NoRound)

            put("archerRoundId", 4)
            put("roundId", 1)
            put("roundSubTypeId", 2)
        }
        val archerRound5RoundWithFace = ContentValues().apply {
            putAll(archerRound1NoRound)

            put("archerRoundId", 5)
            put("roundId", 2)
            put("faces", "0:1")
        }
        val archerRound6NoRoundWithMultipleFaces = ContentValues().apply {
            putAll(archerRound1NoRound)

            put("archerRoundId", 6)
            put("faces", "0:1")
        }

        val oldValuesRound1 = ContentValues().apply {
            put("roundId", 1)
            put("name", "test")
            put("displayName", "Test")
            put("isOutdoor", 1)
            put("isMetric", 1)
            put("fiveArrowEnd", 0)
            put("legacyName", "NULL")
            put("defaultRoundId", "NULL")
        }
        val oldValuesRound2 = ContentValues().apply {
            putAll(oldValuesRound1)
            put("roundId", 2)
        }
        val oldValuesSubtype1 = ContentValues().apply {
            put("roundId", 1)
            put("subTypeId", 1)
            put("name", "subtype 1")
            put("gents", "NULL")
            put("ladies", "NULL")
        }
        val oldValuesSubtype2 = ContentValues().apply {
            putAll(oldValuesSubtype1)

            put("subTypeId", 2)
            put("name", "subtype 2")
        }

        val db = createAndRunMigrations {
            insert("archer_rounds", SQLiteDatabase.CONFLICT_ABORT, archerRound1NoRound)
            insert("archer_rounds", SQLiteDatabase.CONFLICT_ABORT, archerRound2NoRoundWithFace)
            insert("archer_rounds", SQLiteDatabase.CONFLICT_ABORT, archerRound3Round)
            insert("archer_rounds", SQLiteDatabase.CONFLICT_ABORT, archerRound4RoundAndSubtype)
            insert("archer_rounds", SQLiteDatabase.CONFLICT_ABORT, archerRound5RoundWithFace)
            insert("archer_rounds", SQLiteDatabase.CONFLICT_ABORT, archerRound6NoRoundWithMultipleFaces)
            insert("rounds", SQLiteDatabase.CONFLICT_ABORT, oldValuesRound1)
            insert("rounds", SQLiteDatabase.CONFLICT_ABORT, oldValuesRound2)
            insert("round_sub_types", SQLiteDatabase.CONFLICT_ABORT, oldValuesSubtype1)
            insert("round_sub_types", SQLiteDatabase.CONFLICT_ABORT, oldValuesSubtype2)
        }

        //language=RoomSql
        var response = db.query("SELECT * FROM shoot_rounds ORDER BY shootId")
        assertEquals(3, response.count)

        response.moveToFirst()
        MigrationTestHelpers.checkValues(
                response,
                mapOf(
                        "shootId" to 3,
                        "roundId" to 1,
                        "roundSubTypeId" to null,
                        "faces" to null,
                        "sightersCount" to 0,
                ),
        )

        response.moveToNext()
        MigrationTestHelpers.checkValues(
                response,
                mapOf(
                        "shootId" to 4,
                        "roundId" to 1,
                        "roundSubTypeId" to 2,
                        "faces" to null,
                        "sightersCount" to 0,
                ),
        )

        response.moveToNext()
        MigrationTestHelpers.checkValues(
                response,
                mapOf(
                        "shootId" to 5,
                        "roundId" to 2,
                        "roundSubTypeId" to null,
                        "faces" to "0:1",
                        "sightersCount" to 0,
                ),
        )

        //language=RoomSql
        response = db.query("SELECT * FROM shoot_details ORDER BY shootId")
        assertEquals(2, response.count)

        response.moveToFirst()
        MigrationTestHelpers.checkValues(
                response,
                mapOf(
                        "shootId" to 2,
                        "face" to "1",
                        "distance" to null,
                        "isDistanceInMeters" to 1,
                        "faceSizeInCm" to null,
                ),
        )

        response.moveToNext()
        MigrationTestHelpers.checkValues(
                response,
                mapOf(
                        "shootId" to 6,
                        "face" to "0",
                        "distance" to null,
                        "isDistanceInMeters" to 1,
                        "faceSizeInCm" to null,
                ),
        )
    }

    @Test
    fun testMigrateShoots() {
        val archerRound1 = ContentValues().apply {
            put("archerRoundId", 8)
            put("dateShot", 2)
            put("archerId", 3)
            put("countsTowardsHandicap", 0)
            put("bowId", 4)
            put("roundId", 5)
            put("roundSubTypeId", 6)
            put("goalScore", 7)
            put("shootStatus", "Comp")
            put("faces", "0:1")
            put("joinWithPrevious", 1)
        }
        val archerRound2 = ContentValues().apply {
            put("archerRoundId", 2)
            put("dateShot", 2)
            put("archerId", 2)
            put("countsTowardsHandicap", 0)
            put("bowId", 2)
            put("roundId", 2)
            put("roundSubTypeId", 2)
            put("goalScore", 2)
            put("shootStatus", "Practice")
            put("faces", 2)
            put("joinWithPrevious", 0)
        }
        val archerRound3 = ContentValues().apply {
            put("archerRoundId", 3)
            put("dateShot", 3)
            put("archerId", 3)
            put("countsTowardsHandicap", 1)
            put("joinWithPrevious", 0)
        }

        val db = createAndRunMigrations {
            insert("archer_rounds", SQLiteDatabase.CONFLICT_ABORT, archerRound1)
            insert("archer_rounds", SQLiteDatabase.CONFLICT_ABORT, archerRound2)
            insert("archer_rounds", SQLiteDatabase.CONFLICT_ABORT, archerRound3)
        }

        //language=RoomSql
        val response = db.query("SELECT * FROM shoots ORDER BY shootId")
        assertEquals(3, response.count)

        response.moveToFirst()
        MigrationTestHelpers.checkValues(
                response,
                mapOf(
                        "shootId" to 2,
                        "dateShot" to 2,
                        "archerId" to 2,
                        "countsTowardsHandicap" to 0,
                        "bowId" to 2,
                        "goalScore" to 2,
                        "shootStatus" to "Practice",
                        "joinWithPrevious" to 0,
                ),
        )

        response.moveToNext()
        MigrationTestHelpers.checkValues(
                response,
                mapOf(
                        "shootId" to 3,
                        "dateShot" to 3,
                        "archerId" to 3,
                        "countsTowardsHandicap" to 1,
                        "bowId" to null,
                        "goalScore" to null,
                        "shootStatus" to null,
                        "joinWithPrevious" to 0,
                ),
        )

        response.moveToNext()
        MigrationTestHelpers.checkValues(
                response,
                mapOf(
                        "shootId" to 8,
                        "dateShot" to 2,
                        "archerId" to 3,
                        "countsTowardsHandicap" to 0,
                        "bowId" to 4,
                        "goalScore" to 7,
                        "shootStatus" to "Comp",
                        "joinWithPrevious" to 1,
                ),
        )
    }
}
