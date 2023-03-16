package eywa.projectcodex.database.archerRound

import androidx.lifecycle.LiveData

/**
 * @see ArrowValuesRepo
 */
class ArcherRoundsRepo(private val archerRoundDao: ArcherRoundDao) {
    val personalBests = archerRoundDao.getPersonalBests()

    val allFullArcherRounds = archerRoundDao.getAllFullArcherRoundInfo()

    fun getFullArcherRoundInfo(archerRoundId: Int) = archerRoundDao.getFullArcherRoundInfo(archerRoundId)

    fun getArcherRound(archerRoundId: Int): LiveData<ArcherRound> {
        return archerRoundDao.getArcherRoundById(archerRoundId)
    }

    suspend fun insert(archerRound: ArcherRound) = archerRoundDao.insert(archerRound)

    suspend fun deleteRound(archerRoundId: Int) {
        archerRoundDao.deleteRound(archerRoundId)
    }

    suspend fun update(vararg archerRounds: ArcherRound) {
        archerRoundDao.update(*archerRounds)
    }
}
