package eywa.projectcodex.database.archerRound

import androidx.lifecycle.LiveData
import androidx.room.Transaction
import eywa.projectcodex.database.Filters
import kotlinx.coroutines.flow.*

/**
 * @see ArrowValuesRepo
 */
class ArcherRoundsRepo(
        private val archerRoundDao: ArcherRoundDao,
) {
    @Transaction
    fun getFullArcherRoundInfo(
            filters: Filters<ArcherRoundsFilter> = Filters(),
    ): Flow<List<DatabaseFullArcherRoundInfo>> {
        val datesFilter = filters.get<ArcherRoundsFilter.DateRange>()
        val roundsFilter = filters.get<ArcherRoundsFilter.Round>()

        return archerRoundDao.getAllFullArcherRoundInfo(
                filterPersonalBest = filters.contains<ArcherRoundsFilter.PersonalBests>(),
                fromDate = datesFilter?.from?.time,
                toDate = datesFilter?.to?.time,
                roundId = roundsFilter?.roundId,
                subTpeId = roundsFilter?.nonNullSubtypeId,
        ).map { rounds ->
            val pbs = rounds
                    .filter { it.isPersonalBest ?: false }
                    .groupBy { it.archerRound.roundId!! to (it.archerRound.roundSubTypeId ?: 1) }
                    .mapValues { it.value.size > 1 }

            rounds.map {
                val pbCount = pbs[it.archerRound.roundId to (it.archerRound.roundSubTypeId ?: 1)] ?: false
                it.copy(isTiedPersonalBest = pbCount)
            }
        }
    }

    fun getFullArcherRoundInfo(archerRoundId: Int) = archerRoundDao.getFullArcherRoundInfo(archerRoundId)

    fun getArcherRound(archerRoundId: Int): LiveData<ArcherRound> {
        return archerRoundDao.getArcherRoundById(archerRoundId)
    }

    suspend fun insert(archerRound: ArcherRound) = archerRoundDao.insert(archerRound)

    suspend fun deleteRound(archerRoundId: Int) {
        archerRoundDao.deleteRound(archerRoundId)
    }

    suspend fun update(vararg archerRounds: ArcherRound) {
        archerRoundDao.update(*archerRounds)
    }
}
