package eywa.projectcodex.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import eywa.projectcodex.database.entities.ROUND_ARROW_COUNTS_TABLE_NAME
import eywa.projectcodex.database.entities.RoundArrowCount

@Dao
interface RoundArrowCountDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(roundArrowCount: RoundArrowCount)

    @Query("SELECT * FROM $ROUND_ARROW_COUNTS_TABLE_NAME")
    fun getAllArrowCounts(): LiveData<List<RoundArrowCount>>

    @Query("SELECT * FROM $ROUND_ARROW_COUNTS_TABLE_NAME WHERE roundId = :roundId")
    fun getArrowCountsForRound(roundId: Int): LiveData<List<RoundArrowCount>>

    @Update
    fun update(vararg roundArrowCounts: RoundArrowCount)

    @Query("DELETE FROM $ROUND_ARROW_COUNTS_TABLE_NAME WHERE roundId = :roundId")
    suspend fun deleteAll(roundId: Int)

    @Query("DELETE FROM $ROUND_ARROW_COUNTS_TABLE_NAME WHERE roundId = :roundId AND distanceNumber = :distanceNumber")
    suspend fun delete(roundId: Int, distanceNumber: Int)
}