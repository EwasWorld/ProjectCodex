package eywa.projectcodex.database.rounds

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RoundDistanceDao : RoundTypeDao<RoundDistance> {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    override suspend fun insert(insertItem: RoundDistance)

    @Query("SELECT * FROM $ROUND_DISTANCES_TABLE_NAME")
    fun getAllDistances(): LiveData<List<RoundDistance>>

    @Query("SELECT * FROM $ROUND_DISTANCES_TABLE_NAME WHERE roundId = :roundId AND subTypeId = :subTypeId")
    fun getDistancesForRound(roundId: Int, subTypeId: Int?): LiveData<List<RoundDistance>>

    @Update
    override fun updateSingle(updateItem: RoundDistance)

    @Update
    fun update(vararg roundDistances: RoundDistance)

    @Query("DELETE FROM $ROUND_DISTANCES_TABLE_NAME WHERE roundId = :roundId")
    suspend fun deleteAll(roundId: Int)

    @Query(
            """DELETE 
        FROM $ROUND_DISTANCES_TABLE_NAME 
        WHERE roundId = :roundId AND distanceNumber = :distanceNumber AND subTypeId = :subTypeId"""
    )
    suspend fun delete(roundId: Int, distanceNumber: Int, subTypeId: Int)
}