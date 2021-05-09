package eywa.projectcodex.database.archer

/**
 * @see ArrowValuesRepo
 */
class ArcherRepo(private val archerDao: ArcherDao) {
    suspend fun insert(archer: Archer) {
        archerDao.insert(archer)
    }
}