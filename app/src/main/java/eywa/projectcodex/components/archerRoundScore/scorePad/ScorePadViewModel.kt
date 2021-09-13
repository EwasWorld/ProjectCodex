package eywa.projectcodex.components.archerRoundScore.scorePad

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eywa.projectcodex.common.utils.ViewModelAssistedFactory
import eywa.projectcodex.components.archerRoundScore.inputEnd.InputEndViewModel
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
 * @see InputEndViewModel
 */
class ScorePadViewModel @AssistedInject constructor(
        @Assisted private val stateHandle: SavedStateHandle,
        application: Application,
        db: ScoresRoomDatabase
) : AndroidViewModel(application) {
    private val archerRoundId = stateHandle.get<Int>("archerRoundId")!!
    private var roundRepo: RoundRepo = RoundRepo(db)
    private var arrowValueRepo: ArrowValuesRepo = ArrowValuesRepo(db.arrowValueDao(), archerRoundId)
    val archerRoundsRepo = ArcherRoundsRepo(db.archerRoundDao())
    val arrowsForRound: LiveData<List<ArrowValue>> = arrowValueRepo.arrowValuesForRound!!
    val roundInfo: LiveData<Round> = archerRoundsRepo.getRoundInfo(archerRoundId)
    val archerRoundWithInfo: LiveData<ArcherRoundWithRoundInfoAndName> =
            archerRoundsRepo.getArcherRoundWithRoundInfoAndName(archerRoundId)

    fun getArrowCountsForRound(roundId: Int): LiveData<List<RoundArrowCount>> {
        return roundRepo.getArrowCountsForRound(roundId)
    }

    fun getDistancesForRound(roundId: Int, subTypeId: Int?): LiveData<List<RoundDistance>> {
        return roundRepo.getDistancesForRound(roundId, subTypeId)
    }

    /**
     * @param from zero indexed
     */
    fun deleteArrows(from: Int, count: Int) = viewModelScope.launch {
        arrowsForRound.value?.let { arrows ->
            arrowValueRepo.deleteEnd(arrows, from, count)
        }
    }

    @AssistedFactory
    interface Factory : ViewModelAssistedFactory<ScorePadViewModel>
}