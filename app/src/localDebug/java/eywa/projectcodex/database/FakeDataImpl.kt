package eywa.projectcodex.database

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.BuildConfig
import eywa.projectcodex.common.utils.asCalendar
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsPreviewHelper
import eywa.projectcodex.components.sightMarks.SightMarksPreviewHelper
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.database.shootData.DatabaseShootRound
import eywa.projectcodex.hiltModules.FakeData
import eywa.projectcodex.hiltModules.FakeDataAnnotation
import eywa.projectcodex.model.Arrow
import kotlinx.coroutines.flow.first
import java.sql.Date
import java.util.*


class FakeDataImpl : FakeData {
    override suspend fun addFakeData(db: ScoresRoomDatabase) {
        check(BuildConfig.DEBUG) { "Should not be used in release builds" }

        if (db.shootDao().getAllFullShootInfo().first().isNotEmpty()) {
            Log.i(ScoresRoomDatabase.LOG_TAG, "Skipped adding fake data")
            return
        }
        Log.i(ScoresRoomDatabase.LOG_TAG, "Adding fake data")

        ArcherHandicapsPreviewHelper.handicaps.forEach { db.archerHandicapDao().insert(it) }
        SightMarksPreviewHelper.sightMarks.forEach { db.sightMarkDao().insert(it) }

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
        ).forEach { db.roundDao().insert(it) }
        listOf(
                RoundSubType(102, 1, "Sub Type 1"),
                RoundSubType(102, 2, "Sub Type 2")
        ).forEach { db.roundSubTypeDao().insert(it) }
        listOf(
                RoundArrowCount(101, 1, 122.0, 48),
                RoundArrowCount(102, 1, 122.0, 36)
        ).forEach { db.roundArrowCountDao().insert(it) }
        listOf(
                RoundDistance(101, 1, 1, 70),
                RoundDistance(102, 1, 1, 60),
                RoundDistance(102, 1, 2, 50)
        ).forEach { db.roundDistanceDao().insert(it) }

        val firstOfThisYear =
                Date(Calendar.getInstance().get(Calendar.YEAR), Calendar.JANUARY, 1, 10, 0, 0).asCalendar()
        val shoots = listOf(
                DatabaseShoot(1, firstOfThisYear, 1),
                DatabaseShoot(2, Date.valueOf("2012-2-2").asCalendar(), 1),
                DatabaseShoot(3, Date.valueOf("2011-3-3").asCalendar(), 1),
                DatabaseShoot(4, Date.valueOf("2010-4-4").asCalendar(), 1),
                DatabaseShoot(5, Date.valueOf("2009-5-5").asCalendar(), 1),
        )
        shoots.forEach { db.shootDao().insert(it) }
        shoots.map { shoot ->
            val shootId = shoot.shootId
            List(12) { arrowTypes[shootId].toArrowScore(shootId, it + 1) }
        }.flatten().forEach { db.arrowScoreDao().insert(it) }

        listOf(
                DatabaseShootRound(2, roundId = 101),
                DatabaseShootRound(3, roundId = 102),
                DatabaseShootRound(4, roundId = 102, roundSubTypeId = 2),
        ).forEach { db.shootRoundDao().insert(it) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
class FakeDataModule {
    @FakeDataAnnotation
    @Provides
    fun providesFakeData(): FakeData = FakeDataImpl()
}
