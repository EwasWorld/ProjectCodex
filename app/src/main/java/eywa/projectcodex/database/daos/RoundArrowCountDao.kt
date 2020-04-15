package eywa.projectcodex.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import eywa.projectcodex.database.entities.RoundArrowCount

@Dao
interface RoundArrowCountDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(roundArrowCount: RoundArrowCount)

    @Query("SELECT * FROM round_arrow_counts")
    fun getAllArrowCounts(): LiveData<List<RoundArrowCount>>

    @Update
    fun update(vararg roundArrowCounts: RoundArrowCount)

    @Query("DELETE FROM round_arrow_counts WHERE roundId = :roundId")
    suspend fun deleteAll(roundId: Int)

    @Query("DELETE FROM round_arrow_counts WHERE roundId = :roundId AND distanceNumber = :distanceNumber")
    suspend fun delete(roundId: Int, distanceNumber: Int)
}