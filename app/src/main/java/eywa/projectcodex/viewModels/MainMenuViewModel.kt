package eywa.projectcodex.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.UpdateType
import eywa.projectcodex.database.entities.Round
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundDistance
import eywa.projectcodex.database.entities.RoundSubType
import eywa.projectcodex.database.repositories.RoundsRepo
import kotlinx.coroutines.launch

/**
 * @see InputEndViewModel
 */
class MainMenuViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RoundsRepo
    val rounds: LiveData<List<Round>>
    val roundArrowCounts: LiveData<List<RoundArrowCount>>
    val roundDistances: LiveData<List<RoundDistance>>
    val roundSubTypes: LiveData<List<RoundSubType>>

    init {
        val db = ScoresRoomDatabase.getDatabase(application, viewModelScope)

        repository = RoundsRepo(db.roundDao(), db.roundArrowCountDao(), db.roundSubTypeDao(), db.roundDistanceDao())
        rounds = repository.rounds
        roundArrowCounts = repository.roundArrowCounts
        roundDistances = repository.roundDistances
        roundSubTypes = repository.roundSubTypes
    }

    /**
     * @see RoundsRepo.updateRounds
     */
    fun updateRounds(updateItems: Map<Any, UpdateType>) = viewModelScope.launch {
        repository.updateRounds(updateItems)
    }
}