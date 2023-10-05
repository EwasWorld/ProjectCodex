package eywa.projectcodex.database.archer

import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge

const val DEFAULT_ARCHER_ID = -1

class ArcherRepo(
        private val archerDao: ArcherDao,
        private val archerHandicapDao: ArcherHandicapDao,
) {
    val allArchers = archerDao.getAllArchers()
    val defaultArcher = archerDao.getDefaultArcher()

    /**
     * @see ArcherHandicapDao.getLatestHandicaps
     */
    val latestHandicapsForDefaultArcher = getLatestHandicaps(DEFAULT_ARCHER_ID)

    fun getLatestHandicaps(archerId: Int) = archerHandicapDao.getLatestHandicaps(archerId)

    val allHandicapsForDefaultArcher = archerHandicapDao.getAllHandicaps(DEFAULT_ARCHER_ID)

    suspend fun insertDefaultArcherIfNotExist() {
        archerDao.insertOrIgnore(DatabaseArcher(DEFAULT_ARCHER_ID, "Default"))
    }

    suspend fun updateDefaultArcher(isGent: Boolean) = archerDao.updateDefaultArcher(isGent)

    suspend fun updateDefaultArcher(age: ClassificationAge) = archerDao.updateDefaultArcher(age)

    suspend fun insert(archer: DatabaseArcher) {
        archerDao.insert(archer)
    }

    suspend fun insert(handicap: DatabaseArcherHandicap) {
        archerHandicapDao.insert(handicap)
    }

    suspend fun deleteHandicap(handicapId: Int) {
        archerHandicapDao.delete(handicapId)
    }
}
