package eywa.projectcodex.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import eywa.projectcodex.database.entities.Round

@Dao
interface RoundDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(round: Round)

    @Query("SELECT * FROM rounds WHERE name = :uniqueName")
    fun getRoundByName(uniqueName: String): LiveData<List<Round>>

    @Query("SELECT * FROM rounds")
    fun getAllRounds(): LiveData<List<Round>>

    @Query("SELECT MAX(roundId) FROM rounds")
    fun getMaxRoundId(): LiveData<Int>

    @Update
    fun update(vararg rounds: Round)

    @Query("DELETE FROM rounds WHERE roundId = :roundId")
    suspend fun delete(roundId: Int)

    @Query("DELETE FROM rounds")
    suspend fun deleteAll()
}