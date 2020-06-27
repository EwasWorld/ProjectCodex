package eywa.projectcodex.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.database.entities.ArcherRoundWithName
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.database.repositories.ArcherRoundsRepo
import eywa.projectcodex.database.repositories.ArrowValuesRepo
import kotlinx.coroutines.launch

/**
 * @see InputEndViewModel
 */
class ViewRoundsViewModel(application: Application) : AndroidViewModel(application) {
    private val arrowValuesRepo: ArrowValuesRepo =
        ArrowValuesRepo(ScoresRoomDatabase.getDatabase(application, viewModelScope).arrowValueDao())
    private val archerRoundsRepo: ArcherRoundsRepo =
        ArcherRoundsRepo(ScoresRoomDatabase.getDatabase(application, viewModelScope).archerRoundDao())

    val allArrows: LiveData<List<ArrowValue>>
    val allArcherRounds: LiveData<List<ArcherRoundWithName>>

    init {
        allArrows = arrowValuesRepo.allArrowValues
        allArcherRounds = archerRoundsRepo.allArcherRoundsWithName
    }

    /**
     * Deletes the specified round and all its arrows
     */
    fun deleteRound(archerRoundId: Int) = viewModelScope.launch {
        archerRoundsRepo.deleteRound(archerRoundId)
        arrowValuesRepo.deleteRoundsArrows(archerRoundId)
    }
}