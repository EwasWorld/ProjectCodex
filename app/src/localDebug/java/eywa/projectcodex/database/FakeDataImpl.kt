package eywa.projectcodex.database

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.BuildConfig
import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTaskImpl
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsPreviewHelper
import eywa.projectcodex.components.sightMarks.SightMarksPreviewHelper
import eywa.projectcodex.database.archer.DEFAULT_ARCHER_ID
import eywa.projectcodex.database.rounds.*
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.hiltModules.FakeData
import eywa.projectcodex.hiltModules.FakeDataAnnotation
import eywa.projectcodex.hiltModules.LocalDatabaseModule.Companion.add
import kotlinx.coroutines.flow.first
import java.util.*


class FakeDataImpl(
        val context: Context,
        val datastore: CodexDatastore,
        val logging: CustomLogger,
) : FakeData {
    override suspend fun addFakeData(db: ScoresRoomDatabase) {
        check(BuildConfig.DEBUG) { "Should not be used in release builds" }

        if (db.shootsRepo().getFullShootInfo().first().isNotEmpty()) {
            Log.i(LOG_TAG, "Skipped adding fake data")
            return
        }
        Log.i(LOG_TAG, "Adding fake data")

        UpdateDefaultRoundsTaskImpl(db.roundsRepo(), context.resources, datastore, logging).runTask()

        ArcherHandicapsPreviewHelper.handicaps.forEach { db.archerRepo().insert(it) }
        SightMarksPreviewHelper.sightMarks.forEach { db.sightMarkRepo().insert(it) }

        val round1 = FullRoundInfo(
                round = Round(101, "metricround", "Metric Round", true, true),
                roundSubTypes = listOf(
                ),
                roundArrowCounts = listOf(
                        RoundArrowCount(101, 1, 122.0, 48),
                ),
                roundDistances = listOf(
                        RoundDistance(101, 1, 1, 70),
                ),
        )
        val round2 = FullRoundInfo(
                round = Round(102, "imperialround", "Imperial Round", true, false),
                roundSubTypes = listOf(
                        RoundSubType(102, 1, "Sub Type 1"),
                        RoundSubType(102, 2, "Sub Type 2"),
                ),
                roundArrowCounts = listOf(
                        RoundArrowCount(102, 1, 122.0, 36),
                ),
                roundDistances = listOf(
                        RoundDistance(102, 1, 1, 60),
                        RoundDistance(102, 1, 2, 50),
                ),
        )
        db.add(round1)
        db.add(round2)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString().drop(2)
        val firstOfThisYear = DateTimeFormat.SHORT_DATE_TIME.parse("1/1/$currentYear 10:00")
        val shoots = listOf(
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(
                            shootId = 1,
                            dateShot = firstOfThisYear,
                            archerId = DEFAULT_ARCHER_ID,
                    )
                    addFullSetOfArrows()
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(
                            shootId = 2,
                            dateShot = DateTimeFormat.SHORT_DATE.parse("2/2/12"),
                            archerId = DEFAULT_ARCHER_ID,
                    )
                    round = round1
                    completeRound(arrowScore = 8, isX = false)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(
                            shootId = 3,
                            dateShot = DateTimeFormat.SHORT_DATE.parse("3/3/11"),
                            archerId = DEFAULT_ARCHER_ID,
                    )
                    round = round2
                    addFullSetOfArrows()
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(
                            shootId = 4,
                            dateShot = DateTimeFormat.SHORT_DATE.parse("4/4/10"),
                            archerId = DEFAULT_ARCHER_ID,
                    )
                    round = round2
                    roundSubTypeId = 2
                    completeRound(arrowScore = 6, isX = false)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(
                            shootId = 5,
                            dateShot = DateTimeFormat.SHORT_DATE.parse("5/5/09"),
                            archerId = DEFAULT_ARCHER_ID,
                    )
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(
                            shootId = 6,
                            dateShot = DateTimeFormat.SHORT_DATE.parse("6/4/10"),
                            archerId = DEFAULT_ARCHER_ID,
                    )
                    round = round2
                    roundSubTypeId = 2
                    completeRound(arrowScore = 6, isX = false)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(
                            shootId = 7,
                            dateShot = DateTimeFormat.SHORT_DATE.parse("3/4/10"),
                            archerId = DEFAULT_ARCHER_ID,
                    )
                    round = round2
                    roundSubTypeId = 2
                    completeRound(arrowScore = 5, isX = false)
                },
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(
                            shootId = 8,
                            dateShot = DateTimeFormat.SHORT_DATE.parse("2/4/10"),
                            archerId = DEFAULT_ARCHER_ID,
                    )
                    round = round2
                    roundSubTypeId = 2
                    completeRound(arrowScore = 4, isX = false)
                },
        )
        shoots.forEach { db.add(it) }
    }

    companion object {
        const val LOG_TAG = "ScoresDatabase_FakeData"
    }
}

@Module
@InstallIn(SingletonComponent::class)
class FakeDataModule {
    @FakeDataAnnotation
    @Provides
    fun providesFakeData(
            @ApplicationContext context: Context,
            datastore: CodexDatastore,
            logging: CustomLogger,
    ): FakeData = FakeDataImpl(context, datastore, logging)
}
