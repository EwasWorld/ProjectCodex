package eywa.projectcodex.database.repositories

import eywa.projectcodex.database.daos.RoundReferenceDao
import eywa.projectcodex.database.entities.RoundReference

/**
 * @see ArrowValuesRepo
 */
class RoundReferencesRepo(private val roundReferenceDao: RoundReferenceDao) {
    suspend fun insert(roundReference: RoundReference) {
        roundReferenceDao.insert(roundReference)
    }
}