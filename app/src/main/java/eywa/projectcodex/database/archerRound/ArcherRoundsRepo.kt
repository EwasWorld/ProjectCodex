package eywa.projectcodex.database.archerRound

import androidx.room.Transaction
import eywa.projectcodex.database.Filters
import eywa.projectcodex.model.FullArcherRoundInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ArcherRoundsRepo(
        private val archerRoundDao: ArcherRoundDao,
        private val shootDetailDao: ShootDetailDao,
        private val shootRoundDao: ShootRoundDao,
) {
    @Transaction
    fun getFullArcherRoundInfo(
            filters: Filters<ArcherRoundsFilter> = Filters(),
    ): Flow<List<DatabaseFullArcherRoundInfo>> {
        val datesFilter = filters.get<ArcherRoundsFilter.DateRange>()
        val roundsFilter = filters.get<ArcherRoundsFilter.Round>()

        return archerRoundDao.getAllFullArcherRoundInfo(
                filterPersonalBest = filters.contains<ArcherRoundsFilter.PersonalBests>(),
                fromDate = datesFilter?.from,
                toDate = datesFilter?.to,
                roundId = roundsFilter?.roundId,
                subTpeId = roundsFilter?.nonNullSubtypeId,
        ).map { rounds ->
            val pbs = rounds
                    .filter { it.isPersonalBest ?: false }
                    .groupBy { it.shootRound!!.roundId to (it.shootRound.roundSubTypeId ?: 1) }
                    .mapValues { it.value.size > 1 }

            rounds.map {
                val pbCount = pbs[it.shootRound?.roundId to (it.shootRound?.roundSubTypeId ?: 1)] ?: false
                it.copy(isTiedPersonalBest = pbCount)
            }
        }
    }

    fun getFullArcherRoundInfo(archerRoundIds: List<Int>) = archerRoundDao.getFullArcherRoundInfo(archerRoundIds)
    fun getFullArcherRoundInfo(archerRoundId: Int) = archerRoundDao.getFullArcherRoundInfo(archerRoundId)

    suspend fun insert(archerRound: ArcherRound) = archerRoundDao.insert(archerRound)

    @Transaction
    suspend fun insert(
            archerRound: ArcherRound,
            shootRound: DatabaseShootRound?,
            shootDetail: DatabaseShootDetail?,
    ): Long {
        require(
                listOfNotNull(
                        archerRound.archerRoundId,
                        shootRound?.archerRoundId,
                        shootDetail?.archerRoundId,
                ).distinct().size == 1
        ) { "Mismatched archerRoundIds" }
        require(shootRound == null || shootDetail == null) { "Clashing details/round" }

        val id = archerRoundDao.insert(archerRound)
        if (shootRound != null) shootRoundDao.insert(shootRound.copy(archerRoundId = id.toInt()))
        if (shootDetail != null) shootDetailDao.insert(shootDetail.copy(archerRoundId = id.toInt()))
        return id
    }

    suspend fun deleteRound(archerRoundId: Int) {
        archerRoundDao.deleteRound(archerRoundId)
    }

    suspend fun update(
            original: FullArcherRoundInfo,
            archerRound: ArcherRound,
            shootRound: DatabaseShootRound?,
            shootDetail: DatabaseShootDetail?,
    ) {
        require(
                listOfNotNull(
                        original.archerRound.archerRoundId,
                        archerRound.archerRoundId,
                        shootRound?.archerRoundId,
                        shootDetail?.archerRoundId,
                ).distinct().size == 1
        ) { "Mismatched archerRoundIds" }
        require(shootRound == null || shootDetail == null) { "Clashing details/round" }

        archerRoundDao.update(archerRound)

        if (shootRound != null) {
            if (original.shootRound != null) shootRoundDao.update(shootRound)
            else shootRoundDao.insert(shootRound)

            if (original.shootDetail != null) shootDetailDao.delete(archerRound.archerRoundId)
        }
        else if (shootDetail != null) {
            if (original.shootDetail != null) shootDetailDao.update(shootDetail)
            else shootDetailDao.insert(shootDetail)

            if (original.shootRound != null) shootRoundDao.delete(archerRound.archerRoundId)
        }
        else {
            shootRoundDao.delete(archerRound.archerRoundId)
            shootDetailDao.delete(archerRound.archerRoundId)
        }
    }
}
