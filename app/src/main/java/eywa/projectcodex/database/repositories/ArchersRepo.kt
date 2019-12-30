package eywa.projectcodex.database.repositories

import eywa.projectcodex.database.daos.ArcherDao
import eywa.projectcodex.database.entities.Archer

/**
 * @see ArrowValuesRepo
 */
class ArchersRepo(private val archerDao: ArcherDao) {
    suspend fun insert(archer: Archer) {
        archerDao.insert(archer)
    }
}