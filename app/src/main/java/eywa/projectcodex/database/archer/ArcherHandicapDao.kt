package eywa.projectcodex.database.archer

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import kotlinx.coroutines.flow.Flow

@Dao
interface ArcherHandicapDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(archerHandicap: DatabaseArcherHandicap)

    /**
     * @return the latest entry for each [DatabaseArcherHandicap.handicapType]
     */
    @RewriteQueriesToDropUnusedColumns
    @Query(
            """
                SELECT *, MAX(dateSet)
                FROM ${DatabaseArcherHandicap.TABLE_NAME} 
                WHERE archerId = :archerId
                GROUP BY handicapType
            """
    )
    fun getLatestHandicaps(archerId: Int): Flow<List<DatabaseArcherHandicap>>

    @RewriteQueriesToDropUnusedColumns
    @Query(
            """
                SELECT *
                FROM ${DatabaseArcherHandicap.TABLE_NAME} 
                WHERE archerId = :archerId
            """
    )
    fun getAllHandicaps(archerId: Int): Flow<List<DatabaseArcherHandicap>>

    @Query("DELETE FROM ${DatabaseArcherHandicap.TABLE_NAME} WHERE archerHandicapId = :archerHandicapId")
    suspend fun delete(archerHandicapId: Int)
}
