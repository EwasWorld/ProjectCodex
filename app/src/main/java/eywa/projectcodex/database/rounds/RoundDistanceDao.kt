package eywa.projectcodex.database.rounds

import androidx.room.*
import eywa.projectcodex.database.rounds.RoundDistance.Companion.TABLE_NAME

@Dao
interface RoundDistanceDao : RoundTypeDao<RoundDistance> {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    override suspend fun insert(insertItem: RoundDistance)

    @Update
    override fun updateSingle(updateItem: RoundDistance)

    @Update
    fun update(vararg roundDistances: RoundDistance)

    @Query("DELETE FROM $TABLE_NAME WHERE roundId = :roundId")
    suspend fun deleteAll(roundId: Int)

    @Query(
            """DELETE 
        FROM $TABLE_NAME 
        WHERE roundId = :roundId AND distanceNumber = :distanceNumber AND subTypeId = :subTypeId"""
    )
    suspend fun delete(roundId: Int, distanceNumber: Int, subTypeId: Int)

    @Delete
    override suspend fun deleteSingle(deleteItem: RoundDistance)
}
