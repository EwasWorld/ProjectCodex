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
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.ScoresRoomDatabaseImpl
import eywa.projectcodex.database.UpdateType
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.model.FullShootInfo
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
            shootsRepo().insert(shootInfo.shoot, shootInfo.shootRound, shootInfo.shootDetail, true)
            shootInfo.arrows?.let { arrowScoresRepo().insert(*it.toTypedArray()) }
            shootInfo.arrowCounter?.let { arrowCounterRepo().insert(it) }
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
