package eywa.projectcodex.database

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import eywa.projectcodex.database.daos.*
import eywa.projectcodex.database.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

@Database(
        entities = [
            ArcherRound::class, Archer::class, ArrowValue::class,
            Round::class, RoundArrowCount::class, RoundSubType::class, RoundSubTypeCount::class
        ],
        version = 3,
        exportSchema = true
)
@TypeConverters(ScoresRoomDatabase.Converters::class)
abstract class ScoresRoomDatabase : RoomDatabase() {

    abstract fun archerDao(): ArcherDao
    abstract fun archerRoundDao(): ArcherRoundDao
    abstract fun arrowValueDao(): ArrowValueDao
    abstract fun roundDistanceDao(): RoundSubTypeDao
    abstract fun roundReferenceDao(): RoundDao

    companion object {
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

                executeMigrations(sqlStrings, database)
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

                executeMigrations(sqlStrings, database)
            }
        }

        private fun executeMigrations(sqlStrings: List<String>, database: SupportSQLiteDatabase) {
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
                            .addCallback(ScoresDatabaseCallback(scope)).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                return instance
            }
        }
    }

    private class ScoresDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    prepareDatabase(database.arrowValueDao())
                }
            }
        }

        suspend fun prepareDatabase(arrowValueDao: ArrowValueDao) {
//            arrowValueDao.deleteAll()
            // TODO Add default round types
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
            return value.split(':')
        }

        @TypeConverter
        fun toFlatString(value: List<String>): String {
            return value.joinToString(":")
        }
    }
}