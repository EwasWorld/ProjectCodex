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
class ArrowValuesRepo(private val arrowValueDao: ArrowValueDao, archerRoundId: Int? = null) {
    val arrowValuesForRound: LiveData<List<ArrowValue>>? =
        archerRoundId?.let { arrowValueDao.getArrowValuesForRound(archerRoundId) }
    val allArrowValues: LiveData<List<ArrowValue>> = arrowValueDao.getAllArrowValues()

    suspend fun insert(arrowValue: ArrowValue) {
        arrowValueDao.insert(arrowValue)
    }

    suspend fun deleteAll() {
        arrowValueDao.deleteAll()
    }

    suspend fun deleteRoundsArrows(archerRoundId: Int) {
        arrowValueDao.deleteRoundsArrows(archerRoundId)
    }
}