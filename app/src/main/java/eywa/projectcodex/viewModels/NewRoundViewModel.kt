package eywa.projectcodex.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.database.entities.Round
import eywa.projectcodex.database.entities.RoundSubType
import eywa.projectcodex.database.repositories.ArcherRoundsRepo
import kotlinx.coroutines.launch

/**
 * @see InputEndViewModel
 */
class NewRoundViewModel(application: Application) : AndroidViewModel(application) {
    private val archerRoundsRepo: ArcherRoundsRepo
    val maxId: LiveData<Int>
    val allRounds: LiveData<List<Round>>
    val allRoundSubTypes: LiveData<List<RoundSubType>>

    init {
        val db = ScoresRoomDatabase.getDatabase(application, viewModelScope)
        archerRoundsRepo = ArcherRoundsRepo(db.archerRoundDao())
        maxId = archerRoundsRepo.maxId
        allRounds = db.roundDao().getAllRounds()
        allRoundSubTypes = db.roundSubTypeDao().getAllSubTypes()
    }

    fun insert(archerRound: ArcherRound) = viewModelScope.launch {
        archerRoundsRepo.insert(archerRound)
    }
}