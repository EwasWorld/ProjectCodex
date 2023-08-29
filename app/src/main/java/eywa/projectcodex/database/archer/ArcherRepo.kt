package eywa.projectcodex.database.archer

const val DEFAULT_ARCHER_ID = -1

class ArcherRepo(
        private val archerDao: ArcherDao,
        private val archerHandicapDao: ArcherHandicapDao,
) {
    val allArchers = archerDao.getAllArchers()
    val defaultArcher = archerDao.getDefaultArcher()

    /**
     * @see ArcherHandicapDao.getLatestHandicapsForDefaultArcher
     */
    val latestHandicapsForDefaultArcher = archerHandicapDao.getLatestHandicapsForDefaultArcher()

    suspend fun insert(archer: DatabaseArcher) {
        archerDao.insert(archer)
    }

    suspend fun insert(archer: DatabaseArcherHandicap) {
        archerHandicapDao.insert(archer)
    }
}
