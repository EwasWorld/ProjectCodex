package eywa.projectcodex.components.newScore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.common.utils.UpdateDefaultRounds
import eywa.projectcodex.components.app.App
import eywa.projectcodex.components.archerRoundScore.ArcherRoundScoreViewModel
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.ArcherRoundsRepo
import eywa.projectcodex.database.arrowValue.ArrowValuesRepo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @see ArcherRoundScoreViewModel
 */
class NewScoreViewModel(application: Application) : AndroidViewModel(application) {
    @Inject
    lateinit var db: ScoresRoomDatabase

    init {
        (application as App).appComponent.inject(this)
    }

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
            ArrowValuesRepo(db.arrowValueDao()).getArrowValuesForRound(archerRoundId)
}