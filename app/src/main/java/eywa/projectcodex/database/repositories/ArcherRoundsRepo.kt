package eywa.projectcodex.database.repositories

import eywa.projectcodex.database.daos.ArcherRoundDao
import eywa.projectcodex.database.entities.ArcherRound

/**
 * @see ArrowValuesRepo
 */
class ArcherRoundsRepo(private val archerRoundDao: ArcherRoundDao) {
    suspend fun insert(archerRound: ArcherRound) {
        archerRoundDao.insert(archerRound)
    }
}