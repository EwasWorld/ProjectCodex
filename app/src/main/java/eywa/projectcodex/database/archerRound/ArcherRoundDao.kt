package eywa.projectcodex.database.archerRound

import androidx.lifecycle.LiveData
import androidx.room.*
import eywa.projectcodex.database.archerRound.ArcherRound.Companion.TABLE_NAME
import eywa.projectcodex.database.views.ArcherRoundWithScore
import eywa.projectcodex.database.views.PersonalBest
import kotlinx.coroutines.flow.Flow
import java.util.*

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
    @Query("SELECT * FROM $TABLE_NAME WHERE archerRoundId == :archerRoundId")
    fun getFullArcherRoundInfo(archerRoundId: Int): Flow<DatabaseFullArcherRoundInfo>

    @Transaction
    @Query(
            """
                SELECT 
                        archerRound.*, 
                        (archerRound.isComplete = 1 AND archerRound.score = pb.score) as isPersonalBest
                FROM ${ArcherRoundWithScore.TABLE_NAME} as archerRound 
                LEFT JOIN ${PersonalBest.TABLE_NAME} as pb 
                        ON archerRound.roundId = pb.roundId AND archerRound.nonNullSubTypeId = pb.roundSubTypeId
                WHERE (:fromDate IS NULL OR archerRound.dateShot >= :fromDate)
                AND (:toDate IS NULL OR archerRound.dateShot <= :toDate)
                AND (:roundId IS NULL OR archerRound.roundId = :roundId)
                AND (
                        :roundId IS NULL OR :subTpeId IS NULL 
                        OR archerRound.roundSubTypeId = :subTpeId 
                        OR (archerRound.roundSubTypeId IS NULL AND :subTpeId = 1 AND NOT archerRound.roundId IS NULL)
                )
                AND ((archerRound.isComplete = 1 AND archerRound.score = pb.score) OR NOT :filterPersonalBest)
            """
    )
    fun getAllFullArcherRoundInfo(
            filterPersonalBest: Boolean = false,
            fromDate: Date? = null,
            toDate: Date? = null,
            roundId: Int? = null,
            subTpeId: Int? = null,
    ): Flow<List<DatabaseFullArcherRoundInfo>>
}
