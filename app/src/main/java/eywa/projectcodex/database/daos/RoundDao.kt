package eywa.projectcodex.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import eywa.projectcodex.database.entities.ROUND_TABLE_NAME
import eywa.projectcodex.database.entities.Round

@Dao
interface RoundDao : RoundTypeDao<Round> {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    override suspend fun insert(round: Round)

    @Query("SELECT * FROM $ROUND_TABLE_NAME WHERE name = :uniqueName")
    fun getRoundByName(uniqueName: String): LiveData<List<Round>>

    @Query("SELECT * FROM $ROUND_TABLE_NAME WHERE roundId = :id")
    fun getRoundById(id: Int): LiveData<Round>

    @Query("SELECT * FROM $ROUND_TABLE_NAME")
    fun getAllRounds(): LiveData<List<Round>>

    @Query("SELECT MAX(roundId) FROM $ROUND_TABLE_NAME")
    fun getMaxRoundId(): LiveData<Int>

    @Update
    override fun updateSingle(updateItem: Round)

    @Update
    fun update(vararg rounds: Round)

    @Query("DELETE FROM $ROUND_TABLE_NAME WHERE roundId = :roundId")
    suspend fun delete(roundId: Int)

    @Query("DELETE FROM $ROUND_TABLE_NAME")
    suspend fun deleteAll()
}