package eywa.projectcodex.database

import android.util.Log
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eywa.projectcodex.BuildConfig
import eywa.projectcodex.common.utils.asCalendar
import eywa.projectcodex.database.archer.Archer
import eywa.projectcodex.database.archer.ArcherDao
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.ArcherRoundDao
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.arrowValue.ArrowValueDao
import eywa.projectcodex.database.bow.BowDao
import eywa.projectcodex.database.bow.DatabaseBow
import eywa.projectcodex.database.rounds.*
import eywa.projectcodex.database.sightMarks.DatabaseSightMark
import eywa.projectcodex.database.sightMarks.SightMarkDao
import eywa.projectcodex.database.views.ArcherRoundWithScore
import eywa.projectcodex.database.views.PersonalBest
import eywa.projectcodex.model.Arrow
import kotlinx.coroutines.flow.first
import java.sql.Date
import java.util.*

@Database(
        entities = [
            ArcherRound::class, Archer::class, ArrowValue::class,
            Round::class, RoundArrowCount::class, RoundSubType::class, RoundDistance::class,
            DatabaseBow::class, DatabaseSightMark::class,
        ],
        views = [
            ArcherRoundWithScore::class, PersonalBest::class,
        ],
        version = 10,
        autoMigrations = [
            AutoMigration(from = 5, to = 6),
            AutoMigration(from = 6, to = 7),
            AutoMigration(from = 7, to = 8),
            AutoMigration(from = 8, to = 9, spec = DatabaseMigrations.Migration8To9::class),
            AutoMigration(from = 9, to = 10, spec = DatabaseMigrations.Migration9To10::class),
        ],
        exportSchema = true, // Needs a schema location in the build.gradle too to export!
)
@TypeConverters(DatabaseConverters::class)
abstract class ScoresRoomDatabase : RoomDatabase() {

    abstract fun archerDao(): ArcherDao
    abstract fun archerRoundDao(): ArcherRoundDao
    abstract fun arrowValueDao(): ArrowValueDao
    abstract fun roundDao(): RoundDao
    abstract fun roundArrowCountDao(): RoundArrowCountDao
    abstract fun roundSubTypeDao(): RoundSubTypeDao
    abstract fun roundDistanceDao(): RoundDistanceDao
    abstract fun sightMarkDao(): SightMarkDao
    abstract fun bowDao(): BowDao

    fun roundsRepo() = RoundRepo(
            roundDao(), roundArrowCountDao(), roundSubTypeDao(), roundDistanceDao()
    )

    suspend fun insertDefaults() {
        bowDao().insertDefaultBowIfNotExist()
    }

    private suspend fun addFakeData() {
        check(BuildConfig.DEBUG) { "Should not be used in release builds" }

        if (archerRoundDao().getAllFullArcherRoundInfo().first().isNotEmpty()) {
            Log.i(LOG_TAG, "Skipped adding fake data")
            return
        }
        Log.i(LOG_TAG, "Adding fake data")

        val arrowTypes = listOf(
                Arrow(0, false),
                Arrow(1, false),
                Arrow(2, false),
                Arrow(3, false),
                Arrow(4, false),
                Arrow(5, false),
                Arrow(6, false),
                Arrow(7, false),
                Arrow(8, false),
                Arrow(9, false),
                Arrow(10, false),
                Arrow(10, true),
        )

        listOf(
                Round(101, "metricround", "Metric Round", true, true),
                Round(102, "imperialround", "Imperial Round", true, true),
        ).forEach { roundDao().insert(it) }
        listOf(
                RoundSubType(102, 1, "Sub Type 1"),
                RoundSubType(102, 2, "Sub Type 2")
        ).forEach { roundSubTypeDao().insert(it) }
        listOf(
                RoundArrowCount(101, 1, 122.0, 48),
                RoundArrowCount(102, 1, 122.0, 36)
        ).forEach { roundArrowCountDao().insert(it) }
        listOf(
                RoundDistance(101, 1, 1, 70),
                RoundDistance(102, 1, 1, 60),
                RoundDistance(102, 1, 2, 50)
        ).forEach { roundDistanceDao().insert(it) }

        val firstOfThisYear =
                Date(Calendar.getInstance().get(Calendar.YEAR), Calendar.JANUARY, 1, 10, 0, 0).asCalendar()
        val archerRounds = listOf(
                ArcherRound(1, firstOfThisYear, 1),
                ArcherRound(2, Date.valueOf("2012-2-2").asCalendar(), 1, roundId = 101),
                ArcherRound(3, Date.valueOf("2011-3-3").asCalendar(), 1, roundId = 102),
                ArcherRound(4, Date.valueOf("2010-4-4").asCalendar(), 1, roundId = 102, roundSubTypeId = 2),
                ArcherRound(5, Date.valueOf("2009-5-5").asCalendar(), 1),
        )
        archerRounds.forEach { archerRoundDao().insert(it) }
        archerRounds.map { archerRound ->
            val archerRoundId = archerRound.archerRoundId
            List(1) { arrowNumber -> arrowTypes[archerRoundId].toArrowValue(archerRoundId, arrowNumber) }
        }.flatten().forEach { arrowValueDao().insert(it) }
    }

    companion object {
        const val DATABASE_NAME = "scores_database"
        const val LOG_TAG = "ScoresDatabase"
    }
}
