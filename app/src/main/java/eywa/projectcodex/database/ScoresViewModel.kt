package eywa.projectcodex.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.database.entities.ArrowValue
import kotlinx.coroutines.launch

/**
 * If OS kills the app the View Model is destroyed
 * https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate
 * https://medium.com/androiddevelopers/viewmodels-persistence-onsaveinstancestate-restoring-ui-state-and-loaders-fc7cc4a6c090
 */
class ScoresViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScoresRepository
    // LiveData gives us updated words when they change.
    val allArrows: LiveData<List<ArrowValue>>

    init {
        val arrowValueDao = ScoresRoomDatabase.getDatabase(application, viewModelScope).arrowValueDao()
        repository = ScoresRepository(arrowValueDao)
        allArrows = repository.arrowValuesRepo.allArrowValues
    }

    /**
     * Launching in this scope prevents blocking
     */
    fun insert(arrowValue: ArrowValue) = viewModelScope.launch {
        repository.arrowValuesRepo.insert(arrowValue)
    }
}