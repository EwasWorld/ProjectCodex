package eywa.projectcodex.database.repositories

import androidx.lifecycle.LiveData
import eywa.projectcodex.database.daos.ArcherRoundDao
import eywa.projectcodex.database.entities.ArcherRound

/**
 * @see ArrowValuesRepo
 */
class ArcherRoundsRepo(private val archerRoundDao: ArcherRoundDao) {
    val maxId: LiveData<Int> = archerRoundDao.getMaxId()

    suspend fun insert(archerRound: ArcherRound) {
        archerRoundDao.insert(archerRound)
    }
}