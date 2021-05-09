package eywa.projectcodex.components.viewRounds

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.archerRound.ArcherRoundsRepo
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.arrowValue.ArrowValuesRepo
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundRepo
import kotlinx.coroutines.launch

/**
 * @see InputEndViewModel
 */
class ViewRoundsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = ScoresRoomDatabase.getDatabase(application)
    private val arrowValuesRepo: ArrowValuesRepo = ArrowValuesRepo(db.arrowValueDao())
    private val archerRoundsRepo: ArcherRoundsRepo = ArcherRoundsRepo(db.archerRoundDao())
    private val roundRepo: RoundRepo = RoundRepo(db)

    val allArrows: LiveData<List<ArrowValue>>
    val allArcherRounds: LiveData<List<ArcherRoundWithRoundInfoAndName>>
    val allArrowCounts: LiveData<List<RoundArrowCount>>

    init {
        allArrows = arrowValuesRepo.allArrowValues
        allArcherRounds = archerRoundsRepo.allArcherRoundsWithRoundInfoAndName
        allArrowCounts = roundRepo.roundArrowCounts
    }

    /**
     * Deletes the specified round and all its arrows
     */
    fun deleteRound(archerRoundId: Int) = viewModelScope.launch {
        archerRoundsRepo.deleteRound(archerRoundId)
        arrowValuesRepo.deleteRoundsArrows(archerRoundId)
    }
}