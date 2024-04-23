package eywa.projectcodex.database.rounds

import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundEnabledFilters
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundFilter
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.UpdateType
import eywa.projectcodex.database.arrows.ArrowScoresRepo
import kotlinx.coroutines.flow.Flow

/**
 * @see ArrowScoresRepo
 */
class RoundRepo(
        private val roundDao: RoundDao,
        private val roundArrowCountDao: RoundArrowCountDao,
        private val roundSubTypeDao: RoundSubTypeDao,
        private val roundDistanceDao: RoundDistanceDao,
) {
    val fullRoundsInfo = roundDao.getAllRoundsFullInfo()
    val wa1440FullRoundInfo = roundDao.getFullRoundInfo(WA_1440_DEFAULT_ROUND_ID)

    fun fullRoundsInfo(filters: SelectRoundEnabledFilters): Flow<List<FullRoundInfo>> =
            roundDao.getAllRoundsFullInfo(
                    allIndoorOutdoor = filters.contains(SelectRoundFilter.OUTDOOR) == filters.contains(SelectRoundFilter.INDOOR),
                    isOutdoor = filters.contains(SelectRoundFilter.OUTDOOR),
                    allMetricImperial = filters.contains(SelectRoundFilter.METRIC) == filters.contains(SelectRoundFilter.IMPERIAL),
                    isMetric = filters.contains(SelectRoundFilter.METRIC),
            )

    constructor(db: ScoresRoomDatabase) : this(
            db.roundDao(),
            db.roundArrowCountDao(),
            db.roundSubTypeDao(),
            db.roundDistanceDao(),
    )

    /**
     * Updates rounds tables based on update items. WARNING: performs minimal checking for consistency.
     * @param updateItems maps a Round(ArrowCount/SubType/Distance) database item to an action. If the key is not of a
     * recognised type, it is ignored
     * @throws IllegalArgumentException if [updateItems] contains a [UpdateType.NEW] [Round],
     * but [updateItems] contains either no [UpdateType.NEW] [RoundArrowCount] or no [UpdateType.NEW] [RoundDistance]
     * with the same [Round.roundId]
     */
    suspend fun updateRounds(updateItems: Map<Any, UpdateType>) {
        val newRounds = updateItems.filter { it.value == UpdateType.NEW && it.key is Round }
        newRounds.forEach { newRound ->
            require(
                    updateItems.any { it.value == UpdateType.NEW && it.key is RoundArrowCount }
            ) { "$newRound doesn't have any arrow counts" }
            require(
                    updateItems.any { it.value == UpdateType.NEW && it.key is RoundDistance }
            ) { "$newRound doesn't have any distances" }
        }

        for (item in updateItems.entries.toList().sortedWith(UPDATE_ROUNDS_COMPARATOR)) {
            @Suppress("UNCHECKED_CAST")
            val dao = when (item.key::class) {
                Round::class -> roundDao
                RoundArrowCount::class -> roundArrowCountDao
                RoundSubType::class -> roundSubTypeDao
                RoundDistance::class -> roundDistanceDao
                else -> throw IllegalArgumentException("Unknown type")
            } as RoundTypeDao<Any>
            when (item.value) {
                UpdateType.NEW -> dao.insert(item.key)
                UpdateType.UPDATE -> dao.updateSingle(item.key)
                UpdateType.DELETE -> dao.deleteSingle(item.key)
            }
        }
    }


    companion object {
        const val WA_1440_DEFAULT_ROUND_ID = 8

        /**
         * Sort [Round]s to be at the start
         */
        private val UPDATE_ROUNDS_COMPARATOR = object : Comparator<Map.Entry<Any, UpdateType>> {
            override fun compare(entry0: Map.Entry<Any, UpdateType>?, entry1: Map.Entry<Any, UpdateType>?): Int {
                // Simple nulls
                if (entry0 == null && entry1 == null) return 0
                if (entry0 == null) return -1
                if (entry1 == null) return 1

                // Neither is a round
                if (entry0.key !is Round && entry1.key !is Round) {
                    return 0
                }

                // Both refer to the same round
                if (entry0.key is Round && entry1.key is Round) {
                    if ((entry0.key as Round).roundId == (entry1.key as Round).roundId) {
                        throw IllegalStateException("Should only be updating each round once")
                    }
                    else {
                        return 0
                    }
                }

                // Move the round to the start
                if (entry0.key is Round) return -1
                return 1
            }
        }
    }
}
