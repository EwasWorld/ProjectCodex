package eywa.projectcodex.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import eywa.projectcodex.database.entities.ArrowValue

@Dao
interface ArrowValueDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(arrowValue: ArrowValue)

    @Transaction
    suspend fun insert(vararg arrowValues: ArrowValue) {
        for (arrow in arrowValues) {
            insert(arrow)
        }
    }

    @Update
    suspend fun update(vararg arrowValues: ArrowValue)

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

    /**
     * @param fromArrowNumber inclusive
     * @param toArrowNumber exclusive
     */
    @Query(
            """
            DELETE FROM arrow_values 
            WHERE archerRoundId = :archerRoundId 
                AND arrowNumber >= :fromArrowNumber
                AND arrowNumber < :toArrowNumber
            """
    )
    suspend fun deleteArrowsBetween(archerRoundId: Int, fromArrowNumber: Int, toArrowNumber: Int)

    /**
     * Calls update then deleteArrowsBetween as a single transaction
     * @param delArcherRoundId passed to deleteArrowsBetween
     * @param delFromArrowNumber passed to deleteArrowsBetween
     * @param delToArrowNumber passed to deleteArrowsBetween
     * @param updateArrowValue passed to update
     * @see update
     * @see deleteArrowsBetween
     */
    @Transaction
    suspend fun deleteEndTransaction(
            delArcherRoundId: Int, delFromArrowNumber: Int, delToArrowNumber: Int, vararg updateArrowValue: ArrowValue
    ) {
        update(*updateArrowValue)
        deleteArrowsBetween(delArcherRoundId, delFromArrowNumber, delToArrowNumber)
    }
}