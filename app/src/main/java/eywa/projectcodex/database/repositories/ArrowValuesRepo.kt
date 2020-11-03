package eywa.projectcodex.database.repositories

import androidx.lifecycle.LiveData
import eywa.projectcodex.database.daos.ArrowValueDao
import eywa.projectcodex.database.entities.ArrowValue

/**
 * A Repository manages queries and allows you to use multiple backends. In the most common example, the Repository
 * implements the logic for deciding whether to fetch data from a network or use results cached in a local database.
 *  - https://codelabs.developers.google.com/codelabs/android-room-with-a-view-kotlin/#7
 *
 *  If DAOs have just one or two methods then repositories are often combined
 */
class ArrowValuesRepo(private val arrowValueDao: ArrowValueDao, private val archerRoundId: Int? = null) {
    val arrowValuesForRound: LiveData<List<ArrowValue>>? =
            archerRoundId?.let { arrowValueDao.getArrowValuesForRound(archerRoundId) }
    val allArrowValues: LiveData<List<ArrowValue>> = arrowValueDao.getAllArrowValues()

    suspend fun insert(vararg arrowValues: ArrowValue) {
        arrowValueDao.insert(*arrowValues)
    }

    suspend fun update(arrowValue: ArrowValue) {
        arrowValueDao.update(arrowValue)
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
    suspend fun deleteEnd(allArrows: List<ArrowValue>, firstArrowToDelete: Int, numberToDelete: Int) {
        check(archerRoundId != null) { "Must provide an archerRoundId" }
        require(allArrows.size > numberToDelete) { "allArrows must be larger than numberToDelete" }
        require(firstArrowToDelete >= 0 && numberToDelete > 0) {
            "Either firstArrowToDelete is too high or numberToDelete is too low"
        }
        val sortedArrows = allArrows.sortedBy { it.arrowNumber }
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
}