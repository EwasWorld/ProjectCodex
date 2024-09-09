package eywa.projectcodex.databaseTests

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import eywa.projectcodex.common.testDatabaseName
import eywa.projectcodex.database.ScoresRoomDatabaseImpl

object DatabaseTestUtils {
    fun createDatabase(): ScoresRoomDatabaseImpl {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(testDatabaseName)
        return Room
                .inMemoryDatabaseBuilder(context, ScoresRoomDatabaseImpl::class.java)
                .allowMainThreadQueries()
                .build()
    }
}
