package eywa.projectcodex.database.archerRound

import androidx.lifecycle.LiveData
import androidx.room.*
import eywa.projectcodex.database.archerRound.ArcherRound.Companion.TABLE_NAME
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import kotlinx.coroutines.flow.Flow

@Dao
interface ArcherRoundDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(archerRound: ArcherRound): Long

    @Update
    suspend fun update(vararg archerRounds: ArcherRound)

    @Query("SELECT MAX(archerRoundId) FROM $TABLE_NAME")
    fun getMaxId(): LiveData<Int>

    @Query("SELECT * FROM $TABLE_NAME WHERE archerRoundId = :id")
    fun getArcherRoundById(id: Int): LiveData<ArcherRound>

    @Query("DELETE FROM $TABLE_NAME WHERE archerRoundId = :archerRoundId")
    suspend fun deleteRound(archerRoundId: Int)

    @Transaction
    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllFullArcherRoundInfo(): Flow<List<DatabaseFullArcherRoundInfo>>

    @Transaction
    @Query("SELECT * FROM $TABLE_NAME WHERE archerRoundId == :archerRoundId")
    fun getFullArcherRoundInfo(archerRoundId: Int): Flow<DatabaseFullArcherRoundInfo>

    /**
     * @return the [ArcherRound.archerRoundId] of all [ArcherRound]s
     * with a completed [Round] (count [ArrowValue] == sum [RoundArrowCount.arrowCount])
     * with the highest total score (sum [ArrowValue.score]) for the given [Round]
     */
    @Query(
            """
                SELECT 
                    archerRound.archerRoundId, 
                    MAX(arrows.score)
                FROM $TABLE_NAME as archerRound
                LEFT JOIN (
                    SELECT SUM(arrowCount) as count, roundId
                    FROM ${RoundArrowCount.TABLE_NAME}
                    GROUP BY roundId
                ) as roundCount ON archerRound.roundId = roundCount.roundId
                LEFT JOIN (
                    SELECT COUNT(*) as count, SUM(score) as score, archerRoundId
                    FROM ${ArrowValue.TABLE_NAME}
                    GROUP BY archerRoundId
                ) as arrows ON archerRound.archerRoundId = arrows.archerRoundId
                WHERE
                    NOT archerRound.roundId is NULL
                    AND arrows.count = roundCount.count
                GROUP BY 
                    archerRound.roundId,
                    -- Treat NULL subtype as 1 when grouping
                    CASE WHEN archerRound.roundSubTypeId IS NULL THEN 1 else archerRound.roundSubTypeId END
            """
    )
    fun getPersonalBests(): Flow<List<ArcherRoundIdWrapper>>

    // TODO Remove custom type example when from-to filter has been implemented
//    @Entity
//    data class User(private val birthday: Date?)

//    @Query("SELECT * FROM user WHERE birthday BETWEEN :from AND :to")
//    fun findUsersBornBetweenDates(from: Date, to: Date): List<User>

    data class ArcherRoundIdWrapper(val archerRoundId: Int)
}
