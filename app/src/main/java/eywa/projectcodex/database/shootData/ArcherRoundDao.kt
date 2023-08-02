package eywa.projectcodex.database.shootData

import androidx.room.*
import eywa.projectcodex.database.shootData.DatabaseShoot.Companion.TABLE_NAME
import eywa.projectcodex.database.views.ArcherRoundWithScore
import eywa.projectcodex.database.views.PersonalBest
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ArcherRoundDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(shoot: DatabaseShoot): Long

    @Update
    suspend fun update(vararg shootData: DatabaseShoot)

    @Query("SELECT * FROM $TABLE_NAME WHERE archerRoundId IN (:archerRoundIds)")
    fun getFullArcherRoundInfo(archerRoundIds: List<Int>): Flow<List<DatabaseFullArcherRoundInfo>>

    @Query("DELETE FROM $TABLE_NAME WHERE archerRoundId = :archerRoundId")
    suspend fun deleteRound(archerRoundId: Int)

    @Transaction
    @Query("SELECT * FROM $TABLE_NAME WHERE archerRoundId == :archerRoundId")
    fun getFullArcherRoundInfo(archerRoundId: Int): Flow<DatabaseFullArcherRoundInfo?>

    @Query(
            """
                SELECT *,
                        ( 
                            -- Find the latest date earlier than or equal to this one that doesn't join with previous
                            -- This will be the first round (inclusive) in the sequence
                            SELECT MAX(dateShot)
                            FROM $TABLE_NAME
                            WHERE (NOT joinWithPrevious)
                                    AND dateShot <= (
                                        SELECT dateShot FROM $TABLE_NAME WHERE archerRoundId == :archerRoundId
                                    ) 
                        ) as joinedDate
                FROM $TABLE_NAME
                WHERE dateShot >= joinedDate
                AND (
                    -- Find the earliest date late than this one that doesn't join with previous
                    -- This will be the last round (exclusive) in the sequence
                    dateShot < (
                        SELECT MIN(dateShot)
                        FROM $TABLE_NAME
                        WHERE dateShot > joinedDate AND NOT joinWithPrevious
                    )
                    -- If there are no rounds later than this one, then we don't need to bound this side
                    OR 1 > (
                        SELECT COUNT(dateShot)
                        FROM $TABLE_NAME
                        WHERE dateShot > joinedDate
                        LIMIT 1
                    )
                )
            """
    )
    fun getJoinedFullArcherRounds(archerRoundId: Int): Flow<List<DatabaseFullArcherRoundInfo>>

    @Transaction
    @Query(
            """
                SELECT 
                        archerRound.*, 
                        (archerRound.isComplete = 1 AND archerRound.score = personalBest.score) as isPersonalBest
                FROM ${ArcherRoundWithScore.TABLE_NAME} as archerRound
                LEFT JOIN ${PersonalBest.TABLE_NAME} as personalBest
                        ON archerRound.roundId = personalBest.roundId AND archerRound.nonNullSubTypeId = personalBest.roundSubTypeId
                LEFT JOIN (
                    SELECT 
                            (ar.isComplete = 1 AND ar.score = pb.score) as ljIsPersonalBest,
                            ar.joinedDate,
                            COUNT(*) as count
                    FROM ${ArcherRoundWithScore.TABLE_NAME} as ar 
                    LEFT JOIN ${PersonalBest.TABLE_NAME} as pb 
                            ON ar.roundId = pb.roundId AND ar.nonNullSubTypeId = pb.roundSubTypeId
                    WHERE (:fromDate IS NULL OR ar.dateShot >= :fromDate)
                    AND (:toDate IS NULL OR ar.dateShot <= :toDate)
                    AND (:roundId IS NULL OR ar.roundId = :roundId)
                    AND (
                            :roundId IS NULL OR :subTpeId IS NULL 
                            OR ar.nonNullSubTypeId = :subTpeId 
                            OR (ar.nonNullSubTypeId IS NULL AND :subTpeId = 1 AND NOT ar.roundId IS NULL)
                    )
                    AND (ljIsPersonalBest OR NOT :filterPersonalBest)
                    GROUP BY ar.joinedDate
                ) as counts ON counts.joinedDate = archerRound.joinedDate
                WHERE counts.count > 0
            """
    )
    fun getAllFullArcherRoundInfo(
            filterPersonalBest: Boolean = false,
            fromDate: Calendar? = null,
            toDate: Calendar? = null,
            roundId: Int? = null,
            subTpeId: Int? = null,
    ): Flow<List<DatabaseFullArcherRoundInfo>>
}
