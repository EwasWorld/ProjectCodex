package eywa.projectcodex.database.archerRound

import androidx.lifecycle.LiveData
import eywa.projectcodex.database.rounds.Round

/**
 * @see ArrowValuesRepo
 */
class ArcherRoundsRepo(private val archerRoundDao: ArcherRoundDao) {
    val maxId: LiveData<Int> = archerRoundDao.getMaxId()

    val allArcherRoundsWithRoundInfoAndName: LiveData<List<ArcherRoundWithRoundInfoAndName>> =
            archerRoundDao.getAllArcherRoundsWithRoundInfoAndName()

    fun getRoundInfo(archerRoundId: Int): LiveData<Round> {
        return archerRoundDao.getRoundInfo(archerRoundId)
    }

    fun getArcherRoundWithRoundInfoAndName(archerRoundId: Int): LiveData<ArcherRoundWithRoundInfoAndName> {
        return archerRoundDao.getArcherRoundWithRoundInfoAndName(archerRoundId)
    }

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