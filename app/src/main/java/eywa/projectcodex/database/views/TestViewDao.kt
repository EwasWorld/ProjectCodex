package eywa.projectcodex.database.views

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Helper class for testing views
 */
@Dao
interface TestViewDao {
    @Query("SELECT * FROM ${PersonalBest.TABLE_NAME}")
    fun getPbs(): Flow<List<PersonalBest>>

    @Query("SELECT * FROM ${ShootWithScore.TABLE_NAME}")
    fun getShootWithScores(): Flow<List<ShootWithScore>>
}
