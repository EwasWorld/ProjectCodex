package eywa.projectcodex.database.sightMarks

class SightMarkRepo(private val sightMarkDao: SightMarkDao) {
    val allSightMarks = sightMarkDao.getAllSightMarks()

    fun getSightMark(id: Int) = sightMarkDao.getSightMark(id)
    fun getSightMarkForDistance(distance: Int, isMetric: Boolean) =
            sightMarkDao.getSightMarkForDistance(distance, isMetric)

    suspend fun insert(sightMark: DatabaseSightMark) = sightMarkDao.insert(sightMark)
    suspend fun archiveAll() = sightMarkDao.archiveAll()
    suspend fun delete(id: Int) = sightMarkDao.deleteRound(id)
    suspend fun update(vararg sightMarks: DatabaseSightMark) = sightMarkDao.update(*sightMarks)
}
