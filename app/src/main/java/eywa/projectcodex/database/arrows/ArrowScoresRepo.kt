package eywa.projectcodex.database.arrows

import androidx.room.Transaction

/**
 * A Repository manages queries and allows you to use multiple backends. In the most common example, the Repository
 * implements the logic for deciding whether to fetch data from a network or use results cached in a local database.
 *  - https://codelabs.developers.google.com/codelabs/android-room-with-a-view-kotlin/#7
 *
 *  If DAOs have just one or two methods then repositories are often combined
 */
class ArrowScoresRepo(private val arrowScoreDao: ArrowScoreDao) {
    suspend fun insert(vararg arrowScores: DatabaseArrowScore) {
        arrowScoreDao.insert(*arrowScores)
    }

    suspend fun update(vararg arrowScores: DatabaseArrowScore) {
        arrowScoreDao.update(*arrowScores)
    }

    suspend fun deleteAll() {
        arrowScoreDao.deleteAll()
    }

    /**
     * Update [firstArrowToDelete] to end's arrow values to be that of [firstArrowToDelete] - [numberToDelete]. This
     * will overwrite the arrows to be deleted. Then delete [numberToDelete] off the end (as they're now duplicated)
     * @throws IllegalArgumentException if allArrows is null or empty, or if [firstArrowToDelete] and [numberToDelete]
     * results in an out of range arrowNumber
     */
    @Transaction
    suspend fun deleteEnd(allArrowsInRound: List<DatabaseArrowScore>, firstArrowToDelete: Int, numberToDelete: Int) {
        require(numberToDelete > 0) { "numberToDelete must be > 0" }
        require(
                allArrowsInRound.distinctBy { it.shootId }.size == 1
        ) { "allArrowsInRound cannot contain arrows from multiple shoots" }
        require(
                allArrowsInRound.any { it.arrowNumber == firstArrowToDelete }
        ) { "allArrowsInRound does not contain firstArrowToDelete" }

        /*
         * Updating before deleting because arrowNumber is part of the primary key.
         * Update overwrites score and isX for all 'deleted' arrows (effectively shifting all scores down),
         *      then delete removes the highest [numberToDelete] arrow numbers
         */

        // Arrows before [firstArrowToDelete] should remain unchanged
        val arrows = allArrowsInRound
                .sortedBy { it.arrowNumber }
                .dropWhile { it.arrowNumber < firstArrowToDelete }

        val deletedCount = numberToDelete.coerceAtMost(arrows.size)
        arrows.drop(deletedCount).takeIf { it.isNotEmpty() }
                ?.map { DatabaseArrowScore(it.shootId, it.arrowNumber - deletedCount, it.score, it.isX) }
                ?.let { update(*it.toTypedArray()) }

        arrowScoreDao.deleteArrows(
                allArrowsInRound[0].shootId,
                arrows.takeLast(deletedCount).map { it.arrowNumber },
        )
    }

    suspend fun insertEnd(allArrowsInRound: List<DatabaseArrowScore>, toInsert: List<DatabaseArrowScore>) {
        if (toInsert.isEmpty()) return
        val distinctByShootIds = allArrowsInRound.distinctBy { it.shootId }
        require(distinctByShootIds.size == 1) { "allArrowsInRound cannot contain arrows from multiple shoots" }
        require(allArrowsInRound.isNotEmpty()) { "Must provide arrows to shift" }
        val shootId = distinctByShootIds[0].shootId

        /*
         * Check arrow numbers
         */
        val minArrowNumber = toInsert.minOf { it.arrowNumber }
        require(minArrowNumber >= 1) { "Arrow numbers must be >= 1" }
        require(minArrowNumber <= allArrowsInRound.maxOf { it.arrowNumber }) {
            "Insert must start within existing arrows indices"
        }
        require(
                (minArrowNumber until minArrowNumber + toInsert.size).toSet()
                        == toInsert.map { it.arrowNumber }.toSet()
        ) { "Arrow numbers for the arrows to insert must be consecutive" }

        // Shift other arrowNumbers to make space for inserted ones
        val allArrowsReady =
                toInsert.plus(allArrowsInRound.filter { it.arrowNumber >= minArrowNumber }.map {
                    DatabaseArrowScore(shootId, it.arrowNumber + toInsert.size, it.score, it.isX)
                })

        val currentArrowNumbers = allArrowsInRound.map { it.arrowNumber }
        val updateArrows = allArrowsReady.filter { currentArrowNumbers.contains(it.arrowNumber) }
        val insertArrows = allArrowsReady.filter { !currentArrowNumbers.contains(it.arrowNumber) }

        arrowScoreDao.updateAndInsert(updateArrows, insertArrows)
    }
}