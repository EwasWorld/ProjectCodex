package eywa.projectcodex.hiltModules

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.database.DatabaseMigrations
import eywa.projectcodex.database.ScoresRoomDatabase

import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class DatabaseDaggerModule {
    @Singleton
    @Provides
    fun providesRoomDatabase(@ApplicationContext context: Context): ScoresRoomDatabase {
        val scoresRoomDatabase =
                Room.databaseBuilder(context, ScoresRoomDatabase::class.java, ScoresRoomDatabase.DATABASE_NAME)
                        .addMigrations(
                                DatabaseMigrations.MIGRATION_1_2,
                                DatabaseMigrations.MIGRATION_2_3,
                                DatabaseMigrations.MIGRATION_3_4,
                                DatabaseMigrations.MIGRATION_4_5,
                        ).build()
        /*
         * Write ahead mode suspected of causes issues with the instrumented test,
         * crashing suite runs with the error:
         *      SQLiteDiskIOException: disk I/O error (code 522 SQLITE_IOERR_SHORT_READ):
         *      , while compiling: PRAGMA journal_mode
         * A few sources point to turning of WAL, notably: https://github.com/Tencent/wcdb/issues/243
         * This also appears to have fixed some test failures
         */
        scoresRoomDatabase.openHelper.setWriteAheadLoggingEnabled(false)
        return scoresRoomDatabase
    }
}
