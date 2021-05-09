package eywa.projectcodex.database.rounds

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RoundSubTypeDao : RoundTypeDao<RoundSubType> {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    override suspend fun insert(roundSubType: RoundSubType)

    @Query("SELECT * FROM round_sub_types")
    fun getAllSubTypes(): LiveData<List<RoundSubType>>

    @Update
    override fun updateSingle(updateItem: RoundSubType)

    @Update
    fun update(vararg roundSubTypes: RoundSubType)

    @Query("DELETE FROM round_sub_types WHERE roundId = :roundId")
    suspend fun deleteAll(roundId: Int)

    @Query("DELETE FROM round_sub_types WHERE roundId = :roundId AND subTypeId = :subTypeId")
    suspend fun delete(roundId: Int, subTypeId: Int)
}