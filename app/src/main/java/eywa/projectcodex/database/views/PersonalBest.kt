package eywa.projectcodex.database.views

import androidx.room.DatabaseView

@DatabaseView(
        value = """
                SELECT
                    pbs.roundId as roundId,
                    pbs.roundSubTypeId as roundSubTypeId,
                    pbs.pbScore as score,
                    COUNT(*) > 1 as isTiedPb
                FROM ${ShootWithScore.TABLE_NAME} as shoot
                LEFT JOIN (
                    SELECT
                        roundId,
                        nonNullSubTypeId as roundSubTypeId,
                        MAX(score) as pbScore
                    FROM ${ShootWithScore.TABLE_NAME}
                    WHERE isComplete AND NOT roundId IS NULL
                    GROUP BY roundId, roundSubTypeId
                ) as pbs ON shoot.roundId = pbs.roundId AND shoot.nonNullSubTypeId = pbs.roundSubTypeId
                WHERE shoot.score = pbs.pbScore
                GROUP BY pbs.roundId, pbs.roundSubTypeId
                """,
        viewName = PersonalBest.TABLE_NAME,
)
data class PersonalBest(
        val roundId: Int,
        val roundSubTypeId: Int,
        val score: Int,
        val isTiedPb: Boolean,
) {
    companion object {
        const val TABLE_NAME = "personal_bests"

        /**
         * TODO Would be better to use a windowed query to get isTiedPb,
         *  however this isn't available until SQLite version 3.28.0 aka api level 30.
         *
         * https://www.sqlitetutorial.net/sqlite-window-functions/sqlite-rank/
         * https://stackoverflow.com/questions/2129693/using-limit-within-group-by-to-get-n-results-per-group
         */
        @Suppress("unused")
        private const val windowedQuery = """
            SELECT 
                score,
                roundId,
                subTypeId,
                COUNT(*) > 1 as isTiedPb
            FROM (
                SELECT *, RANK() OVER (PARTITION BY roundId, subTypeId ORDER BY score DESC) AS rank
                FROM ShootWithScore
                WHERE NOT roundId IS NULL
            )
            WHERE rank = 1
            GROUP BY roundId, subTypeId
        """
    }
}
