package eywa.projectcodex.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eywa.projectcodex.database.archer.ArcherDao
import eywa.projectcodex.database.archer.ArcherHandicapDao
import eywa.projectcodex.database.archer.ArcherRepo
import eywa.projectcodex.database.archer.DatabaseArcher
import eywa.projectcodex.database.archer.DatabaseArcherHandicap
import eywa.projectcodex.database.arrows.ArrowCounterDao
import eywa.projectcodex.database.arrows.ArrowCounterRepo
import eywa.projectcodex.database.arrows.ArrowScoreDao
import eywa.projectcodex.database.arrows.ArrowScoresRepo
import eywa.projectcodex.database.arrows.DatabaseArrowCounter
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.bow.BowDao
import eywa.projectcodex.database.bow.BowRepo
import eywa.projectcodex.database.bow.DatabaseBow
import eywa.projectcodex.database.migrations.DatabaseMigrations
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundArrowCountDao
import eywa.projectcodex.database.rounds.RoundDao
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundDistanceDao
import eywa.projectcodex.database.rounds.RoundRepo
import eywa.projectcodex.database.rounds.RoundSubType
import eywa.projectcodex.database.rounds.RoundSubTypeDao
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.database.shootData.DatabaseShootDetail
import eywa.projectcodex.database.shootData.DatabaseShootRound
import eywa.projectcodex.database.shootData.ShootDao
import eywa.projectcodex.database.shootData.ShootDetailDao
import eywa.projectcodex.database.shootData.ShootRoundDao
import eywa.projectcodex.database.shootData.ShootsRepo
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHead
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadDetail
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadMatch
import eywa.projectcodex.database.shootData.headToHead.HeadToHeadDao
import eywa.projectcodex.database.shootData.headToHead.HeadToHeadDetailDao
import eywa.projectcodex.database.shootData.headToHead.HeadToHeadMatchDao
import eywa.projectcodex.database.shootData.headToHead.HeadToHeadRepo
import eywa.projectcodex.database.sightMarks.DatabaseSightMark
import eywa.projectcodex.database.sightMarks.SightMarkDao
import eywa.projectcodex.database.sightMarks.SightMarkRepo
import eywa.projectcodex.database.views.PersonalBest
import eywa.projectcodex.database.views.ShootWithScore
import eywa.projectcodex.database.views.TestViewDao

interface ScoresRoomDatabase {
    fun clearAllData()
    fun closeDb()
    fun checkpoint()

    suspend fun insertDefaults()

    fun h2hRepo(): HeadToHeadRepo
    fun roundsRepo(): RoundRepo
    fun shootsRepo(): ShootsRepo
    fun arrowScoresRepo(): ArrowScoresRepo
    fun archerRepo(): ArcherRepo
    fun bowRepo(): BowRepo
    fun arrowCounterRepo(): ArrowCounterRepo
    fun sightMarkRepo(): SightMarkRepo
}

@Database(
        entities = [
            DatabaseShoot::class, DatabaseArcher::class, DatabaseArrowScore::class,
            Round::class, RoundArrowCount::class, RoundSubType::class, RoundDistance::class,
            DatabaseBow::class, DatabaseSightMark::class,
            DatabaseShootRound::class, DatabaseShootDetail::class, DatabaseArrowCounter::class,
            DatabaseArcherHandicap::class, DatabaseHeadToHead::class, DatabaseHeadToHeadMatch::class,
            DatabaseHeadToHeadDetail::class,
        ],
        views = [
            ShootWithScore::class, PersonalBest::class,
        ],
        version = 14,
        autoMigrations = [
            AutoMigration(from = 5, to = 6),
            AutoMigration(from = 6, to = 7),
            AutoMigration(from = 7, to = 8),
            AutoMigration(from = 8, to = 9, spec = DatabaseMigrations.Migration8To9::class),
            AutoMigration(from = 9, to = 10, spec = DatabaseMigrations.Migration9To10::class),
            AutoMigration(from = 11, to = 12),
            AutoMigration(from = 12, to = 13),
            AutoMigration(from = 12, to = 14),
        ],
        exportSchema = true, // Needs a schema location in the build.gradle too to export!
)
@TypeConverters(DatabaseConverters::class)
abstract class ScoresRoomDatabaseImpl : RoomDatabase(), ScoresRoomDatabase {

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
    abstract fun headToHeadDao(): HeadToHeadDao
    abstract fun headToHeadHeatDao(): HeadToHeadMatchDao
    abstract fun headToHeadDetailDao(): HeadToHeadDetailDao
    abstract fun testViewDao(): TestViewDao

    override fun h2hRepo() = HeadToHeadRepo(headToHeadDao(), headToHeadHeatDao(), headToHeadDetailDao())
    override fun roundsRepo() = RoundRepo(roundDao(), roundArrowCountDao(), roundSubTypeDao(), roundDistanceDao())
    override fun shootsRepo() =
            ShootsRepo(shootDao(), shootDetailDao(), shootRoundDao(), arrowCounterRepo(), h2hRepo())

    override fun arrowScoresRepo() = ArrowScoresRepo(arrowScoreDao())
    override fun archerRepo() = ArcherRepo(archerDao(), archerHandicapDao())
    override fun bowRepo() = BowRepo(bowDao())
    override fun arrowCounterRepo() = ArrowCounterRepo(arrowCounterDao())
    override fun sightMarkRepo() = SightMarkRepo(sightMarkDao())

    override suspend fun insertDefaults() {
        bowRepo().insertDefaultBowIfNotExist()
        archerRepo().insertDefaultArcherIfNotExist()
    }

    override fun clearAllData() = clearAllTables()

    override fun closeDb() = close()

    /**
     * Note from stack overflow answer: the checkpoint function doesn't appear to actually checkpoint
     * (closing the Database, which would checkpoint the database, results in issues around the Room framework/wrapper).
     *
     * Not sure why they include it if it doesn't actually checkpoint the db
     */
    override fun checkpoint() {
        openHelper.writableDatabase.apply {
            query("PRAGMA wal_checkpoint(FULL);", null)
            query("PRAGMA wal_checkpoint(TRUNCATE);", null)
        }
    }

    companion object {
        const val DATABASE_NAME = "scores_database"
        const val LOG_TAG = "ScoresDatabase"
    }
}
