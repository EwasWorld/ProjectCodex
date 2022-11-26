package eywa.projectcodex.database.rounds

import androidx.lifecycle.LiveData
import androidx.room.*
import eywa.projectcodex.database.rounds.RoundSubType.Companion.TABLE_NAME

@Dao
interface RoundSubTypeDao : RoundTypeDao<RoundSubType> {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    override suspend fun insert(insertItem: RoundSubType)

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllSubTypes(): LiveData<List<RoundSubType>>

    @Update
    override fun updateSingle(updateItem: RoundSubType)

    @Update
    fun update(vararg roundSubTypes: RoundSubType)

    @Query("DELETE FROM $TABLE_NAME WHERE roundId = :roundId")
    suspend fun deleteAll(roundId: Int)

    @Query("DELETE FROM $TABLE_NAME WHERE roundId = :roundId AND subTypeId = :subTypeId")
    suspend fun delete(roundId: Int, subTypeId: Int)

    @Delete
    override suspend fun deleteSingle(deleteItem: RoundSubType)
}