package eywa.projectcodex.components.archerRoundScore.inputEnd

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eywa.projectcodex.common.utils.ViewModelAssistedFactory
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.archerRound.ArcherRoundsRepo
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.arrowValue.ArrowValuesRepo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundRepo
import kotlinx.coroutines.launch

/**
 * With a ViewModel, the data is kept even if the activity is destroyed (e.g. in the event of a screen rotation)
 * You should make a ViewModel per screen rather than per entity
 *
 * If OS kills the app the View Model is destroyed
 * https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate
 * https://medium.com/androiddevelopers/viewmodels-persistence-onsaveinstancestate-restoring-ui-state-and-loaders-fc7cc4a6c090
 */
class InputEndViewModel @AssistedInject constructor(
        @Assisted private val stateHandle: SavedStateHandle,
        application: Application,
        db: ScoresRoomDatabase
) : AndroidViewModel(application) {
    private val archerRoundId = stateHandle.get<Int>("archerRoundId")!!
    private val arrowValueRepo: ArrowValuesRepo = ArrowValuesRepo(db.arrowValueDao(), archerRoundId)
    private val roundRepo: RoundRepo = RoundRepo(db)
    val arrows: LiveData<List<ArrowValue>> = arrowValueRepo.arrowValuesForRound!!
    val archerRoundWithInfo: LiveData<ArcherRoundWithRoundInfoAndName> =
            ArcherRoundsRepo(db.archerRoundDao()).getArcherRoundWithRoundInfoAndName(archerRoundId)

    fun getArrowCountsForRound(roundId: Int): LiveData<List<RoundArrowCount>> {
        return roundRepo.getArrowCountsForRound(roundId)
    }

    fun getDistancesForRound(roundId: Int, subTypeId: Int?): LiveData<List<RoundDistance>> {
        return roundRepo.getDistancesForRound(roundId, subTypeId)
    }

    fun getRoundById(roundId: Int): LiveData<Round> {
        return roundRepo.getRoundById(roundId)
    }

    /**
     * Launching in this scope prevents blocking
     */
    fun insert(vararg arrowValues: ArrowValue) = viewModelScope.launch {
        arrowValueRepo.insert(*arrowValues)
    }

    fun update(vararg arrowValues: ArrowValue) = viewModelScope.launch {
        arrowValueRepo.update(*arrowValues)
    }

    fun insertEnd(allArrows: List<ArrowValue>, toInsert: List<ArrowValue>) = viewModelScope.launch {
        arrowValueRepo.insertEnd(allArrows, toInsert)
    }

    @AssistedFactory
    interface Factory : ViewModelAssistedFactory<InputEndViewModel>
}