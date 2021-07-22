package eywa.projectcodex.components.archerRoundScore.scorePad

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.archerRound.ArcherRoundsRepo
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.arrowValue.ArrowValuesRepo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundRepo
import kotlinx.coroutines.launch

/**
 * @see InputEndViewModel
 */
class ScorePadViewModel(application: Application, archerRoundId: Int) : AndroidViewModel(application) {
    private var roundRepo: RoundRepo
    private var arrowValueRepo: ArrowValuesRepo
    val arrowsForRound: LiveData<List<ArrowValue>>
    val roundInfo: LiveData<Round>
    val archerRoundWithInfo: LiveData<ArcherRoundWithRoundInfoAndName>

    init {
        val db = ScoresRoomDatabase.getDatabase(application)
        arrowValueRepo = ArrowValuesRepo(db.arrowValueDao(), archerRoundId)
        arrowsForRound = arrowValueRepo.arrowValuesForRound!!
        val archerRoundsRepo = ArcherRoundsRepo(db.archerRoundDao())
        archerRoundWithInfo = archerRoundsRepo.getArcherRoundWithRoundInfoAndName(archerRoundId)
        roundInfo = archerRoundsRepo.getRoundInfo(archerRoundId)
        roundRepo = RoundRepo(db)
    }

    fun getArrowCountsForRound(roundId: Int): LiveData<List<RoundArrowCount>> {
        return roundRepo.getArrowCountsForRound(roundId)
    }

    fun getDistancesForRound(roundId: Int, subTypeId: Int?): LiveData<List<RoundDistance>> {
        return roundRepo.getDistancesForRound(roundId, subTypeId)
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