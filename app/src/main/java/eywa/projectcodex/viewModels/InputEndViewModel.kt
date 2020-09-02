package eywa.projectcodex.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.*
import eywa.projectcodex.database.repositories.ArcherRoundsRepo
import eywa.projectcodex.database.repositories.ArrowValuesRepo
import eywa.projectcodex.database.repositories.RoundsRepo
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
    private val arrowValueRepo: ArrowValuesRepo
    private val roundsRepo: RoundsRepo
    val arrows: LiveData<List<ArrowValue>>
    val archerRound: LiveData<ArcherRound>

    init {
        val db = ScoresRoomDatabase.getDatabase(application, viewModelScope)
        roundsRepo = RoundsRepo(db.roundDao(), db.roundArrowCountDao(), db.roundSubTypeDao(), db.roundDistanceDao())
        arrowValueRepo = ArrowValuesRepo(db.arrowValueDao(), archerRoundId)
        arrows = arrowValueRepo.arrowValuesForRound!!
        archerRound = ArcherRoundsRepo(db.archerRoundDao()).getArcherRound(archerRoundId)
    }

    fun getArrowCountsForRound(roundId: Int): LiveData<List<RoundArrowCount>> {
        return roundsRepo.getArrowCountsForRound(roundId)
    }

    fun getDistancesForRound(roundId: Int, subTypeId: Int?): LiveData<List<RoundDistance>> {
        return roundsRepo.getDistancesForRound(roundId, subTypeId)
    }

    fun getRoundById(roundId: Int): LiveData<Round> {
        return roundsRepo.getRoundById(roundId)
    }

    /**
     * Launching in this scope prevents blocking
     */
    fun insert(arrowValue: ArrowValue) = viewModelScope.launch {
        arrowValueRepo.insert(arrowValue)
    }

    fun update(arrowValue: ArrowValue) {
        arrowValueRepo.update(arrowValue)
    }
}