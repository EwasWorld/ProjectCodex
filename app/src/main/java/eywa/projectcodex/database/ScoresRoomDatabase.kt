package eywa.projectcodex.database

import androidx.room.*
import eywa.projectcodex.database.archer.Archer
import eywa.projectcodex.database.archer.ArcherDao
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.ArcherRoundDao
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.arrowValue.ArrowValueDao
import eywa.projectcodex.database.rounds.*
import eywa.projectcodex.database.views.ArcherRoundWithScore
import eywa.projectcodex.database.views.PersonalBest
import java.util.*

@Database(
        entities = [
            ArcherRound::class, Archer::class, ArrowValue::class,
            Round::class, RoundArrowCount::class, RoundSubType::class, RoundDistance::class
        ],
        views = [
            ArcherRoundWithScore::class, PersonalBest::class,
        ],
        version = 7,
        autoMigrations = [
            AutoMigration(from = 5, to = 6),
            AutoMigration(from = 6, to = 7)
        ],
        exportSchema = true, // Needs a schema location in the build.gradle too to export!
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

    fun roundsRepo() = RoundRepo(
            roundDao(), roundArrowCountDao(), roundSubTypeDao(), roundDistanceDao()
    )

    companion object {
        const val DATABASE_NAME = "scores_database"
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
