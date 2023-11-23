package eywa.projectcodex.database.arrows

class ArrowCounterRepo(private val arrowCounterDao: ArrowCounterDao) {
    suspend fun insert(vararg counters: DatabaseArrowCounter) {
        arrowCounterDao.insert(*counters)
    }

    suspend fun update(vararg counters: DatabaseArrowCounter) {
        arrowCounterDao.update(*counters)
    }

    suspend fun delete(counter: DatabaseArrowCounter) {
        arrowCounterDao.delete(counter.shootId)
    }
}
