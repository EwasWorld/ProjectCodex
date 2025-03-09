package eywa.projectcodex.database.shootData.headToHead

import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.math.abs

class HeadToHeadRepo(
        private val headToHeadDao: HeadToHeadDao,
        private val headToHeadMatchDao: HeadToHeadMatchDao,
        private val headToHeadDetailDao: HeadToHeadDetailDao,
) {
    fun get(shootId: Int): Flow<DatabaseFullHeadToHead?> = headToHeadDao.getFullHeadToHead(shootId)

    @Transaction
    suspend fun delete(shootId: Int) {
        headToHeadDao.delete(shootId)
        headToHeadMatchDao.delete(shootId)
        headToHeadDetailDao.deleteAll(shootId)
    }

    @Transaction
    suspend fun delete(shootId: Int, matchNumber: Int) {
        headToHeadMatchDao.delete(shootId = shootId, matchNumber = matchNumber)
        headToHeadDetailDao.delete(shootId = shootId, matchNumber = matchNumber)
        incrementMatchNumbersIfExist(
                shootId = shootId,
                matchNumbersAboveAndIncluding = matchNumber,
                increment = -1,
        )
    }

    suspend fun deleteSets(shootId: Int, matchNumber: Int) {
        headToHeadDetailDao.delete(shootId = shootId, matchNumber = matchNumber)
    }

    @Transaction
    suspend fun delete(shootId: Int, matchNumber: Int, setNumber: Int) {
        headToHeadDetailDao.delete(shootId = shootId, matchNumber = matchNumber, setNumber = setNumber)
        incrementSetNumbersIfExist(
                shootId = shootId,
                matchNumber = matchNumber,
                setNumbersAboveAndIncluding = setNumber,
                increment = -1,
        )
    }

    suspend fun insert(headToHead: DatabaseHeadToHead) {
        headToHeadDao.insert(headToHead)
    }

    @Transaction
    suspend fun insert(match: DatabaseHeadToHeadMatch) {
        incrementMatchNumbersIfExist(
                shootId = match.shootId,
                matchNumbersAboveAndIncluding = match.matchNumber,
                increment = 1,
        )
        headToHeadMatchDao.insert(match)
    }

    @Transaction
    suspend fun insert(vararg details: DatabaseHeadToHeadDetail) {
        check(details.isNotEmpty())
        check(
                details.distinctBy { "${it.shootId}-${it.matchNumber}-${it.setNumber}" }.size == 1,
        ) { "Multiple set's data found" }

        val firstItem = details[0]
        incrementSetNumbersIfExist(
                shootId = firstItem.shootId,
                matchNumber = firstItem.matchNumber,
                setNumbersAboveAndIncluding = firstItem.setNumber,
                increment = 1,
        )

        headToHeadDetailDao.insert(*details)
    }

    @Transaction
    suspend fun update(
            newDetails: List<DatabaseHeadToHeadDetail>,
            oldDetails: List<DatabaseHeadToHeadDetail>,
    ) {
        check(newDetails.isNotEmpty()) { "No details found" }
        check(
                newDetails.distinctBy { "${it.type}-${it.arrowNumber}" }.size == newDetails.size,
        ) { "Duplicate types found" }
        check(
                oldDetails.plus(newDetails).distinctBy { "${it.shootId}-${it.matchNumber}-${it.setNumber}" }.size == 1,
        ) { "Multiple set's data found" }
        check(oldDetails.all { it.headToHeadArrowScoreId != 0 }) { "Old details not from database" }

        val remove = oldDetails.toMutableList()
        val insert = mutableListOf<DatabaseHeadToHeadDetail>()
        val update = mutableListOf<DatabaseHeadToHeadDetail>()

        newDetails.forEach { new ->
            val old = oldDetails.find { it.type == new.type && it.arrowNumber == new.arrowNumber }
            if (old == null) insert.add(new.copy(headToHeadArrowScoreId = 0))
            else {
                remove.remove(old)
                update.add(new.copy(headToHeadArrowScoreId = old.headToHeadArrowScoreId))
            }
        }

        check(remove.size + update.size == oldDetails.size) { "Old details not properly converted" }
        check(insert.size + update.size == newDetails.size) { "New details not properly converted" }

        remove.forEach {
            headToHeadDetailDao.delete(it.headToHeadArrowScoreId)
        }
        headToHeadDetailDao.update(*update.toTypedArray())
        headToHeadDetailDao.insert(*insert.toTypedArray())
    }

    suspend fun update(match: DatabaseHeadToHeadMatch) {
        headToHeadMatchDao.update(match)
    }

    suspend fun update(h2h: DatabaseHeadToHead) {
        headToHeadDao.update(h2h)
    }

    /**
     * Cannot do in SQL
     *
     * @see incrementMatchNumbersIfExist
     */
    private suspend fun incrementSetNumbersIfExist(
            shootId: Int,
            matchNumber: Int,
            setNumbersAboveAndIncluding: Int,
            increment: Int,
    ) {
        val sort: List<DatabaseHeadToHeadDetail>.() -> List<DatabaseHeadToHeadDetail> = {
            if (increment > 0) sortedByDescending { it.setNumber } else sortedBy { it.setNumber }
        }

        headToHeadDetailDao
                .getSetNumberGreaterThanOrEqualTo(
                        shootId = shootId,
                        matchNumber = matchNumber,
                        setNumbersAboveAndIncluding = setNumbersAboveAndIncluding,
                )
                .first()
                .takeIf { it.isNotEmpty() }
                ?.map { it.copy(setNumber = it.setNumber + increment) }
                ?.sort()
                ?.let { headToHeadDetailDao.update(*it.toTypedArray()) }
    }

    /**
     * Cannot use SQL `UPDATE WHERE matchNumber >=` because it causes duplicate shootId-matchNumber combinations,
     * causing a constraint violation. Could work around this for the matches table by deferring the foreign key,
     * (cannot do this for the details table as it's an index). Wasn't happy with any of those solutions though so
     * instead this function uses the standard dao.update methods and manually sets the order of the updates
     */
    private suspend fun incrementMatchNumbersIfExist(
            shootId: Int,
            matchNumbersAboveAndIncluding: Int,
            increment: Int,
    ) {
        val sort: List<DatabaseHeadToHeadMatch>.() -> List<DatabaseHeadToHeadMatch> = {
            if (increment > 0) sortedByDescending { it.matchNumber } else sortedBy { it.matchNumber }
        }
        val oldMatches = headToHeadMatchDao
                .getMatchNumberGreaterThanOrEqualTo(
                        shootId = shootId,
                        matchNumbersAboveAndIncluding = matchNumbersAboveAndIncluding,
                )
                .first()
                .takeIf { it.isNotEmpty() }
                ?.sort()

        // matchNumber is part of the primary key so can't just use update as details table can
        if (oldMatches != null) {
            val newMatches = oldMatches
                    .map { it.copy(matchNumber = it.matchNumber + increment) }
                    .sort()
            val insert = newMatches.take(abs(increment))
            val update = newMatches.drop(abs(increment))
            val delete = oldMatches.takeLast(abs(increment))

            update.let {
                headToHeadMatchDao.update(*it.toTypedArray())
            }
            headToHeadMatchDao.insert(*insert.toTypedArray())
            headToHeadMatchDao.delete(*delete.toTypedArray())
        }

        headToHeadDetailDao
                .getMatchNumberGreaterThanOrEqualTo(
                        shootId = shootId,
                        matchNumbersAboveAndIncluding = matchNumbersAboveAndIncluding,
                )
                .first()
                .takeIf { it.isNotEmpty() }
                ?.map { it.copy(matchNumber = it.matchNumber + increment) }
                ?.sortedWith { t, t2 ->
                    val result = t.matchNumber.compareTo(t2.matchNumber).takeIf { it != 0 }
                            ?: t.setNumber.compareTo(t2.setNumber)

                    if (increment > 0) -result else result
                }
                ?.let { headToHeadDetailDao.update(*it.toTypedArray()) }
    }
}
