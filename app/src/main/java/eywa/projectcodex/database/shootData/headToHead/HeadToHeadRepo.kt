package eywa.projectcodex.database.shootData.headToHead

import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

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
        headToHeadMatchDao.incrementMatchNumber(
                shootId = shootId,
                matchNumbersAboveAndIncluding = matchNumber,
                increment = -1,
        )
        headToHeadDetailDao.incrementMatchNumber(
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
        headToHeadDetailDao.incrementSetNumber(
                shootId = shootId,
                matchNumber = matchNumber,
                setNumbersAboveAndIncluding = setNumber,
                increment = -1,
        )
    }

    suspend fun insert(headToHead: DatabaseHeadToHead) {
        headToHeadDao.insert(headToHead)
    }

    suspend fun insert(match: DatabaseHeadToHeadMatch) {
        val hasExistingSet = headToHeadMatchDao
                .getMatchCount(shootId = match.shootId, matchNumber = match.matchNumber)
                .first()
                .let { it != 0 }
        if (hasExistingSet) {
            headToHeadMatchDao.incrementMatchNumber(
                    shootId = match.shootId,
                    matchNumbersAboveAndIncluding = match.matchNumber,
                    increment = 1,
            )
            headToHeadDetailDao.incrementMatchNumber(
                    shootId = match.shootId,
                    matchNumbersAboveAndIncluding = match.matchNumber,
                    increment = 1,
            )
        }

        headToHeadMatchDao.insert(match)
    }

    @Transaction
    suspend fun insert(vararg details: DatabaseHeadToHeadDetail, isShootOffWin: Boolean? = null) {
        check(details.isNotEmpty())
        check(
                details.distinctBy { "${it.shootId}-${it.matchNumber}-${it.setNumber}" }.size == 1,
        ) { "Multiple set's data found" }

        val firstItem = details[0]
        val hasExistingSet = headToHeadDetailDao
                .getDetailsCount(
                        shootId = firstItem.shootId,
                        matchNumber = firstItem.matchNumber,
                        setNumber = firstItem.setNumber,
                )
                .first()
                .let { it != 0 }

        if (hasExistingSet) {
            headToHeadDetailDao.incrementSetNumber(
                    shootId = firstItem.shootId,
                    matchNumber = firstItem.matchNumber,
                    setNumbersAboveAndIncluding = firstItem.setNumber,
                    increment = 1,
            )
        }
        headToHeadDetailDao.insert(*details)

        if (isShootOffWin != null) {
            headToHeadMatchDao.setIsShootOffWin(firstItem.shootId, firstItem.matchNumber, isShootOffWin)
        }
    }

    @Transaction
    suspend fun update(
            newDetails: List<DatabaseHeadToHeadDetail>,
            oldDetails: List<DatabaseHeadToHeadDetail>,
            isShootOffWin: Boolean? = null,
    ) {
        check(newDetails.isNotEmpty()) { "No details found" }
        check(newDetails.distinctBy { it.type }.size == newDetails.size) { "Duplicate types found" }
        check(
                oldDetails.plus(newDetails).distinctBy { "${it.shootId}-${it.matchNumber}-${it.setNumber}" }.size == 1,
        ) { "Multiple set's data found" }
        check(oldDetails.all { it.headToHeadArrowScoreId != 0 }) { "Old details not from database" }

        val remove = oldDetails.toMutableList()
        val insert = mutableListOf<DatabaseHeadToHeadDetail>()
        val update = mutableListOf<DatabaseHeadToHeadDetail>()

        newDetails.forEach { new ->
            val old = oldDetails.find { it.type == new.type }
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
        if (isShootOffWin != null) {
            val firstItem = newDetails[0]
            headToHeadMatchDao.setIsShootOffWin(firstItem.shootId, firstItem.matchNumber, isShootOffWin)
        }
    }

    suspend fun update(match: DatabaseHeadToHeadMatch) {
        headToHeadMatchDao.update(match)
    }

    suspend fun update(h2h: DatabaseHeadToHead) {
        headToHeadDao.update(h2h)
    }
}
