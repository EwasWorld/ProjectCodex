package eywa.projectcodex.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.*
import eywa.projectcodex.database.repositories.ArcherRoundsRepo
import eywa.projectcodex.database.repositories.ArrowValuesRepo
import eywa.projectcodex.database.repositories.RoundsRepo
import kotlinx.coroutines.launch

/**
 * @see InputEndViewModel
 */
class ScorePadViewModel(application: Application, archerRoundId: Int) : AndroidViewModel(application) {
    private var roundsRepo: RoundsRepo
    private var arrowValueRepo: ArrowValuesRepo
    val arrowsForRound: LiveData<List<ArrowValue>>
    val roundInfo: LiveData<Round>
    val archerRound: LiveData<ArcherRound>

    init {
        val db = ScoresRoomDatabase.getDatabase(application, viewModelScope)
        arrowValueRepo = ArrowValuesRepo(db.arrowValueDao(), archerRoundId)
        arrowsForRound = arrowValueRepo.arrowValuesForRound!!
        val archerRoundsRepo = ArcherRoundsRepo(db.archerRoundDao())
        archerRound = archerRoundsRepo.getArcherRound(archerRoundId)
        roundInfo = archerRoundsRepo.getRoundInfo(archerRoundId)
        roundsRepo = RoundsRepo(db.roundDao(), db.roundArrowCountDao(), db.roundSubTypeDao(), db.roundDistanceDao())
    }

    fun getArrowCountsForRound(roundId: Int): LiveData<List<RoundArrowCount>> {
        return roundsRepo.getArrowCountsForRound(roundId)
    }

    fun getDistancesForRound(roundId: Int, subTypeId: Int?): LiveData<List<RoundDistance>> {
        return roundsRepo.getDistancesForRound(roundId, subTypeId)
    }

    /**
     * @param from zero indexed
     */
    fun deleteArrows(from: Int, count: Int) = viewModelScope.launch {
        arrowsForRound.value?.let { arrows ->
            arrowValueRepo.deleteEnd(arrows, from, count)
        }
    }
}