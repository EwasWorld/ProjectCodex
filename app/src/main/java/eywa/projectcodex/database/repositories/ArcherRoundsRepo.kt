package eywa.projectcodex.database.repositories

import androidx.lifecycle.LiveData
import eywa.projectcodex.database.daos.ArcherRoundDao
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.database.entities.ArcherRoundWithName
import eywa.projectcodex.database.entities.Round

/**
 * @see ArrowValuesRepo
 */
class ArcherRoundsRepo(private val archerRoundDao: ArcherRoundDao) {
    val maxId: LiveData<Int> = archerRoundDao.getMaxId()

    val allArcherRoundsWithName: LiveData<List<ArcherRoundWithName>> =
            archerRoundDao.getAllArcherRoundsWithName()

    fun getRoundInfo(archerRoundId: Int): LiveData<Round> {
        return archerRoundDao.getRoundInfo(archerRoundId)
    }

    suspend fun insert(archerRound: ArcherRound) {
        archerRoundDao.insert(archerRound)
    }

    suspend fun deleteRound(archerRoundId: Int) {
        archerRoundDao.deleteRound(archerRoundId)
    }
}