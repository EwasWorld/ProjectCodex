package eywa.projectcodex.hiltModules

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.database.ScoresRoomDatabase
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class LocalDatabaseDaggerModule {
    companion object {
        var scoresRoomDatabase: ScoresRoomDatabase? = null

        fun createScoresRoomDatabase(context: Context) {
            scoresRoomDatabase = Room
                    .inMemoryDatabaseBuilder(context, ScoresRoomDatabase::class.java)
                    .allowMainThreadQueries()
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
