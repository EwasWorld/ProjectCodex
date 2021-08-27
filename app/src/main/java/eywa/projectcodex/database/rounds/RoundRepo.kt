package eywa.projectcodex.database.rounds

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.UpdateType
import eywa.projectcodex.database.rounds.*
import java.util.concurrent.locks.ReentrantLock

/**
 * @see ArrowValuesRepo
 * TODO_CURRENT If a round is deleted, all archerRounds associated with it should also be stripped of their round info
 */
class RoundRepo(
        private val roundDao: RoundDao,
        private val roundArrowCountDao: RoundArrowCountDao,
        private val roundSubTypeDao: RoundSubTypeDao,
        private val roundDistanceDao: RoundDistanceDao
) {
    companion object {
        var repositoryWriteLock = ReentrantLock()

        @VisibleForTesting(otherwise = VisibleForTesting.NONE)
        fun reCreateLock() {
            repositoryWriteLock = ReentrantLock()
        }
    }

    val rounds: LiveData<List<Round>> = roundDao.getAllRounds()
    val roundArrowCounts: LiveData<List<RoundArrowCount>> = roundArrowCountDao.getAllArrowCounts()
    val roundSubTypes: LiveData<List<RoundSubType>> = roundSubTypeDao.getAllSubTypes()
    val roundDistances: LiveData<List<RoundDistance>> = roundDistanceDao.getAllDistances()

    constructor(db: ScoresRoomDatabase) : this(
            db.roundDao(),
            db.roundArrowCountDao(),
            db.roundSubTypeDao(),
            db.roundDistanceDao()
    )

    fun getArrowCountsForRound(roundId: Int): LiveData<List<RoundArrowCount>> {
        return roundArrowCountDao.getArrowCountsForRound(roundId)
    }

    fun getDistancesForRound(roundId: Int, subTypeId: Int?): LiveData<List<RoundDistance>> {
        return roundDistanceDao.getDistancesForRound(roundId, subTypeId ?: 1)
    }

    fun getRoundById(roundId: Int): LiveData<Round> {
        return roundDao.getRoundById(roundId)
    }

    /**
     * Updates rounds tables based on update items. WARNING: performs minimal checking for consistency.
     * Will make sure to delete everything when a Round is deleted
     * @param updateItems maps a Round(ArrowCount/SubType/Distance) database item to an action. If the key is not of a
     * recognised type, it is ignored
     * @throws IllegalArgumentException if a Round object is given with the NEW action, but no NEW ArrowCounts or no NEW
     * Distances were given
     */
    suspend fun updateRounds(updateItems: Map<Any, UpdateType>) {
        check(repositoryWriteLock.isHeldByCurrentThread) { "Repository write lock not acquired" }

        val newRounds = updateItems.filter { it.value == UpdateType.NEW && it.key is Round }
        for (newRound in newRounds) {
            require(updateItems.filter { it.value == UpdateType.NEW && it.key is RoundArrowCount }
                    .isNotEmpty()) { "$newRound doesn't have any arrow counts" }
            require(updateItems.filter { it.value == UpdateType.NEW && it.key is RoundDistance }
                    .isNotEmpty()) { "$newRound doesn't have any distances" }
        }

        for (item in updateItems.entries.toList().sortedWith(UpdateRoundsComparator())) {
            if (item.value == UpdateType.DELETE) {
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
            else {
                @Suppress("UNCHECKED_CAST")
                val dao = when (item.key::class) {
                    Round::class -> roundDao
                    RoundArrowCount::class -> roundArrowCountDao
                    RoundSubType::class -> roundSubTypeDao
                    RoundDistance::class -> roundDistanceDao
                    else -> throw IllegalArgumentException("Unknown type")
                } as RoundTypeDao<Any>
                if (item.value == UpdateType.NEW) {
                    dao.insert(item.key)
                }
                else if (item.value == UpdateType.UPDATE) {
                    dao.updateSingle(item.key)
                }
            }
        }
    }

    /**
     * Deletes all data associated with the given round ID
     */
    suspend fun deleteRound(roundId: Int) {
        check(repositoryWriteLock.isHeldByCurrentThread) { "Repository write lock not acquired" }

        roundDao.delete(roundId)
        roundArrowCountDao.deleteAll(roundId)
        roundSubTypeDao.deleteAll(roundId)
        roundDistanceDao.deleteAll(roundId)
    }

    /**
     * Sort first by roundId then put Round at the start if [UpdateType] is [UpdateType.DELETE] or the end if not
     */
    private class UpdateRoundsComparator : Comparator<Map.Entry<Any, UpdateType>> {
        override fun compare(o1: Map.Entry<Any, UpdateType>?, o2: Map.Entry<Any, UpdateType>?): Int {
            // Simple nulls
            if (o1 == null && o2 == null) {
                return 0
            }
            if (o1 == null) {
                return -1
            }
            if (o2 == null) {
                return 1
            }

            // Compare roundId
            val roundIdComparison = getRoundId(o1.key).compareTo(getRoundId(o2.key))
            if (roundIdComparison != 0) {
                return roundIdComparison
            }

            // Compare class
            if (o1.key !is Round && o2.key !is Round) {
                return 0
            }
            if (o1.key is Round && o2.key is Round) {
                throw IllegalStateException("Should only be updating each round once")
            }

            // If Round, move delete to start, others to end
            if (o1.key is Round) {
                if (o1.value == UpdateType.DELETE) {
                    return -1
                }
                return 1
            }
            // o2 is Round
            if (o2.value == UpdateType.DELETE) {
                return 1
            }
            return -1
        }

        fun getRoundId(item: Any): Int {
            return when (item::class) {
                Round::class -> (item as Round).roundId
                RoundArrowCount::class -> (item as RoundArrowCount).roundId
                RoundSubType::class -> (item as RoundSubType).roundId
                RoundDistance::class -> (item as RoundDistance).roundId
                else -> throw IllegalArgumentException("Not a round object")
            }
        }
    }
}