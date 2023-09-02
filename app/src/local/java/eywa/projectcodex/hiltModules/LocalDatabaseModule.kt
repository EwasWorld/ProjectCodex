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
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.model.FullShootInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Singleton
import kotlin.jvm.optionals.getOrNull


@Module
@InstallIn(SingletonComponent::class)
class LocalDatabaseModule {
    companion object {
        var scoresRoomDatabase: ScoresRoomDatabase? = null

        fun createScoresRoomDatabase(context: Context, addFakeData: suspend () -> Unit) {
            scoresRoomDatabase = Room
                    .inMemoryDatabaseBuilder(context, ScoresRoomDatabase::class.java)
                    .allowMainThreadQueries()
                    .addCallback(
                            object : RoomDatabase.Callback() {
                                override fun onOpen(db: SupportSQLiteDatabase) {
                                    super.onOpen(db)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        scoresRoomDatabase!!.insertDefaults()
                                        addFakeData()
                                    }
                                }
                            }
                    )
                    .build()
        }

        fun teardown() {
            scoresRoomDatabase?.clearAllTables()
            scoresRoomDatabase = null
        }

        suspend fun ScoresRoomDatabase.add(shootInfo: FullShootInfo) {
            shootDao().insert(shootInfo.shoot)
            shootInfo.arrows?.let { arrowScoreDao().insert(*it.toTypedArray()) }
            shootInfo.shootRound?.let { shootRoundDao().insert(it) }
            shootInfo.shootDetail?.let { shootDetailDao().insert(it) }
        }

        suspend fun ScoresRoomDatabase.add(roundInfo: FullRoundInfo) {
            roundDao().insert(roundInfo.round)
            roundInfo.roundSubTypes?.forEach { roundSubTypeDao().insert(it) }
            roundInfo.roundArrowCounts?.forEach { roundArrowCountDao().insert(it) }
            roundInfo.roundDistances?.forEach { roundDistanceDao().insert(it) }
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
