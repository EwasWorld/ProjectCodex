package eywa.projectcodex.database.archer

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

    suspend fun insert(archer: DatabaseArcher) {
        archerDao.insert(archer)
    }

    suspend fun insert(archer: DatabaseArcherHandicap) {
        archerHandicapDao.insert(archer)
    }
}
