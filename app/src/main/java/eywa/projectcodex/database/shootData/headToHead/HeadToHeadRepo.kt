package eywa.projectcodex.database.shootData.headToHead

import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

class HeadToHeadRepo(
        private val headToHeadDao: HeadToHeadDao,
        private val headToHeadHeatDao: HeadToHeadHeatDao,
        private val headToHeadDetailDao: HeadToHeadDetailDao,
) {
    fun get(shootId: Int): Flow<DatabaseFullHeadToHead?> = headToHeadDao.getFullHeadToHead(shootId)

    @Transaction
    suspend fun delete(shootId: Int) {
        headToHeadDao.delete(shootId)
        headToHeadHeatDao.delete(shootId)
        headToHeadDetailDao.delete(shootId)
    }

    @Transaction
    suspend fun delete(shootId: Int, matchNumber: Int) {
        headToHeadHeatDao.delete(shootId = shootId, matchNumber = matchNumber)
        headToHeadDetailDao.delete(shootId = shootId, matchNumber = matchNumber)
    }

    @Transaction
    suspend fun delete(shootId: Int, matchNumber: Int, setNumber: Int) {
        headToHeadDetailDao.delete(shootId = shootId, matchNumber = matchNumber, setNumber = setNumber)
    }

    suspend fun insert(headToHead: DatabaseHeadToHead) {
        headToHeadDao.insert(headToHead)
    }

    suspend fun insert(heat: DatabaseHeadToHeadHeat) {
        headToHeadHeatDao.insert(heat)
    }

    suspend fun insert(vararg details: DatabaseHeadToHeadDetail) {
        headToHeadDetailDao.insert(*details)
    }

    suspend fun update(vararg details: DatabaseHeadToHeadDetail) {
        headToHeadDetailDao.update(*details)
    }

    suspend fun update(heat: DatabaseHeadToHeadHeat) {
        headToHeadHeatDao.update(heat)
    }

    suspend fun update(heat: DatabaseHeadToHead) {
        headToHeadDao.update(heat)
    }
}
