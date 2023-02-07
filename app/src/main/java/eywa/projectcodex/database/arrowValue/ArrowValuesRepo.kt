package eywa.projectcodex.database.arrowValue

import androidx.lifecycle.LiveData
import androidx.room.Transaction

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
    @Transaction
    suspend fun deleteEnd(allArrowsInRound: List<ArrowValue>, firstArrowToDelete: Int, numberToDelete: Int) {
        require(numberToDelete > 0) { "numberToDelete must be > 0" }
        require(
                allArrowsInRound.distinctBy { it.archerRoundId }.size == 1
        ) { "allArrowsInRound cannot contain arrows from multiple archerRounds" }
        require(
                allArrowsInRound.any { it.arrowNumber == firstArrowToDelete }
        ) { "allArrowsInRound does not contain firstArrowToDelete" }

        val arrowsToDelete = allArrowsInRound
                .map { it.arrowNumber }
                .sorted()
                .dropWhile { it < firstArrowToDelete }
                .take(numberToDelete)

        arrowValueDao.deleteArrows(allArrowsInRound[0].archerRoundId, arrowsToDelete)

        allArrowsInRound
                .filter { it.arrowNumber >= firstArrowToDelete + numberToDelete }
                .map { ArrowValue(it.archerRoundId, it.arrowNumber - numberToDelete, it.score, it.isX) }
                .takeIf { it.isNotEmpty() }
                ?.let { update(*it.toTypedArray()) }
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
