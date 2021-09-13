package eywa.projectcodex.components.viewScores

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRoundsRepo
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.arrowValue.ArrowValuesRepo
import eywa.projectcodex.database.rounds.RoundRepo
import kotlinx.coroutines.launch

/**
 * @see InputEndViewModel
 */
class ViewScoresViewModel(
        application: Application,
        val db: ScoresRoomDatabase
) : AndroidViewModel(application), ConvertScore.ConvertScoreViewModel {
    private val arrowValuesRepo: ArrowValuesRepo = ArrowValuesRepo(db.arrowValueDao())
    private val archerRoundsRepo: ArcherRoundsRepo = ArcherRoundsRepo(db.archerRoundDao())
    private val roundRepo: RoundRepo = RoundRepo(db)

    val allArrows = arrowValuesRepo.allArrowValues
    val allArcherRounds = archerRoundsRepo.allArcherRoundsWithRoundInfoAndName
    val allArrowCounts = roundRepo.roundArrowCounts
    val allDistances = roundRepo.roundDistances

    /**
     * Deletes the specified round and all its arrows
     */
    fun deleteRound(archerRoundId: Int) = viewModelScope.launch {
        archerRoundsRepo.deleteRound(archerRoundId)
        arrowValuesRepo.deleteRoundsArrows(archerRoundId)
    }

    override fun updateArrowValues(vararg arrows: ArrowValue) = viewModelScope.launch {
        arrowValuesRepo.update(*arrows)
    }
}