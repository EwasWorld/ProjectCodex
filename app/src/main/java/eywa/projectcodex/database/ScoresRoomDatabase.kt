package eywa.projectcodex.database

import android.app.Activity
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.database.daos.*
import eywa.projectcodex.database.entities.*
import kotlinx.coroutines.CoroutineScope
import java.util.*

@Database(
        entities = [
            ArcherRound::class, Archer::class, ArrowValue::class,
            Round::class, RoundArrowCount::class, RoundSubType::class, RoundDistance::class
        ],
        version = 4,
        exportSchema = true
)
@TypeConverters(ScoresRoomDatabase.Converters::class)
abstract class ScoresRoomDatabase : RoomDatabase() {

    abstract fun archerDao(): ArcherDao
    abstract fun archerRoundDao(): ArcherRoundDao
    abstract fun arrowValueDao(): ArrowValueDao
    abstract fun roundDao(): RoundDao
    abstract fun roundArrowCountDao(): RoundArrowCountDao
    abstract fun roundSubTypeDao(): RoundSubTypeDao
    abstract fun roundDistanceDao(): RoundDistanceDao

    companion object {
        private const val MIGRATION_LOG_TAG = "DatabaseMigration"

        @VisibleForTesting
        var DATABASE_NAME = "scores_database"
        // Singleton prevents multiple instances of database opening at the same time.
        @Volatile
        private var INSTANCE: ScoresRoomDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val sqlStrings = mutableListOf<String>()
                /*
                 * Delete arrow_value_table (don't need the data from it)
                 * Create arrow_values to replace it
                 */
                sqlStrings.add("DROP TABLE `arrow_value_table`")
                sqlStrings.add(
                        """
                        CREATE TABLE `arrow_values` (
                            `archerRoundId` INTEGER NOT NULL, 
                            `arrowNumber` INTEGER NOT NULL, 
                            `score` INTEGER NOT NULL, 
                            `isX` INTEGER NOT NULL, 
                            CONSTRAINT PK_arrow_values PRIMARY KEY (archerRoundId, arrowNumber)
                        )"""
                )

                /*
                 * Create new tables
                 */
                sqlStrings.add(
                        """
                        CREATE TABLE `archers` (
                            `archerId` INTEGER NOT NULL, 
                            `name` TEXT NOT NULL, 
                            PRIMARY KEY(`archerId`)
                        )"""
                )
                sqlStrings.add(
                        """
                        CREATE TABLE `archer_rounds` (
                            `archerRoundId` INTEGER NOT NULL, 
                            `dateShot` INTEGER NOT NULL, 
                            `archerId` INTEGER NOT NULL, 
                            `bowId` INTEGER, 
                            `roundReferenceId` INTEGER, 
                            `roundDistanceId` INTEGER, 
                            `goalScore` INTEGER, 
                            `shootStatus` TEXT, 
                            `countsTowardsHandicap` INTEGER NOT NULL, 
                            PRIMARY KEY(`archerRoundId`)
                        )"""
                )
                sqlStrings.add(
                        """
                        CREATE TABLE `round_distances` (
                            `roundDistanceId` INTEGER NOT NULL, 
                            `roundReferenceId` INTEGER, 
                            `distanceInM` REAL, 
                            `faceSizeInCm` INTEGER, 
                            `arrowCount` INTEGER, 
                            PRIMARY KEY(`roundDistanceId`)
                        )"""
                )
                sqlStrings.add(
                        """
                        CREATE TABLE `rounds_references` (
                            `roundReferenceId` INTEGER NOT NULL, 
                            `type` TEXT NOT NULL, 
                            `length` TEXT, 
                            `scoringType` TEXT NOT NULL, 
                            `outdoor` INTEGER NOT NULL, 
                            `innerTenScoring` INTEGER NOT NULL, 
                            PRIMARY KEY(`roundReferenceId`)
                        )"""
                )

                executeMigrations(sqlStrings, database, startVersion, endVersion)
            }
        }

        @VisibleForTesting
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val sqlStrings = mutableListOf<String>()

                /*
                 * Delete old rounds tables
                 */
                sqlStrings.add("DROP TABLE `round_distances`")
                sqlStrings.add("DROP TABLE `rounds_references`")

                /*
                 * Create new rounds tables
                 */
                sqlStrings.add(
                        """
                        CREATE TABLE `rounds` (
                            `roundId` INTEGER NOT NULL, 
                            `name` TEXT NOT NULL, 
                            `isOutdoor` INTEGER NOT NULL, 
                            `isMetric` INTEGER NOT NULL, 
                            `fiveArrowEnd` INTEGER NOT NULL, 
                            `permittedFaces` TEXT NOT NULL, 
                            `isDefaultRound` INTEGER NOT NULL, 
                            PRIMARY KEY(`roundId`)
                        )"""
                )
                sqlStrings.add(
                        """
                        CREATE TABLE `round_arrow_counts` (
                            `roundId` INTEGER NOT NULL, 
                            `distanceNumber` INTEGER NOT NULL, 
                            `faceSizeInCm` INTEGER NOT NULL, 
                            `arrowCount` INTEGER NOT NULL, 
                            CONSTRAINT PK_round_arrow_counts PRIMARY KEY(roundId, distanceNumber)
                        )"""
                )
                sqlStrings.add(
                        """
                        CREATE TABLE `round_sub_types` (
                            `roundId` INTEGER NOT NULL, 
                            `subTypeId` INTEGER NOT NULL, 
                            `name` TEXT, 
                            `gents` INTEGER, 
                            `ladies` INTEGER, 
                            CONSTRAINT PK_round_sub_types PRIMARY KEY(roundId, subTypeId)
                        )"""
                )
                sqlStrings.add(
                        """
                        CREATE TABLE `round_sub_type_counts` (
                            `roundId` INTEGER NOT NULL, 
                            `distanceNumber` INTEGER NOT NULL, 
                            `subTypeId` INTEGER NOT NULL, 
                            `distance` INTEGER NOT NULL, 
                            CONSTRAINT PK_round_sub_type_counts PRIMARY KEY(roundId, distanceNumber, subTypeId)
                        )"""
                )

                executeMigrations(sqlStrings, database, startVersion, endVersion)
            }
        }

        @VisibleForTesting
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val sqlStrings = mutableListOf<String>()

                // All the other round tables were remade, so delete all the records in here to maintain consistency
                sqlStrings.add("DELETE FROM round_sub_types")

                /*
                 * round_sub_type_counts -> round_distances
                 */
                sqlStrings.add("DROP TABLE `round_sub_type_counts`")
                sqlStrings.add(
                        """
                        CREATE TABLE `round_distances` (
                            `roundId` INTEGER NOT NULL, 
                            `distanceNumber` INTEGER NOT NULL, 
                            `subTypeId` INTEGER NOT NULL, 
                            `distance` INTEGER NOT NULL, 
                            CONSTRAINT PK_round_sub_type_counts PRIMARY KEY(roundId, distanceNumber, subTypeId)
                        )"""
                )

                /*
                 * Change type of faceSizeInCm
                 */
                sqlStrings.add("DROP TABLE `round_arrow_counts`")
                sqlStrings.add(
                        """
                        CREATE TABLE `round_arrow_counts` (
                            `roundId` INTEGER NOT NULL, 
                            `distanceNumber` INTEGER NOT NULL, 
                            `faceSizeInCm` REAL NOT NULL, 
                            `arrowCount` INTEGER NOT NULL, 
                            CONSTRAINT PK_round_arrow_counts PRIMARY KEY(roundId, distanceNumber)
                        )"""
                )

                /*
                 * Add displayName column to Rounds
                 */
                sqlStrings.add("DROP TABLE `rounds`")
                sqlStrings.add(
                        """
                        CREATE TABLE `rounds` (
                            `roundId` INTEGER NOT NULL, 
                            `name` TEXT NOT NULL, 
                            `displayName` TEXT NOT NULL, 
                            `isOutdoor` INTEGER NOT NULL, 
                            `isMetric` INTEGER NOT NULL, 
                            `fiveArrowEnd` INTEGER NOT NULL, 
                            `permittedFaces` TEXT NOT NULL, 
                            `isDefaultRound` INTEGER NOT NULL, 
                            PRIMARY KEY(`roundId`)
                        )"""
                )

                /*
                 * Rename columns in ArcherRound
                 * roundReferenceId, roundDistanceId -> roundId, roundSubTypeId
                 */
                sqlStrings.add("ALTER TABLE archer_rounds RENAME TO archer_rounds_old")
                sqlStrings.add(
                        """
                        CREATE TABLE `archer_rounds` (
                            `archerRoundId` INTEGER NOT NULL, 
                            `dateShot` INTEGER NOT NULL, 
                            `archerId` INTEGER NOT NULL, 
                            `bowId` INTEGER, 
                            `roundId` INTEGER, 
                            `roundSubTypeId` INTEGER, 
                            `goalScore` INTEGER, 
                            `shootStatus` TEXT, 
                            `countsTowardsHandicap` INTEGER NOT NULL, 
                            PRIMARY KEY(`archerRoundId`)
                        )"""
                )
                sqlStrings.add(
                        """
                            INSERT INTO `archer_rounds` (`archerRoundId`, `dateShot`, `archerId`, `bowId`, `roundId`, 
                                                         `roundSubTypeId`, `goalScore`, `shootStatus`, 
                                                         `countsTowardsHandicap`)
                            SELECT `archerRoundId`, `dateShot`, `archerId`, `bowId`, `roundReferenceId`, 
                                   `roundDistanceId`, `goalScore`, `shootStatus`, `countsTowardsHandicap`
                            FROM archer_rounds_old;
                        """
                )
                sqlStrings.add("DROP TABLE `archer_rounds_old`")
                executeMigrations(sqlStrings, database, startVersion, endVersion)
            }
        }

        /**
         * Migration template from which others can be made
         */
        @VisibleForTesting
        val MIGRATION_BLANK = object : Migration(1, 1) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val sqlStrings = mutableListOf<String>()

                executeMigrations(sqlStrings, database, startVersion, endVersion)
            }
        }

        /**
         * Delete the database and clear the instance
         */
        @VisibleForTesting
        fun clearInstance(activity: Activity) {
            activity.applicationContext.deleteDatabase(DATABASE_NAME)
            INSTANCE = null
        }

        private fun executeMigrations(
                sqlStrings: List<String>, database: SupportSQLiteDatabase,
                startVersion: Int, endVersion: Int
        ) {
            CustomLogger.i(MIGRATION_LOG_TAG, "migrating from $startVersion to $endVersion")
            for (sqlStatement in sqlStrings) {
                database.execSQL(sqlStatement.trimIndent().replace("\\n", ""))
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): ScoresRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance =
                    Room.databaseBuilder(context.applicationContext, ScoresRoomDatabase::class.java, DATABASE_NAME)
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()
                INSTANCE = instance
                return instance
            }
        }
    }

    class Converters {
        @TypeConverter
        fun fromTimestamp(value: Long?): Date? {
            return value?.let { Date(it) }
        }

        @TypeConverter
        fun dateToTimestamp(date: Date?): Long? {
            return date?.time
        }

        @TypeConverter
        fun toStringList(value: String): List<String> {
            if (value.isEmpty()) return listOf()
            return value.split(':')
        }

        @TypeConverter
        fun toFlatString(value: List<String>): String {
            return value.joinToString(":")
        }
    }
}