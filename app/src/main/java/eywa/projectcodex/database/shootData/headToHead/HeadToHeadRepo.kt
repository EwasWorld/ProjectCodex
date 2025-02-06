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
    }

    suspend fun deleteSets(shootId: Int, matchNumber: Int) {
        headToHeadDetailDao.delete(shootId = shootId, matchNumber = matchNumber)
    }

    @Transaction
    suspend fun delete(shootId: Int, matchNumber: Int, setNumber: Int) {
        headToHeadDetailDao.delete(shootId = shootId, matchNumber = matchNumber, setNumber = setNumber)
        headToHeadDetailDao.incrementSetNumber(shootId, matchNumber, setNumber, -1)
    }

    suspend fun insert(headToHead: DatabaseHeadToHead) {
        headToHeadDao.insert(headToHead)
    }

    suspend fun insert(match: DatabaseHeadToHeadMatch) {
        headToHeadMatchDao.insert(match)
    }

    @Transaction
    suspend fun insert(vararg details: DatabaseHeadToHeadDetail) {
        check(details.isNotEmpty())
        check(
                details.distinctBy { "${it.shootId}-${it.matchNumber}-${it.setNumber}" }.size == 1,
        ) { "Multiple set's data found" }

        val firstItem = details[0]
        val hasExistingSet = headToHeadDetailDao
                .getDetailsCount(firstItem.shootId, firstItem.matchNumber, firstItem.setNumber)
                .first()
                .let { it != 0 }

        if (hasExistingSet) {
            headToHeadDetailDao.incrementSetNumber(firstItem.shootId, firstItem.matchNumber, firstItem.setNumber, 1)
        }
        headToHeadDetailDao.insert(*details)
    }

    @Transaction
    suspend fun update(
            newDetails: List<DatabaseHeadToHeadDetail>,
            oldDetails: List<DatabaseHeadToHeadDetail>,
    ) {
        check(newDetails.distinctBy { it.type }.size == newDetails.size) { "Duplicate types found" }
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
    }

    suspend fun update(match: DatabaseHeadToHeadMatch) {
        headToHeadMatchDao.update(match)
    }

    suspend fun update(h2h: DatabaseHeadToHead) {
        headToHeadDao.update(h2h)
    }
}
