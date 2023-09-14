package eywa.projectcodex.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eywa.projectcodex.database.archer.*
import eywa.projectcodex.database.arrows.*
import eywa.projectcodex.database.bow.BowDao
import eywa.projectcodex.database.bow.BowRepo
import eywa.projectcodex.database.bow.DatabaseBow
import eywa.projectcodex.database.rounds.*
import eywa.projectcodex.database.shootData.*
import eywa.projectcodex.database.sightMarks.DatabaseSightMark
import eywa.projectcodex.database.sightMarks.SightMarkDao
import eywa.projectcodex.database.views.PersonalBest
import eywa.projectcodex.database.views.ShootWithScore
import eywa.projectcodex.database.views.TestViewDao

@Database(
        entities = [
            DatabaseShoot::class, DatabaseArcher::class, DatabaseArrowScore::class,
            Round::class, RoundArrowCount::class, RoundSubType::class, RoundDistance::class,
            DatabaseBow::class, DatabaseSightMark::class,
            DatabaseShootRound::class, DatabaseShootDetail::class, DatabaseArrowCounter::class,
            DatabaseArcherHandicap::class,
        ],
        views = [
            ShootWithScore::class, PersonalBest::class,
        ],
        version = 11,
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
    abstract fun archerHandicapDao(): ArcherHandicapDao
    abstract fun shootDao(): ShootDao
    abstract fun arrowScoreDao(): ArrowScoreDao
    abstract fun roundDao(): RoundDao
    abstract fun roundArrowCountDao(): RoundArrowCountDao
    abstract fun roundSubTypeDao(): RoundSubTypeDao
    abstract fun roundDistanceDao(): RoundDistanceDao
    abstract fun sightMarkDao(): SightMarkDao
    abstract fun bowDao(): BowDao
    abstract fun shootDetailDao(): ShootDetailDao
    abstract fun shootRoundDao(): ShootRoundDao
    abstract fun arrowCounterDao(): ArrowCounterDao
    abstract fun testViewDao(): TestViewDao

    fun roundsRepo() = RoundRepo(roundDao(), roundArrowCountDao(), roundSubTypeDao(), roundDistanceDao())
    fun shootsRepo() = ShootsRepo(shootDao(), shootDetailDao(), shootRoundDao())
    fun arrowScoresRepo() = ArrowScoresRepo(arrowScoreDao())
    fun archerRepo() = ArcherRepo(archerDao(), archerHandicapDao())
    fun bowRepo() = BowRepo(bowDao())

    suspend fun insertDefaults() {
        bowRepo().insertDefaultBowIfNotExist()
        archerRepo().insertDefaultArcherIfNotExist()
    }

    companion object {
        const val DATABASE_NAME = "scores_database"
        const val LOG_TAG = "ScoresDatabase"
    }
}
