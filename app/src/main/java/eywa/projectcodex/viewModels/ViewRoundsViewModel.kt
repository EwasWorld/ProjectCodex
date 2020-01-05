package eywa.projectcodex.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.database.repositories.ArcherRoundsRepo
import eywa.projectcodex.database.repositories.ArrowValuesRepo

/**
 * @see InputEndViewModel
 */
class ViewRoundsViewModel(application: Application) : AndroidViewModel(application) {
    private val arrowValuesRepo: ArrowValuesRepo =
        ArrowValuesRepo(ScoresRoomDatabase.getDatabase(application, viewModelScope).arrowValueDao())
    private val archerRoundsRepo: ArcherRoundsRepo =
        ArcherRoundsRepo(ScoresRoomDatabase.getDatabase(application, viewModelScope).archerRoundDao())

    val allArrows: LiveData<List<ArrowValue>>
    val allArcherRounds: LiveData<List<ArcherRound>>

    init {
        allArrows = arrowValuesRepo.allArrowValues
        allArcherRounds = archerRoundsRepo.allArcherRounds
    }
}