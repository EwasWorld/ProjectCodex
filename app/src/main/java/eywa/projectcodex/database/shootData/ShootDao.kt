package eywa.projectcodex.database.shootData

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import eywa.projectcodex.database.arrows.DatabaseArrowCounter
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.bow.DEFAULT_BOW_ID
import eywa.projectcodex.database.bow.DatabaseBow
import eywa.projectcodex.database.shootData.DatabaseShoot.Companion.TABLE_NAME
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHead
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadDetail
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadMatch
import eywa.projectcodex.database.views.PersonalBest
import eywa.projectcodex.database.views.ShootWithScore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

@Dao
interface ShootDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(shoot: DatabaseShoot): Long

    @Update
    suspend fun update(vararg shootData: DatabaseShoot)

    @Transaction
    @Query(
            """
                SELECT 
                        shoot.shootId,
                        shoot.dateShot,
                        shoot.archerId,
                        shoot.countsTowardsHandicap,
                        shoot.bowId,
                        shoot.goalScore,
                        shoot.shootStatus,
                        shoot.joinWithPrevious,
                        (shoot.scoringArrowCount = shoot.roundCount AND shoot.score = personalBest.score) as isPersonalBest,
                        (personalBest.isTiedPb) as isTiedPersonalBest,
                        bow.type as bow
                FROM ${ShootWithScore.TABLE_NAME} as shoot
                LEFT JOIN ${PersonalBest.TABLE_NAME} as personalBest
                        ON shoot.roundId = personalBest.roundId AND shoot.nonNullSubTypeId = personalBest.roundSubTypeId
                LEFT JOIN ${DatabaseBow.TABLE_NAME} as bow ON bow.bowId = $DEFAULT_BOW_ID
                WHERE shootId IN (:shootIds)
            """
    )
    fun getFullShootInfo(shootIds: List<Int>): Flow<List<DatabaseFullShootInfo>>

    @Query("DELETE FROM $TABLE_NAME WHERE shootId = :shootId")
    suspend fun deleteRound(shootId: Int)

    fun getFullShootInfo(shootId: Int) = getFullShootInfo(listOf(shootId)).map { it.firstOrNull() }

    /**
     * Most recent shoots with the specified round that have at least 1 arrow shot
     */
    @Transaction
    @Query(
            """
                SELECT shootRound.*
                FROM ${DatabaseShootRound.TABLE_NAME} as shootRound 
                LEFT JOIN $TABLE_NAME as shoot ON shootRound.shootId = shoot.shootId
                WHERE NOT roundId IS NULL
                ORDER BY dateShot DESC
                LIMIT 1
            """
    )
    fun getMostRecentRoundShot(): Flow<DatabaseShootRound?>

    /**
     * Most recent shoots with the specified round that have at least 1 arrow shot
     */
    @Transaction
    @Query(
            """
                SELECT shootId, dateShot, score, (scoringArrowCount = roundCount OR counterCount = roundCount) as isComplete
                FROM ${ShootWithScore.TABLE_NAME}
                WHERE roundId = :roundId AND nonNullSubTypeId = :subTypeId AND (scoringArrowCount > 0 OR counterCount > 0)
                ORDER BY dateShot DESC
                LIMIT :count
            """
    )
    fun getMostRecentShootsForRound(
            count: Int,
            roundId: Int,
            subTypeId: Int,
    ): Flow<List<DatabaseShootShortRecord>>

    /**
     * Top scoring shoots with the specified round that are complete
     */
    @Transaction
    @Query(
            """
                SELECT shootId, dateShot, score, 1 as isComplete
                FROM ${ShootWithScore.TABLE_NAME}
                WHERE roundId = :roundId AND nonNullSubTypeId = :subTypeId AND scoringArrowCount = roundCount
                ORDER BY score DESC, dateShot
                LIMIT :count
            """
    )
    fun getHighestScoreShootsForRound(
            count: Int,
            roundId: Int,
            subTypeId: Int,
    ): Flow<List<DatabaseShootShortRecord>>

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
                        ) as joinedDate,
                        bow.type as bow
                FROM $TABLE_NAME
                LEFT JOIN ${DatabaseBow.TABLE_NAME} as bow ON bow.bowId = $DEFAULT_BOW_ID
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

    @RawQuery(observedEntities = [ShootWithScore::class])
    fun getAllFullShootInfo(query: SupportSQLiteQuery): Flow<List<DatabaseFullShootInfo>>

    @Query(
            """
                SELECT
                        strftime("%d-%m", shoot.dateShot / 1000, 'unixepoch') as dateString,
                        (TOTAL(scores.count) 
                            + TOTAL(counts.shotCount)
                            + TOTAL(rounds.sightersCount) 
                            + TOTAL(h2hMatches.count)
                            + TOTAL(h2hDetails.count)) as count
                FROM $TABLE_NAME as shoot
                LEFT JOIN (
                    SELECT s.shootId, COUNT(s.rowId) as count
                    FROM ${DatabaseArrowScore.TABLE_NAME} as s
                    GROUP BY s.shootId 
                ) as scores ON shoot.shootId = scores.shootId
                LEFT JOIN (
                    SELECT a.shootId, a.shotCount 
                    FROM ${DatabaseArrowCounter.TABLE_NAME} as a
                    GROUP BY a.shootId
                ) as counts ON shoot.shootId = counts.shootId
                LEFT JOIN (
                    SELECT r.shootId, r.sightersCount 
                    FROM ${DatabaseShootRound.TABLE_NAME} as r
                    GROUP BY r.shootId
                ) as rounds ON shoot.shootId = rounds.shootId
                LEFT JOIN (
                    SELECT h.shootId, SUM(h.sightersCount) as count
                    FROM ${DatabaseHeadToHeadMatch.TABLE_NAME} as h
                    GROUP BY h.shootId
                ) as h2hMatches ON shoot.shootId = h2hMatches.shootId
                LEFT JOIN (
                    SELECT x.shootId, 
                            (
                                -- Each shoot-off set will be a single arrow
                                x.shootOffSetCount 
                                -- Non shoot-off sets
                                + (x.totalSetCount - x.shootOffSetCount)
                                -- Either use the custom end size or the standard 2 or 3 for teams or individuals
                                * IFNULL(i.endSize, CASE WHEN i.teamSize == 1 THEN 3 ELSE 2 END) 
                            ) as count
                    FROM (
                        SELECT 
                            shootId,
                            COUNT(DISTINCT d.matchNumber || '-' || d.setNumber) as totalSetCount,
                            SUM(CASE WHEN d.type == "SHOOT_OFF" THEN 1 ELSE 0 END) as shootOffSetCount
                            FROM ${DatabaseHeadToHeadDetail.TABLE_NAME} as d
                            GROUP BY d.shootId
                    ) as x
                    LEFT JOIN ${DatabaseHeadToHead.TABLE_NAME} as i ON x.shootId = i.shootId
                ) as h2hDetails ON shoot.shootId = h2hDetails.shootId
                WHERE shoot.dateShot >= :fromDate AND shoot.dateShot <= :toDate
                GROUP BY dateString
                ORDER BY shoot.dateShot, count
            """
    )
    fun getCountsForCalendar(
            fromDate: Calendar,
            toDate: Calendar,
    ): Flow<List<DatabaseArrowCountCalendarData>>

    @Transaction
    @Query(
            """
                SELECT shootId 
                FROM (
                    SELECT s.shootId, strftime("%d-%m", dateShot / 1000, 'unixepoch') as dateString
                    FROM $TABLE_NAME as s
                    LEFT JOIN ${DatabaseShootRound.TABLE_NAME} as r ON r.shootId = s.shootId
                    WHERE :dateShot = dateString AND r.roundId = :roundId
                    ORDER BY dateShot DESC
                    LIMIT 1
                )
            """
    )
    fun getQualifyingRoundId(dateShot: String, roundId: Int): Flow<Int?>
}
