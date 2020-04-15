package eywa.projectcodex.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import eywa.projectcodex.database.entities.RoundSubType

@Dao
interface RoundSubTypeDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(roundSubType: RoundSubType)

    @Query("SELECT * FROM round_sub_types")
    fun getAllSubTypes(): LiveData<List<RoundSubType>>

    @Update
    fun update(vararg roundSubTypes: RoundSubType)

    @Query("DELETE FROM round_sub_types WHERE roundId = :roundId")
    suspend fun deleteAll(roundId: Int)

    @Query("DELETE FROM round_sub_types WHERE roundId = :roundId AND subTypeId = :subTypeId")
    suspend fun delete(roundId: Int, subTypeId: Int)
}