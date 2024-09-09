package eywa.projectcodex.database.arrows

import androidx.room.Transaction
import eywa.projectcodex.common.logging.debugLog
import eywa.projectcodex.database.shootData.DatabaseShoot

/**
 * A Repository manages queries and allows you to use multiple backends. In the most common example, the Repository
 * implements the logic for deciding whether to fetch data from a network or use results cached in a local database.
 *  - https://codelabs.developers.google.com/codelabs/android-room-with-a-view-kotlin/#7
 *
 *  If DAOs have just one or two methods then repositories are often combined
 */
class ArrowScoresRepo(private val arrowScoreDao: ArrowScoreDao) {
    val allArrows = arrowScoreDao.getAllArrows()

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
     * Deletes [numberToDelete] arrows from a round starting with [firstArrowNumberToDelete].
     * Shifts remaining high arrow numbers so that arrow numbers are always consecutive
     *
     * @param allArrowsInRound all arrows for a single [DatabaseShoot]. Cannot be empty
     * @param firstArrowNumberToDelete the first arrow to delete (arrow numbers are 1-indexed).
     * Must be in [allArrowsInRound]
     * @param numberToDelete must be > 0
     */
    @Transaction
    suspend fun deleteEnd(
            allArrowsInRound: List<DatabaseArrowScore>,
            firstArrowNumberToDelete: Int,
            numberToDelete: Int,
    ) {
        require(numberToDelete > 0) { "numberToDelete must be > 0" }
        require(
                allArrowsInRound.distinctBy { it.shootId }.size == 1
        ) { "allArrowsInRound cannot contain arrows from multiple shoots" }
        require(
                allArrowsInRound.any { it.arrowNumber == firstArrowNumberToDelete }
        ) { "allArrowsInRound does not contain firstArrowToDelete" }

        /*
         * Updating before deleting because arrowNumber is part of the primary key.
         * Update overwrites score and isX for all 'deleted' arrows (effectively shifting all scores down),
         *      then delete removes the highest [numberToDelete] arrow numbers
         */

        // Arrows before [firstArrowToDelete] should remain unchanged
        val arrows = allArrowsInRound
                .sortedBy { it.arrowNumber }
                .dropWhile { it.arrowNumber < firstArrowNumberToDelete }

        val deletedCount = numberToDelete.coerceAtMost(arrows.size)
        arrows.drop(deletedCount).takeIf { it.isNotEmpty() }
                ?.map { DatabaseArrowScore(it.shootId, it.arrowNumber - deletedCount, it.score, it.isX) }
                ?.let { update(*it.toTypedArray()) }

        arrowScoreDao.deleteArrows(
                allArrowsInRound[0].shootId,
                arrows.takeLast(deletedCount).map { it.arrowNumber },
        )
    }

    /**
     * Inserts an end of arrows into a [DatabaseShoot]
     *
     * @param allArrowsInRound all arrows for a single [DatabaseShoot]. Cannot be empty.
     * [DatabaseArrowScore.shootId]s must match those in [toInsert]
     * @param toInsert arrows to insert. [DatabaseArrowScore.arrowNumber]s must be consecutive and all > 0.
     * [DatabaseArrowScore.shootId]s must match those in [allArrowsInRound]
     */
    @Transaction
    suspend fun insertEnd(
            allArrowsInRound: List<DatabaseArrowScore>,
            toInsert: List<DatabaseArrowScore>,
    ) {
        if (toInsert.isEmpty()) return

        /*
         * Check allArrowsInRound
         */
        require(allArrowsInRound.isNotEmpty()) { "Must provide arrows to shift" }
        require(
                allArrowsInRound.distinctBy { it.shootId }.size == 1
        ) { "allArrowsInRound cannot contain arrows from multiple shoots" }

        /*
         * Check toInsert
         */
        val minArrowNumber = toInsert.minOf { it.arrowNumber }
        require(minArrowNumber > 0) { "Arrow numbers must be > 0" }
        require(
                minArrowNumber <= allArrowsInRound.maxOf { it.arrowNumber }
        ) { "Insert must start within existing arrows indices" }
        require(
                (minArrowNumber until minArrowNumber + toInsert.size).toSet()
                        == toInsert.map { it.arrowNumber }.toSet()
        ) { "Arrow numbers for the arrows to insert must be consecutive" }

        /*
         * Execute
         */
        // Shift current arrowNumbers to make space for inserted ones
        val shiftedCurrentArrows = allArrowsInRound
                .filter { it.arrowNumber >= minArrowNumber }
                .map { it.copy(arrowNumber = it.arrowNumber + toInsert.size) }
        val currentArrowNumbers = allArrowsInRound.map { it.arrowNumber }
        val (updateArrows, insertArrows) = (toInsert + shiftedCurrentArrows)
                .partition { currentArrowNumbers.contains(it.arrowNumber) }
        debugLog("Updated: " + updateArrows.joinToString { it.toString() })
        debugLog("Inserted: " + insertArrows.joinToString { it.toString() })
        update(*updateArrows.toTypedArray())
        insert(*insertArrows.toTypedArray())
    }
}
