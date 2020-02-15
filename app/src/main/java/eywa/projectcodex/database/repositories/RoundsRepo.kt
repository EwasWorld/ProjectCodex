package eywa.projectcodex.database.repositories

import eywa.projectcodex.database.daos.*
import eywa.projectcodex.database.entities.Round
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundSubType
import eywa.projectcodex.database.entities.RoundSubTypeCount

/**
 * @see ArrowValuesRepo
 */
class RoundsRepo(
        private val roundDao: RoundDao,
        private val roundArrowCountDao: RoundArrowCountDao,
        private val roundSubTypeDao: RoundSubTypeDao,
        private val roundSubTypeCountDao: RoundSubTypeCountDao
) {
    suspend fun insertRound(round: Round) {
        roundDao.insert(round)
    }
    suspend fun insertArrowCount(roundArrowCount: RoundArrowCount) {
        roundArrowCountDao.insert(roundArrowCount)
    }
    suspend fun insertSubType(roundSubType: RoundSubType) {
        roundSubTypeDao.insert(roundSubType)
    }
    suspend fun insertSubTypeCount(roundSubTypeCount: RoundSubTypeCount) {
        roundSubTypeCountDao.insert(roundSubTypeCount)
    }
}