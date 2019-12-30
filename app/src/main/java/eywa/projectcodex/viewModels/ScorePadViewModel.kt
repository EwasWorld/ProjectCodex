package eywa.projectcodex.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.database.repositories.ArrowValuesRepo

/**
 * @see InputEndViewModel
 */
class ScorePadViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ArrowValuesRepo
    val allArrows: LiveData<List<ArrowValue>>

    init {
        val arrowValueDao = ScoresRoomDatabase.getDatabase(application, viewModelScope).arrowValueDao()
        repository = ArrowValuesRepo(arrowValueDao)
        allArrows = repository.allArrowValues
    }
}