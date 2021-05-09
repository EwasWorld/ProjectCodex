package eywa.projectcodex.components.newRound

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.ArcherRoundsRepo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import kotlinx.coroutines.launch

/**
 * @see InputEndViewModel
 */
class NewRoundViewModel(application: Application) : AndroidViewModel(application) {
    private val archerRoundsRepo: ArcherRoundsRepo
    val maxId: LiveData<Int>
    val allRounds: LiveData<List<Round>>
    val allRoundSubTypes: LiveData<List<RoundSubType>>
    val allRoundArrowCounts: LiveData<List<RoundArrowCount>>
    val allRoundDistances: LiveData<List<RoundDistance>>

    init {
        val db = ScoresRoomDatabase.getDatabase(application)
        archerRoundsRepo = ArcherRoundsRepo(db.archerRoundDao())
        maxId = archerRoundsRepo.maxId
        allRounds = db.roundDao().getAllRounds()
        allRoundSubTypes = db.roundSubTypeDao().getAllSubTypes()
        allRoundArrowCounts = db.roundArrowCountDao().getAllArrowCounts()
        allRoundDistances = db.roundDistanceDao().getAllDistances()
    }

    fun insert(archerRound: ArcherRound) = viewModelScope.launch {
        archerRoundsRepo.insert(archerRound)
    }
}