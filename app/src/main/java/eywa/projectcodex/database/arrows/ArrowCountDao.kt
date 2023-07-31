package eywa.projectcodex.database.arrows

import androidx.room.*
import eywa.projectcodex.database.arrows.DatabaseArrowCount.Companion.TABLE_NAME

@Dao
interface ArrowCountDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(arrowCount: DatabaseArrowCount)

    @Update
    suspend fun update(vararg arrowCounts: DatabaseArrowCount)

    @Query("DELETE FROM $TABLE_NAME WHERE archerRoundId = :archerRoundId")
    suspend fun delete(archerRoundId: Int)
}
