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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class LocalDatabaseModule {
    companion object {
        var scoresRoomDatabase: ScoresRoomDatabase? = null

        fun createScoresRoomDatabase(context: Context) {
            scoresRoomDatabase = Room
                    .inMemoryDatabaseBuilder(context, ScoresRoomDatabase::class.java)
                    .allowMainThreadQueries()
                    .addCallback(
                            object : RoomDatabase.Callback() {
                                override fun onOpen(db: SupportSQLiteDatabase) {
                                    super.onOpen(db)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        scoresRoomDatabase!!.insertDefaults()
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
    }

    @Singleton
    @Provides
    fun providesRoomDatabase(
            @ApplicationContext context: Context
    ): ScoresRoomDatabase {
        if (scoresRoomDatabase == null) {
            createScoresRoomDatabase(context)
        }
        return scoresRoomDatabase!!
    }
}
