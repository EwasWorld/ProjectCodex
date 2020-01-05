package eywa.projectcodex.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.database.repositories.ArrowValuesRepo
import kotlinx.coroutines.launch

/**
 * With a ViewModel, the data is kept even if the activity is destroyed (e.g. in the event of a screen rotation)
 * You should make a ViewModel per screen rather than per entity
 *
 * If OS kills the app the View Model is destroyed
 * https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate
 * https://medium.com/androiddevelopers/viewmodels-persistence-onsaveinstancestate-restoring-ui-state-and-loaders-fc7cc4a6c090
 */
class InputEndViewModel(application: Application, archerRoundId: Int) : AndroidViewModel(application) {
    private val repository: ArrowValuesRepo
    val allArrows: LiveData<List<ArrowValue>>

    init {
        val arrowValueDao = ScoresRoomDatabase.getDatabase(application, viewModelScope).arrowValueDao()
        repository = ArrowValuesRepo(arrowValueDao, archerRoundId)
        allArrows = repository.arrowValuesForRound!!
    }

    /**
     * Launching in this scope prevents blocking
     */
    fun insert(arrowValue: ArrowValue) = viewModelScope.launch {
        repository.insert(arrowValue)
    }
}