package eywa.projectcodex.database

import android.app.Application
import android.os.Build
import androidx.room.Room
import dagger.Module
import dagger.Provides

import javax.inject.Singleton


@Module
class DatabaseDaggerModule(application: Application) {
    // Keep all initialisation together in constructor
    @Suppress("JoinDeclarationAndAssignment")
    private val scoresRoomDatabase: ScoresRoomDatabase

    init {
        scoresRoomDatabase =
                Room.databaseBuilder(application, ScoresRoomDatabase::class.java, ScoresRoomDatabase.DATABASE_NAME)
                        .addMigrations(
                                DatabaseMigrations.MIGRATION_1_2,
                                DatabaseMigrations.MIGRATION_2_3,
                                DatabaseMigrations.MIGRATION_3_4
                        ).build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            /*
             * Write ahead mode suspected of causes issues with the instrumented test,
             * crashing suite runs with the error:
             *      SQLiteDiskIOException: disk I/O error (code 522 SQLITE_IOERR_SHORT_READ):
             *      , while compiling: PRAGMA journal_mode
             * A few sources point to turning of WAL, notably: https://github.com/Tencent/wcdb/issues/243
             * This also appears to have fixed some test failures
             */
            scoresRoomDatabase.openHelper.setWriteAheadLoggingEnabled(false)
        }
    }

    @Singleton
    @Provides
    fun providesRoomDatabase(): ScoresRoomDatabase {
        return scoresRoomDatabase
    }
}