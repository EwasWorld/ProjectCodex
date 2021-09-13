package eywa.projectcodex.components.archerRoundScore.archerRoundStats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
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
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundRepo

/**
 * @see InputEndViewModel
 */
class ArcherRoundStatsViewModel @AssistedInject constructor(
        @Assisted private val stateHandle: SavedStateHandle,
        application: Application,
        db: ScoresRoomDatabase
) : AndroidViewModel(application) {
    private val archerRoundId = stateHandle.get<Int>("archerRoundId")!!
    private val arrowValueRepo: ArrowValuesRepo = ArrowValuesRepo(db.arrowValueDao(), archerRoundId)
    private val roundRepo: RoundRepo = RoundRepo(db)
    val arrows: LiveData<List<ArrowValue>> = arrowValueRepo.arrowValuesForRound!!
    val archerRoundWithRoundInfo: LiveData<ArcherRoundWithRoundInfoAndName> =
            ArcherRoundsRepo(db.archerRoundDao()).getArcherRoundWithRoundInfoAndName(archerRoundId)

    fun getArrowCountsForRound(roundId: Int): LiveData<List<RoundArrowCount>> {
        return roundRepo.getArrowCountsForRound(roundId)
    }

    fun getDistancesForRound(roundId: Int, subTypeId: Int?): LiveData<List<RoundDistance>> {
        return roundRepo.getDistancesForRound(roundId, subTypeId)
    }

    @AssistedFactory
    interface Factory : ViewModelAssistedFactory<ArcherRoundStatsViewModel>
}