package eywa.projectcodex.database.arrows

import androidx.room.*
import eywa.projectcodex.database.arrows.DatabaseArrowCounter.Companion.TABLE_NAME

@Dao
interface ArrowCounterDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(arrowCounter: DatabaseArrowCounter)

    @Update
    suspend fun update(vararg arrowCounters: DatabaseArrowCounter)

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId")
    suspend fun delete(shootId: Int)
}
