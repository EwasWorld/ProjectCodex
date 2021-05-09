package eywa.projectcodex.database.rounds

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RoundArrowCountDao : RoundTypeDao<RoundArrowCount> {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    override suspend fun insert(insertItem: RoundArrowCount)

    @Query("SELECT * FROM $ROUND_ARROW_COUNTS_TABLE_NAME")
    fun getAllArrowCounts(): LiveData<List<RoundArrowCount>>

    @Query("SELECT * FROM $ROUND_ARROW_COUNTS_TABLE_NAME WHERE roundId = :roundId")
    fun getArrowCountsForRound(roundId: Int): LiveData<List<RoundArrowCount>>

    @Update
    override fun updateSingle(updateItem: RoundArrowCount)

    @Update
    fun update(vararg updateItems: RoundArrowCount)

    @Query("DELETE FROM $ROUND_ARROW_COUNTS_TABLE_NAME WHERE roundId = :roundId")
    suspend fun deleteAll(roundId: Int)

    @Query("DELETE FROM $ROUND_ARROW_COUNTS_TABLE_NAME WHERE roundId = :roundId AND distanceNumber = :distanceNumber")
    suspend fun delete(roundId: Int, distanceNumber: Int)
}