package eywa.projectcodex.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.components.app.App
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class LocalDatabaseDaggerModule {
    companion object {
        val scoresRoomDatabase by lazy {
            Room.inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext<App>(),
                    ScoresRoomDatabase::class.java
            )
                    .allowMainThreadQueries()
                    .build()
        }

        fun teardown() {
            scoresRoomDatabase.clearAllTables()
        }
    }

    @Singleton
    @Provides
    fun providesRoomDatabase(): ScoresRoomDatabase {
        return scoresRoomDatabase
    }
}