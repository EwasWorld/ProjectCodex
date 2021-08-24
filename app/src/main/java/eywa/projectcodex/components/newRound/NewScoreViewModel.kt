package eywa.projectcodex.components.newRound

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.components.commonUtils.UpdateDefaultRounds
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.ArcherRoundsRepo
import eywa.projectcodex.database.arrowValue.ArrowValuesRepo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import kotlinx.coroutines.launch

/**
 * @see InputEndViewModel
 */
class NewScoreViewModel(application: Application) : AndroidViewModel(application) {
    private val db: ScoresRoomDatabase = ScoresRoomDatabase.getDatabase(application)
    private val archerRoundsRepo: ArcherRoundsRepo = ArcherRoundsRepo(db.archerRoundDao())
    val maxId: LiveData<Int> = archerRoundsRepo.maxId
    val allRounds: LiveData<List<Round>> = db.roundDao().getAllRounds()
    val allRoundSubTypes: LiveData<List<RoundSubType>> = db.roundSubTypeDao().getAllSubTypes()
    val allRoundArrowCounts: LiveData<List<RoundArrowCount>> = db.roundArrowCountDao().getAllArrowCounts()
    val allRoundDistances: LiveData<List<RoundDistance>> = db.roundDistanceDao().getAllDistances()
    val updateDefaultRoundsState = UpdateDefaultRounds.taskProgress.getState()
    val updateDefaultRoundsProgressMessage = UpdateDefaultRounds.taskProgress.getMessage()

    fun insert(archerRound: ArcherRound) = viewModelScope.launch {
        archerRoundsRepo.insert(archerRound)
    }

    fun update(archerRound: ArcherRound) = viewModelScope.launch {
        archerRoundsRepo.update(archerRound)
    }

    fun getArcherRound(archerRoundId: Int) = archerRoundsRepo.getArcherRound(archerRoundId)

    fun getArrowsForRound(archerRoundId: Int) =
            ArrowValuesRepo(db.arrowValueDao(), archerRoundId).arrowValuesForRound!!
}