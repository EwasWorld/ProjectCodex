package eywa.projectcodex.databaseTests

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import eywa.projectcodex.common.testDatabaseName
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.model.FullShootInfo

object DatabaseTestUtils {
    fun createDatabase(): ScoresRoomDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(testDatabaseName)
        return Room
                .inMemoryDatabaseBuilder(context, ScoresRoomDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }
}
