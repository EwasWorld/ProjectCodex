package eywa.projectcodex.database.archer

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArcherHandicapDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(archerHandicap: DatabaseArcherHandicap)

    // TODO_CURRENT test
    @Query(
            """
                SELECT *, MAX(dateSet)
                FROM ${DatabaseArcherHandicap.TABLE_NAME} 
                WHERE archerId = $DEFAULT_ARCHER_ID
                GROUP BY handicapType
            """
    )
    fun getLatestHandicapsForDefaultArcher(): Flow<List<DatabaseArcherHandicap>>
}
