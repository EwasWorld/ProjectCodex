package eywa.projectcodex.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import eywa.projectcodex.database.entities.RoundDistance

@Dao
interface RoundDistanceDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(roundDistance: RoundDistance)

    @Query("SELECT * FROM round_distances")
    fun getAllDistances(): LiveData<List<RoundDistance>>

    @Update
    fun update(vararg roundDistances: RoundDistance)

    @Query("DELETE FROM round_distances WHERE roundId = :roundId")
    suspend fun deleteAll(roundId: Int)

    @Query(
            """DELETE 
        FROM round_distances 
        WHERE roundId = :roundId AND distanceNumber = :distanceNumber AND subTypeId = :subTypeId"""
    )
    suspend fun delete(roundId: Int, distanceNumber: Int, subTypeId: Int)
}