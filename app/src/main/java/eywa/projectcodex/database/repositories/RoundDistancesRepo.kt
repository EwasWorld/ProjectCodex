package eywa.projectcodex.database.repositories

import eywa.projectcodex.database.daos.RoundDistanceDao
import eywa.projectcodex.database.entities.RoundDistance

/**
 * @see ArrowValuesRepo
 */
class RoundDistancesRepo(private val roundDistanceDao: RoundDistanceDao) {
    suspend fun insert(roundDistance: RoundDistance) {
        roundDistanceDao.insert(roundDistance)
    }
}