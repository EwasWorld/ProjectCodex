package eywa.projectcodex.database.rounds

import androidx.room.*
import eywa.projectcodex.database.rounds.RoundArrowCount.Companion.TABLE_NAME

@Dao
interface RoundArrowCountDao : RoundTypeDao<RoundArrowCount> {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    override suspend fun insert(insertItem: RoundArrowCount)

    @Update
    override fun updateSingle(updateItem: RoundArrowCount)

    @Update
    fun update(vararg updateItems: RoundArrowCount)

    @Query("DELETE FROM $TABLE_NAME WHERE roundId = :roundId")
    suspend fun deleteAll(roundId: Int)

    @Query("DELETE FROM $TABLE_NAME WHERE roundId = :roundId AND distanceNumber = :distanceNumber")
    suspend fun delete(roundId: Int, distanceNumber: Int)

    @Delete
    override suspend fun deleteSingle(deleteItem: RoundArrowCount)
}
