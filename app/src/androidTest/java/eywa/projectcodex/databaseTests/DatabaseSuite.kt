package eywa.projectcodex.databaseTests

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.testDatabaseName
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        GeneralDatabaseTests::class, ArcherRoundsTest::class, ArrowValueTest::class
)
class DatabaseSuite {
    companion object {
        fun createDatabase(): ScoresRoomDatabase {
            val context = ApplicationProvider.getApplicationContext<Context>()
            context.deleteDatabase(testDatabaseName)
            return Room.inMemoryDatabaseBuilder(context, ScoresRoomDatabase::class.java).allowMainThreadQueries()
                    .build()
        }
    }
}