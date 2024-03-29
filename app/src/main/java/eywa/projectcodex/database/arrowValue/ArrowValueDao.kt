package eywa.projectcodex.database.arrowValue

import androidx.room.*
import eywa.projectcodex.database.arrowValue.ArrowValue.Companion.TABLE_NAME

@Dao
interface ArrowValueDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(arrowValue: ArrowValue)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vararg arrowValue: ArrowValue)

    @Update
    suspend fun update(vararg arrowValues: ArrowValue)

    @Query("DELETE FROM $TABLE_NAME")
    suspend fun deleteAll()

    @Query("DELETE FROM $TABLE_NAME WHERE archerRoundId = :archerRoundId")
    suspend fun deleteRoundsArrows(archerRoundId: Int)

    /**
     * @param fromArrowNumber inclusive
     * @param toArrowNumber exclusive
     */
    @Query(
            """
            DELETE FROM $TABLE_NAME 
            WHERE archerRoundId = :archerRoundId 
                AND arrowNumber >= :fromArrowNumber
                AND arrowNumber < :toArrowNumber
            """
    )
    suspend fun deleteArrowsBetween(archerRoundId: Int, fromArrowNumber: Int, toArrowNumber: Int)

    @Query(
            """
            DELETE FROM $TABLE_NAME 
            WHERE archerRoundId = :archerRoundId AND arrowNumber IN (:arrowNumbers)
            """
    )
    suspend fun deleteArrows(archerRoundId: Int, arrowNumbers: List<Int>)

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

    /**
     * Updates THEN inserts
     *
     * @see update
     * @see insert
     */
    @Transaction
    suspend fun updateAndInsert(update: List<ArrowValue>, insert: List<ArrowValue>) {
        update(*update.toTypedArray())
        insert(*insert.toTypedArray())
    }
}
