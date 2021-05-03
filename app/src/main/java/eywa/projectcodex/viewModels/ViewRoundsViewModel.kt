package eywa.projectcodex.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.repositories.ArcherRoundsRepo
import eywa.projectcodex.database.repositories.ArrowValuesRepo
import eywa.projectcodex.database.repositories.RoundsRepo
import kotlinx.coroutines.launch

/**
 * @see InputEndViewModel
 */
class ViewRoundsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = ScoresRoomDatabase.getDatabase(application)
    private val arrowValuesRepo: ArrowValuesRepo = ArrowValuesRepo(db.arrowValueDao())
    private val archerRoundsRepo: ArcherRoundsRepo = ArcherRoundsRepo(db.archerRoundDao())
    private val roundsRepo: RoundsRepo = RoundsRepo(db)

    val allArrows: LiveData<List<ArrowValue>>
    val allArcherRounds: LiveData<List<ArcherRoundWithRoundInfoAndName>>
    val allArrowCounts: LiveData<List<RoundArrowCount>>

    init {
        allArrows = arrowValuesRepo.allArrowValues
        allArcherRounds = archerRoundsRepo.allArcherRoundsWithRoundInfoAndName
        allArrowCounts = roundsRepo.roundArrowCounts
    }

    /**
     * Deletes the specified round and all its arrows
     */
    fun deleteRound(archerRoundId: Int) = viewModelScope.launch {
        archerRoundsRepo.deleteRound(archerRoundId)
        arrowValuesRepo.deleteRoundsArrows(archerRoundId)
    }
}