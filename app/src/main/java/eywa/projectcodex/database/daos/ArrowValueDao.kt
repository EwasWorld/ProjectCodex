package eywa.projectcodex.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import eywa.projectcodex.database.entities.ArrowValue

@Dao
interface ArrowValueDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(arrowValue: ArrowValue)

    @Update
    fun update(vararg arrowValue: ArrowValue)

    /**
     * When returning LiveData, suspend is not needed as LiveData is already async
     */
    @Query("SELECT * FROM arrow_values WHERE archerRoundId = :archerRoundId")
    fun getArrowValuesForRound(archerRoundId: Int): LiveData<List<ArrowValue>>

    @Query("SELECT * FROM arrow_values")
    fun getAllArrowValues(): LiveData<List<ArrowValue>>

    @Query("DELETE FROM arrow_values")
    suspend fun deleteAll()

    @Query("DELETE FROM arrow_values WHERE archerRoundId = :archerRoundId")
    suspend fun deleteRoundsArrows(archerRoundId: Int)
}