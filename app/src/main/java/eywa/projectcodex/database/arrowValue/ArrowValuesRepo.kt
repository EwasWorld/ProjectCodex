package eywa.projectcodex.database.arrowValue

import androidx.lifecycle.LiveData

/**
 * A Repository manages queries and allows you to use multiple backends. In the most common example, the Repository
 * implements the logic for deciding whether to fetch data from a network or use results cached in a local database.
 *  - https://codelabs.developers.google.com/codelabs/android-room-with-a-view-kotlin/#7
 *
 *  If DAOs have just one or two methods then repositories are often combined
 */
class ArrowValuesRepo(private val arrowValueDao: ArrowValueDao) {
    val allArrowValues: LiveData<List<ArrowValue>> = arrowValueDao.getAllArrowValues()

    fun getArrowValuesForRound(archerRoundId: Int): LiveData<List<ArrowValue>> =
            arrowValueDao.getArrowValuesForRound(archerRoundId)

    suspend fun insert(vararg arrowValues: ArrowValue) {
        arrowValueDao.insert(*arrowValues)
    }

    suspend fun update(vararg arrowValues: ArrowValue) {
        arrowValueDao.update(*arrowValues)
    }

    suspend fun deleteAll() {
        arrowValueDao.deleteAll()
    }

    suspend fun deleteRoundsArrows(archerRoundId: Int) {
        arrowValueDao.deleteRoundsArrows(archerRoundId)
    }

    /**
     * Update [firstArrowToDelete] to end's arrow values to be that of [firstArrowToDelete] - [numberToDelete]. This
     * will overwrite the arrows to be deleted. Then delete [numberToDelete] off the end (as they're now duplicated)
     * @throws IllegalArgumentException if allArrows is null or empty, or if [firstArrowToDelete] and [numberToDelete]
     * results in an out of range arrowNumber
     * @throws IllegalStateException if this repo was created without an archerRoundId
     */
    suspend fun deleteEnd(allArrowsInRound: List<ArrowValue>, firstArrowToDelete: Int, numberToDelete: Int) {
        val distinctByArcherRoundIds = allArrowsInRound.distinctBy { it.archerRoundId }
        require(distinctByArcherRoundIds.size == 1) { "allArrowsInRound cannot contain arrows from multiple archerRounds" }
        require(allArrowsInRound.size >= numberToDelete) { "allArrowsInRound must be larger than numberToDelete" }
        require(firstArrowToDelete >= 0 && numberToDelete > 0) {
            "Either firstArrowToDelete is too high or numberToDelete is too low"
        }

        val archerRoundId = distinctByArcherRoundIds[0].archerRoundId
        if (allArrowsInRound.size == numberToDelete) {
            arrowValueDao.deleteRoundsArrows(archerRoundId)
            return
        }

        val sortedArrows = allArrowsInRound.sortedBy { it.arrowNumber }
        val maxArrowNumber = sortedArrows.last().arrowNumber
        // e.g. firstArrow 0 + deleteCount 6 - 1 == maxNumber 5
        require(firstArrowToDelete + numberToDelete - 1 <= maxArrowNumber) {
            "Either firstArrowToDelete is too high or numberToDelete is too high"
        }

        var arrowsToUpdate = sortedArrows.filter { it.arrowNumber >= firstArrowToDelete }
        arrowsToUpdate = arrowsToUpdate.subList(numberToDelete, arrowsToUpdate.size)
                .map { ArrowValue(it.archerRoundId, it.arrowNumber - numberToDelete, it.score, it.isX) }

        // Deleting the LAST arrows because all arrows have been shifted down and last arrows are now duplicates
        arrowValueDao.deleteEndTransaction(
                /* Delete */
                archerRoundId,
                maxArrowNumber - numberToDelete + 1, // e.g. delete all 6 arrows: 5 - 6 + 1
                maxArrowNumber + 1, // e.g. delete all 6 arrows: 5 + 1
                /* Update */
                *arrowsToUpdate.toTypedArray()
        )
    }

    suspend fun insertEnd(allArrowsInRound: List<ArrowValue>, toInsert: List<ArrowValue>) {
        if (toInsert.isEmpty()) return
        val distinctByArcherRoundIds = allArrowsInRound.distinctBy { it.archerRoundId }
        require(distinctByArcherRoundIds.size == 1) { "allArrowsInRound cannot contain arrows from multiple archerRounds" }
        require(allArrowsInRound.isNotEmpty()) { "Must provide arrows to shift" }
        val archerRoundId = distinctByArcherRoundIds[0].archerRoundId

        /*
         * Check arrow numbers
         */
        val minArrowNumber = toInsert.minOf { it.arrowNumber }
        require(minArrowNumber >= 1) { "Arrow numbers must be >= 1" }
        require(minArrowNumber < allArrowsInRound.maxOf { it.arrowNumber }) {
            "Insert must start within existing arrows indices"
        }
        require(
                (minArrowNumber until minArrowNumber + toInsert.size).toSet()
                        == toInsert.map { it.arrowNumber }.toSet()
        ) { "Arrow numbers for the arrows to insert must be consecutive" }

        // Shift other arrowNumbers to make space for inserted ones
        val allArrowsReady =
                toInsert.plus(allArrowsInRound.filter { it.arrowNumber >= minArrowNumber }.map {
                    ArrowValue(archerRoundId, it.arrowNumber + toInsert.size, it.score, it.isX)
                })

        val currentArrowNumbers = allArrowsInRound.map { it.arrowNumber }
        val updateArrows = allArrowsReady.filter { currentArrowNumbers.contains(it.arrowNumber) }
        val insertArrows = allArrowsReady.filter { !currentArrowNumbers.contains(it.arrowNumber) }

        arrowValueDao.updateAndInsert(updateArrows, insertArrows)
    }
}