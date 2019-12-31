package eywa.projectcodex.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.database.repositories.ArcherRoundsRepo
import kotlinx.coroutines.launch

/**
 * @see InputEndViewModel
 */
class NewRoundViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ArcherRoundsRepo
    val maxId: LiveData<Int>

    init {
        val arrowValueDao = ScoresRoomDatabase.getDatabase(application, viewModelScope).archerRoundDao()
        repository = ArcherRoundsRepo(arrowValueDao)
        maxId = repository.maxId
    }

    fun insert(archerRound: ArcherRound) = viewModelScope.launch {
        repository.insert(archerRound)
    }
}