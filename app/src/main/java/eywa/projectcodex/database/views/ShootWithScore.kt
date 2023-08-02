package eywa.projectcodex.database.views

import androidx.room.DatabaseView
import androidx.room.Embedded
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.database.shootData.DatabaseShootRound
import java.util.*

@DatabaseView(
        value = """
                SELECT 
                    shoot.*, 
                    arrows.score,
                    shootRound.roundId,
                    (CASE WHEN roundSubTypeId IS NULL THEN 1 else roundSubTypeId END) as nonNullSubTypeId,
                    ((NOT shootRound.roundId IS NULL) AND arrows.count = roundCount.count) as isComplete,
                    ( 
                        -- Find the latest date earlier than or equal to this one that doesn't join with previous
                        -- This will be the first round (inclusive) in the sequence
                        SELECT MAX(dateShot)
                        FROM ${DatabaseShoot.TABLE_NAME}
                        WHERE dateShot <= shoot.dateShot AND NOT joinWithPrevious
                    ) as joinedDate
                FROM ${DatabaseShoot.TABLE_NAME} as shoot
                LEFT JOIN ${DatabaseShootRound.TABLE_NAME} as shootRound 
                        ON shootRound.shootId = shoot.shootId
                LEFT JOIN (
                    SELECT SUM(arrowCount) as count, roundId
                    FROM ${RoundArrowCount.TABLE_NAME}
                    GROUP BY roundId
                ) as roundCount ON shootRound.roundId = roundCount.roundId
                LEFT JOIN (
                    SELECT COUNT(*) as count, SUM(score) as score, shootId
                    FROM ${DatabaseArrowScore.TABLE_NAME}
                    GROUP BY shootId
                ) as arrows ON shoot.shootId = arrows.shootId
                """,
        viewName = ShootWithScore.TABLE_NAME,
)
data class ShootWithScore(
        @Embedded val shoot: DatabaseShoot,
        val score: Int,
        val roundId: Int?,
        val nonNullSubTypeId: Int,
        val isComplete: Boolean,
        val joinedDate: Calendar,
) {
    companion object {
        const val TABLE_NAME = "completed_round_scores"
    }
}

