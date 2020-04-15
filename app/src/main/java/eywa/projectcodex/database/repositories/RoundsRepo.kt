package eywa.projectcodex.database.repositories

import androidx.lifecycle.LiveData
import eywa.projectcodex.database.UpdateType
import eywa.projectcodex.database.daos.RoundArrowCountDao
import eywa.projectcodex.database.daos.RoundDao
import eywa.projectcodex.database.daos.RoundDistanceDao
import eywa.projectcodex.database.daos.RoundSubTypeDao
import eywa.projectcodex.database.entities.Round
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundSubType
import eywa.projectcodex.database.entities.RoundDistance

/**
 * @see ArrowValuesRepo
 */
class RoundsRepo(
        private val roundDao: RoundDao,
        private val roundArrowCountDao: RoundArrowCountDao,
        private val roundSubTypeDao: RoundSubTypeDao,
        private val roundDistanceDao: RoundDistanceDao
) {
    val rounds: LiveData<List<Round>> = roundDao.getAllRounds()
    val roundArrowCounts: LiveData<List<RoundArrowCount>> = roundArrowCountDao.getAllArrowCounts()
    val roundSubTypes: LiveData<List<RoundSubType>> = roundSubTypeDao.getAllSubTypes()
    val roundDistances: LiveData<List<RoundDistance>> = roundDistanceDao.getAllDistances()

    /**
     * Updates rounds tables based on update items. WARNING: performs minimal checking for consistency.
     * Will make sure to delete everything when a Round is deleted
     * @param updateItems maps a Round(ArrowCount/SubType/Distance) database item to an action. If the key is not of a
     * recognised type, it is ignored
     * @throws IllegalArgumentException if a Round object is given with the NEW action, but no NEW ArrowCounts or no NEW
     * Distances were given
     */
    suspend fun updateRounds(updateItems: Map<Any, UpdateType>) {
        val newRounds = updateItems.filter { it.value == UpdateType.NEW && it.key is Round }
        for (newRound in newRounds) {
            require(updateItems.filter { it.value == UpdateType.NEW && it.key is RoundArrowCount }.isNotEmpty()) { "$newRound doesn't have any arrow counts" }
            require(updateItems.filter { it.value == UpdateType.NEW && it.key is RoundDistance }.isNotEmpty()) { "$newRound doesn't have any distances" }
        }

        for (item in updateItems) {
            when (item.value) {
                UpdateType.NEW -> {
                    when (item.key::class) {
                        Round::class -> roundDao.insert(item.key as Round)
                        RoundArrowCount::class -> roundArrowCountDao.insert(item.key as RoundArrowCount)
                        RoundSubType::class -> roundSubTypeDao.insert(item.key as RoundSubType)
                        RoundDistance::class -> roundDistanceDao.insert(item.key as RoundDistance)
                    }
                }
                UpdateType.UPDATE -> {
                    when (item.key::class) {
                        Round::class -> roundDao.update(item.key as Round)
                        RoundArrowCount::class -> roundArrowCountDao.update(item.key as RoundArrowCount)
                        RoundSubType::class -> roundSubTypeDao.update(item.key as RoundSubType)
                        RoundDistance::class -> roundDistanceDao.update(item.key as RoundDistance)
                    }
                }
                UpdateType.DELETE -> {
                    when (item.key::class) {
                        Round::class -> deleteRound((item.key as Round).roundId)
                        RoundArrowCount::class -> {
                            val arrowCount = item.key as RoundArrowCount
                            roundArrowCountDao.delete(arrowCount.roundId, arrowCount.distanceNumber)
                        }
                        RoundSubType::class -> {
                            val subType = item.key as RoundSubType
                            roundSubTypeDao.delete(subType.roundId, subType.subTypeId)
                        }
                        RoundDistance::class -> {
                            val distance = item.key as RoundDistance
                            roundDistanceDao.delete(
                                    distance.roundId,
                                    distance.distanceNumber,
                                    distance.subTypeId
                            )
                        }
                    }
                }
            }
        }
    }

    suspend fun insertRound(
            round: Round,
            roundArrowCount: RoundArrowCount,
            roundSubType: RoundSubType,
            roundDistance: RoundDistance
    ) {
        roundDao.insert(round)
        roundArrowCountDao.insert(roundArrowCount)
        roundSubTypeDao.insert(roundSubType)
        roundDistanceDao.insert(roundDistance)
    }

    /**
     * Deletes all data associated with the given round ID
     */
    suspend fun deleteRound(roundId: Int) {
        roundDao.delete(roundId)
        roundArrowCountDao.deleteAll(roundId)
        roundSubTypeDao.deleteAll(roundId)
        roundDistanceDao.deleteAll(roundId)
    }

    suspend fun updateRounds(vararg rounds: Round) {
        roundDao.update(*rounds)
    }

    suspend fun updateRoundArrowCount(vararg roundArrowCounts: RoundArrowCount) {
        roundArrowCountDao.update(*roundArrowCounts)
    }

    suspend fun updateRoundSubType(vararg roundSubTypes: RoundSubType) {
        roundSubTypeDao.update(*roundSubTypes)
    }

    suspend fun updateRoundDistance(vararg roundDistances: RoundDistance) {
        roundDistanceDao.update(*roundDistances)
    }
}