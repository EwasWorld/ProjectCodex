package eywa.projectcodex.database.views

import androidx.room.DatabaseView

@DatabaseView(
        value = """
                SELECT 
                    roundId,
                    nonNullSubTypeId as roundSubTypeId,
                    MAX(score) as score
                FROM ${ArcherRoundWithScore.TABLE_NAME}
                GROUP BY roundId, roundSubTypeId
                HAVING isComplete = 1
                """,
        viewName = PersonalBest.TABLE_NAME,
)
data class PersonalBest(
        val roundId: Int,
        val roundSubTypeId: Int,
        val score: Int,
) {
    companion object {
        const val TABLE_NAME = "personal_bests"
    }
}
