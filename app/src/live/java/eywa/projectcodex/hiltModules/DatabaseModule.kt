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
import eywa.projectcodex.database.migrations.DatabaseMigrations
import eywa.projectcodex.database.migrations.MIGRATION_10_11
import eywa.projectcodex.database.migrations.MIGRATION_13_14
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Singleton
    @Provides
    fun providesRoomDatabase(@ApplicationContext context: Context): ScoresRoomDatabase {
        var scoresRoomDatabase: ScoresRoomDatabaseImpl? = null
        scoresRoomDatabase = Room
                .databaseBuilder(
                        context,
                        ScoresRoomDatabaseImpl::class.java,
                        ScoresRoomDatabaseImpl.DATABASE_NAME,
                )
                .addMigrations(
                        DatabaseMigrations.MIGRATION_1_2,
                        DatabaseMigrations.MIGRATION_2_3,
                        DatabaseMigrations.MIGRATION_3_4,
                        DatabaseMigrations.MIGRATION_4_5,
                        MIGRATION_10_11,
                        MIGRATION_13_14,
                )
                .addCallback(
                        object : RoomDatabase.Callback() {
                            override fun onOpen(db: SupportSQLiteDatabase) {
                                super.onOpen(db)
                                CoroutineScope(Dispatchers.IO).launch {
                                    scoresRoomDatabase!!.insertDefaults()
                                }
                            }
                        },
                )
                .build()

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
