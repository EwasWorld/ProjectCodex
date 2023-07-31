package eywa.projectcodex.database.views

import androidx.room.DatabaseView
import androidx.room.Embedded
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.DatabaseShootRound
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.RoundArrowCount
import java.util.*

@DatabaseView(
        value = """
                SELECT 
                    archerRound.*, 
                    arrows.score,
                    shootRound.roundId,
                    (CASE WHEN roundSubTypeId IS NULL THEN 1 else roundSubTypeId END) as nonNullSubTypeId,
                    ((NOT shootRound.roundId IS NULL) AND arrows.count = roundCount.count) as isComplete,
                    ( 
                        -- Find the latest date earlier than or equal to this one that doesn't join with previous
                        -- This will be the first round (inclusive) in the sequence
                        SELECT MAX(dateShot)
                        FROM ${ArcherRound.TABLE_NAME}
                        WHERE dateShot <= archerRound.dateShot AND NOT joinWithPrevious
                    ) as joinedDate
                FROM ${ArcherRound.TABLE_NAME} as archerRound
                LEFT JOIN ${DatabaseShootRound.TABLE_NAME} as shootRound 
                        ON shootRound.archerRoundId = archerRound.archerRoundId
                LEFT JOIN (
                    SELECT SUM(arrowCount) as count, roundId
                    FROM ${RoundArrowCount.TABLE_NAME}
                    GROUP BY roundId
                ) as roundCount ON shootRound.roundId = roundCount.roundId
                LEFT JOIN (
                    SELECT COUNT(*) as count, SUM(score) as score, archerRoundId
                    FROM ${ArrowValue.TABLE_NAME}
                    GROUP BY archerRoundId
                ) as arrows ON archerRound.archerRoundId = arrows.archerRoundId
                """,
        viewName = ArcherRoundWithScore.TABLE_NAME,
)
data class ArcherRoundWithScore(
        @Embedded val archerRound: ArcherRound,
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

