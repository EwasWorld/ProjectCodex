package eywa.projectcodex.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.database.entities.Round
import eywa.projectcodex.database.repositories.ArcherRoundsRepo
import eywa.projectcodex.database.repositories.ArrowValuesRepo

/**
 * @see InputEndViewModel
 */
class ScorePadViewModel(application: Application, archerRoundId: Int) : AndroidViewModel(application) {
    val arrowsForRound: LiveData<List<ArrowValue>>
    val roundInfo: LiveData<Round>

    init {
        val db = ScoresRoomDatabase.getDatabase(application, viewModelScope)
        arrowsForRound = ArrowValuesRepo(db.arrowValueDao(), archerRoundId).arrowValuesForRound!!
        roundInfo = ArcherRoundsRepo(db.archerRoundDao()).getRoundInfo(archerRoundId)
    }
}