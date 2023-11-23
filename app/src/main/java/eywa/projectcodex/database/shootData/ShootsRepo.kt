package eywa.projectcodex.database.shootData

import androidx.room.Transaction
import eywa.projectcodex.database.Filters
import eywa.projectcodex.database.arrows.ArrowCounterRepo
import eywa.projectcodex.database.arrows.DatabaseArrowCounter
import eywa.projectcodex.model.FullShootInfo
import kotlinx.coroutines.flow.Flow

class ShootsRepo(
        private val shootDao: ShootDao,
        private val shootDetailDao: ShootDetailDao,
        private val shootRoundDao: ShootRoundDao,
        private val arrowCounterRepo: ArrowCounterRepo,
) {
    @Transaction
    fun getMostRecentShootsForRound(count: Int, roundId: Int, subTypeId: Int = 1) =
            shootDao.getMostRecentShootsForRound(count, roundId, subTypeId)

    @Transaction
    fun getRoundPb(roundId: Int, subTypeId: Int = 1) =
            shootDao.getRoundPb(roundId, subTypeId)

    @Transaction
    fun getFullShootInfo(
            filters: Filters<ShootFilter> = Filters(),
    ): Flow<List<DatabaseFullShootInfo>> {
        val datesFilter = filters.get<ShootFilter.DateRange>()
        val roundsFilter = filters.get<ShootFilter.Round>()

        return shootDao.getAllFullShootInfo(
                filterPersonalBest = filters.contains<ShootFilter.PersonalBests>(),
                fromDate = datesFilter?.from,
                toDate = datesFilter?.to,
                roundId = roundsFilter?.roundId,
                subTpeId = roundsFilter?.nonNullSubtypeId,
        )
    }

    fun getFullShootInfo(shootIds: List<Int>) = shootDao.getFullShootInfo(shootIds)
    fun getFullShootInfo(shootId: Int) = shootDao.getFullShootInfo(shootId)

    suspend fun insert(shoot: DatabaseShoot) = shootDao.insert(shoot)

    @Transaction
    suspend fun insert(
            shoot: DatabaseShoot,
            shootRound: DatabaseShootRound?,
            shootDetail: DatabaseShootDetail?,
            isScoringNotCounting: Boolean,
    ): Long {
        require(
                listOfNotNull(
                        shoot.shootId,
                        shootRound?.shootId,
                        shootDetail?.shootId,
                ).distinct().size == 1
        ) { "Mismatched shootIds" }
        require(shootRound == null || shootDetail == null) { "Clashing details/round" }

        val id = shootDao.insert(shoot)
        if (shootRound != null) shootRoundDao.insert(shootRound.copy(shootId = id.toInt()))
        if (shootDetail != null) shootDetailDao.insert(shootDetail.copy(shootId = id.toInt()))
        if (!isScoringNotCounting) arrowCounterRepo.insert(DatabaseArrowCounter(id.toInt(), 0))
        return id
    }

    suspend fun deleteRound(shootId: Int) {
        shootDao.deleteRound(shootId)
    }

    @Transaction
    suspend fun update(
            original: FullShootInfo,
            shoot: DatabaseShoot,
            shootRound: DatabaseShootRound?,
            shootDetail: DatabaseShootDetail?,
            isScoringNotCounting: Boolean,
    ) {
        require(
                listOfNotNull(
                        original.shoot.shootId,
                        shoot.shootId,
                        shootRound?.shootId,
                        shootDetail?.shootId,
                ).distinct().size == 1
        ) { "Mismatched shootIds" }
        require(shootRound == null || shootDetail == null) { "Clashing details/round" }
        require(
                isScoringNotCounting == (original.arrowCounter == null) || original.arrowsShot == 0
        ) { "Cannot change type if arrows have been shot" }

        shootDao.update(shoot)

        if (shootRound != null) {
            if (original.shootRound != null) shootRoundDao.update(shootRound)
            else shootRoundDao.insert(shootRound)

            if (original.shootDetail != null) shootDetailDao.delete(shoot.shootId)
        }
        else if (shootDetail != null) {
            if (original.shootDetail != null) shootDetailDao.update(shootDetail)
            else shootDetailDao.insert(shootDetail)

            if (original.shootRound != null) shootRoundDao.delete(shoot.shootId)
        }
        else {
            shootRoundDao.delete(shoot.shootId)
            shootDetailDao.delete(shoot.shootId)
        }

        if (isScoringNotCounting && original.arrowCounter != null) {
            arrowCounterRepo.delete(original.arrowCounter)
        }
        else if (!isScoringNotCounting && original.arrowCounter == null) {
            arrowCounterRepo.insert(DatabaseArrowCounter(original.shoot.shootId, 0))
        }
    }
}
