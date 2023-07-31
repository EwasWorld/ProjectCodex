package eywa.projectcodex.database.arrows

import androidx.room.*
import eywa.projectcodex.database.arrows.DatabaseArrowScore.Companion.TABLE_NAME

@Dao
interface ArrowScoreDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(arrowScore: DatabaseArrowScore)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vararg arrowScore: DatabaseArrowScore)

    @Update
    suspend fun update(vararg arrowScores: DatabaseArrowScore)

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
     * @param updateArrowScore passed to update
     * @see update
     * @see deleteArrowsBetween
     */
    @Transaction
    suspend fun deleteEndTransaction(
            delArcherRoundId: Int,
            delFromArrowNumber: Int,
            delToArrowNumber: Int,
            vararg updateArrowScore: DatabaseArrowScore
    ) {
        update(*updateArrowScore)
        deleteArrowsBetween(delArcherRoundId, delFromArrowNumber, delToArrowNumber)
    }

    /**
     * Updates THEN inserts
     *
     * @see update
     * @see insert
     */
    @Transaction
    suspend fun updateAndInsert(update: List<DatabaseArrowScore>, insert: List<DatabaseArrowScore>) {
        update(*update.toTypedArray())
        insert(*insert.toTypedArray())
    }
}
