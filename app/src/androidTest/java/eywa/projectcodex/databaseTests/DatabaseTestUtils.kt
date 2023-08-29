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

    suspend fun ScoresRoomDatabase.add(shootInfo: FullShootInfo) {
        shootDao().insert(shootInfo.shoot)
        if (shootInfo.arrows != null) {
            arrowScoreDao().insert(*shootInfo.arrows!!.toTypedArray())
        }
        if (shootInfo.shootRound != null) {
            shootRoundDao().insert(shootInfo.shootRound!!)
        }
        if (shootInfo.shootDetail != null) {
            shootDetailDao().insert(shootInfo.shootDetail!!)
        }
    }

    suspend fun ScoresRoomDatabase.add(roundInfo: FullRoundInfo) {
        roundDao().insert(roundInfo.round)
        roundInfo.roundSubTypes?.forEach { roundSubTypeDao().insert(it) }
        roundInfo.roundArrowCounts?.forEach { roundArrowCountDao().insert(it) }
        roundInfo.roundDistances?.forEach { roundDistanceDao().insert(it) }
    }
}
