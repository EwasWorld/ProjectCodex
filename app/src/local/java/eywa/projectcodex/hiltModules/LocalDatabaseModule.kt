package eywa.projectcodex.hiltModules

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.components.newScore.NewScoreType
import eywa.projectcodex.components.shootDetails.headToHead.addEnd.HeadToHeadAddEndExtras
import eywa.projectcodex.components.shootDetails.headToHead.addEnd.HeadToHeadAddEndState
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.ScoresRoomDatabaseImpl
import eywa.projectcodex.database.UpdateType
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.model.headToHead.FullHeadToHead
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Optional
import javax.inject.Singleton
import kotlin.jvm.optionals.getOrNull


@Module
@InstallIn(SingletonComponent::class)
class LocalDatabaseModule {
    companion object {
        var scoresRoomDatabase: ScoresRoomDatabaseImpl? = null
        private var headToHeadArrowScoreId: Int = 1

        fun createScoresRoomDatabase(context: Context, addFakeData: suspend () -> Unit) {
            scoresRoomDatabase = Room
                    .inMemoryDatabaseBuilder(context, ScoresRoomDatabaseImpl::class.java)
                    .allowMainThreadQueries()
                    .addCallback(
                            object : RoomDatabase.Callback() {
                                override fun onOpen(db: SupportSQLiteDatabase) {
                                    super.onOpen(db)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        @Suppress("ControlFlowWithEmptyBody")
                                        while (scoresRoomDatabase == null) {
                                        }
                                        scoresRoomDatabase!!.insertDefaults()
                                        addFakeData()
                                    }
                                }
                            },
                    )
                    .build()
        }

        fun teardown() {
            scoresRoomDatabase?.clearAllTables()
            scoresRoomDatabase = null
        }

        suspend fun ScoresRoomDatabase.add(shootInfo: FullShootInfo) {
            shootsRepo().insert(
                    shoot = shootInfo.shoot,
                    shootRound = shootInfo.shootRound,
                    shootDetail = shootInfo.shootDetail,
                    headToHead = null, // Added later
                    type = NewScoreType.SCORING,
            )
            shootInfo.arrows?.let { arrowScoresRepo().insert(*it.toTypedArray()) }
            shootInfo.arrowCounter?.let { arrowCounterRepo().insert(it) }
            shootInfo.h2h?.let { add(it) }
        }

        suspend fun ScoresRoomDatabase.add(roundInfo: FullRoundInfo) {
            roundsRepo().updateRounds(
                    listOfNotNull(
                            listOf(roundInfo.round),
                            roundInfo.roundSubTypes,
                            roundInfo.roundArrowCounts,
                            roundInfo.roundDistances,
                    ).flatten().associateWith { UpdateType.NEW }
            )
        }

        suspend fun ScoresRoomDatabase.add(h2hInfo: FullHeadToHead) {
            h2hRepo().insert(h2hInfo.headToHead)
            h2hInfo.matches.forEach { heat ->
                h2hRepo().insert(heat.match)

                heat.sets.flatMap {
                    HeadToHeadAddEndState(
                            extras = HeadToHeadAddEndExtras(set = it),
                            match = heat.match
                    ).setToDbDetails()
                }.forEach { h2hRepo().insert(it.copy(headToHeadArrowScoreId = headToHeadArrowScoreId++)) }
            }
        }
    }

    @Singleton
    @Provides
    fun providesRoomDatabase(
            @ApplicationContext context: Context,
            @FakeDataAnnotation fakeData: Optional<FakeData>,
    ): ScoresRoomDatabase {
        if (scoresRoomDatabase == null) {
            createScoresRoomDatabase(context) { fakeData.getOrNull()?.addFakeData(scoresRoomDatabase!!) }
        }
        return scoresRoomDatabase!!
    }
}
