package eywa.projectcodex.databaseTests

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import eywa.projectcodex.common.testDatabaseName
import eywa.projectcodex.database.ScoresRoomDatabase

object DatabaseTestUtils {
    /**
     * Breaks in some kind of 'get thread for transaction' type message. Probably something to do with tests running
     * queries in the main thread?
     */
    const val brokenTransactionMessage = "Transactions can't be tested right now for some reason"

    fun createDatabase(): ScoresRoomDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(testDatabaseName)
        return Room
                .inMemoryDatabaseBuilder(context, ScoresRoomDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }
}
