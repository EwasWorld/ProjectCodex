package eywa.projectcodex.database.shootData

import androidx.room.*
import eywa.projectcodex.database.shootData.DatabaseShoot.Companion.TABLE_NAME
import eywa.projectcodex.database.views.PersonalBest
import eywa.projectcodex.database.views.ShootWithScore
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ShootDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(shoot: DatabaseShoot): Long

    @Update
    suspend fun update(vararg shootData: DatabaseShoot)

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
            """
                SELECT 
                        shoot.*,
                        (shoot.isComplete = 1 AND shoot.score = personalBest.score) as isPersonalBest,
                        (personalBest.isTiedPb) as isTiedPersonalBest
                FROM ${ShootWithScore.TABLE_NAME} as shoot
                LEFT JOIN ${PersonalBest.TABLE_NAME} as personalBest
                        ON shoot.roundId = personalBest.roundId AND shoot.nonNullSubTypeId = personalBest.roundSubTypeId
                WHERE shootId IN (:shootIds)
            """
    )
    fun getFullShootInfo(shootIds: List<Int>): Flow<List<DatabaseFullShootInfo>>

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId")
    suspend fun deleteRound(shootId: Int)

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
            """
                SELECT 
                        shoot.*,
                        (shoot.isComplete = 1 AND shoot.score = personalBest.score) as isPersonalBest,
                        (personalBest.isTiedPb) as isTiedPersonalBest
                FROM ${ShootWithScore.TABLE_NAME} as shoot
                LEFT JOIN ${PersonalBest.TABLE_NAME} as personalBest
                        ON shoot.roundId = personalBest.roundId AND shoot.nonNullSubTypeId = personalBest.roundSubTypeId
                WHERE shoot.shootId == :shootId
            """
    )
    fun getFullShootInfo(shootId: Int): Flow<DatabaseFullShootInfo?>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
            """
                SELECT *
                FROM ${ShootWithScore.TABLE_NAME}
                WHERE roundId = :roundId AND nonNullSubTypeId = :subTypeId AND isComplete
                ORDER BY score DESC, dateShot
                LIMIT :count
            """
    )
    fun getMostRecentShootsForRound(
            count: Int,
            roundId: Int,
            subTypeId: Int,
    ): Flow<List<DatabaseFullShootInfo>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
            """
                SELECT *
                FROM ${ShootWithScore.TABLE_NAME}
                WHERE roundId = :roundId
                    AND nonNullSubTypeId = :subTypeId
                    AND score = (
                        SELECT score
                        FROM ${PersonalBest.TABLE_NAME}
                        WHERE roundId = :roundId AND roundSubTypeId = :subTypeId
                    )
            """
    )
    fun getRoundPb(
            roundId: Int,
            subTypeId: Int,
    ): Flow<DatabaseFullShootInfo?>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
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
                                        SELECT dateShot FROM $TABLE_NAME WHERE shootId == :shootId
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
    fun getJoinedFullShoots(shootId: Int): Flow<List<DatabaseFullShootInfo>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
            """
                SELECT 
                        shoot.*, 
                        (shoot.isComplete = 1 AND shoot.score = personalBest.score) as isPersonalBest,
                        (personalBest.isTiedPb) as isTiedPersonalBest
                FROM ${ShootWithScore.TABLE_NAME} as shoot
                LEFT JOIN ${PersonalBest.TABLE_NAME} as personalBest
                        ON shoot.roundId = personalBest.roundId AND shoot.nonNullSubTypeId = personalBest.roundSubTypeId
                LEFT JOIN (
                    SELECT 
                            (ar.isComplete = 1 AND ar.score = pb.score) as ljIsPersonalBest,
                            ar.joinedDate,
                            COUNT(*) as count
                    FROM ${ShootWithScore.TABLE_NAME} as ar 
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
                ) as counts ON counts.joinedDate = shoot.joinedDate
                WHERE counts.count > 0
            """
    )
    fun getAllFullShootInfo(
            filterPersonalBest: Boolean = false,
            fromDate: Calendar? = null,
            toDate: Calendar? = null,
            roundId: Int? = null,
            subTpeId: Int? = null,
    ): Flow<List<DatabaseFullShootInfo>>
}
