package eywa.projectcodex.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import eywa.projectcodex.database.daos.ArrowValueDao
import eywa.projectcodex.database.entities.ArrowValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * TODO: In a real app, you should consider setting a directory for Room to use to export the schema so you can check the current schema into your version control system.
 * TODO: When you modify the database schema, you'll need to update the version number and define a migration strategy https://medium.com/androiddevelopers/understanding-migrations-with-room-f01e04b07929
 */
@Database(entities = [ArrowValue::class], version = 1, exportSchema = false)
abstract class ScoresRoomDatabase : RoomDatabase() {

    abstract fun arrowValueDao(): ArrowValueDao

    companion object {
        var dbName = "scores_database"
        // Singleton prevents multiple instances of database opening at the same time.
        @Volatile
        private var INSTANCE: ScoresRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ScoresRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance =
                    Room.databaseBuilder(context.applicationContext, ScoresRoomDatabase::class.java, dbName)
                            .addCallback(ScoresDatabaseCallback(scope)).build()
                INSTANCE = instance
                return instance
            }
        }
    }

    private class ScoresDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.arrowValueDao())
                }
            }
        }

        suspend fun populateDatabase(arrowValueDao: ArrowValueDao) {
            // TODO Don't delete everything in the database on launch
//            if (dbName.contains("test")) {
                arrowValueDao.deleteAll()
//            }
        }
    }
}