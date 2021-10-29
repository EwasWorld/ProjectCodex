package eywa.projectcodex.instrumentedTests.daggerObjects

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dagger.Module
import dagger.Provides
import eywa.projectcodex.components.app.App
import eywa.projectcodex.database.ScoresRoomDatabase
import javax.inject.Singleton


@Module
class DatabaseDaggerTestModule {
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