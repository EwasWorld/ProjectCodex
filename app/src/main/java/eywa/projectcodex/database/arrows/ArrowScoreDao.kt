package eywa.projectcodex.database.arrows

import androidx.room.*
import eywa.projectcodex.database.arrows.DatabaseArrowScore.Companion.TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface ArrowScoreDao {
    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllArrows(): Flow<List<DatabaseArrowScore>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vararg arrowScore: DatabaseArrowScore)

    @Update
    suspend fun update(vararg arrowScores: DatabaseArrowScore)

    @Query("DELETE FROM $TABLE_NAME")
    suspend fun deleteAll()

    @Query(
            """
            DELETE FROM $TABLE_NAME 
            WHERE shootId = :shootId AND arrowNumber IN (:arrowNumbers)
            """
    )
    suspend fun deleteArrows(shootId: Int, arrowNumbers: List<Int>)
}
