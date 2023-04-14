package eywa.projectcodex.database.views

import androidx.room.DatabaseView
import androidx.room.Embedded
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.RoundArrowCount

@DatabaseView(
        value = """
                SELECT 
                    archerRound.*, 
                    arrows.score,
                    (CASE WHEN roundSubTypeId IS NULL THEN 1 else roundSubTypeId END) as nonNullSubTypeId,
                    ((NOT archerRound.roundId IS NULL) AND arrows.count = roundCount.count) as isComplete
                FROM ${ArcherRound.TABLE_NAME} as archerRound
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
                """,
        viewName = ArcherRoundWithScore.TABLE_NAME,
)
data class ArcherRoundWithScore(
        @Embedded
        val archerRound: ArcherRound,
        val score: Int,
        val nonNullSubTypeId: Int,
        val isComplete: Boolean,
) {
    companion object {
        const val TABLE_NAME = "completed_round_scores"
    }
}

