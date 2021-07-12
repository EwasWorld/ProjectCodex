package eywa.projectcodex.components.archerRoundScore.archerRoundStats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
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
class ArcherRoundStatsViewModel(application: Application, archerRoundId: Int) : AndroidViewModel(application) {
    private val arrowValueRepo: ArrowValuesRepo
    private val roundRepo: RoundRepo
    val arrows: LiveData<List<ArrowValue>>
    val archerRoundWithRoundInfo: LiveData<ArcherRoundWithRoundInfoAndName>

    init {
        val db = ScoresRoomDatabase.getDatabase(application)
        roundRepo = RoundRepo(db)
        arrowValueRepo = ArrowValuesRepo(db.arrowValueDao(), archerRoundId)
        arrows = arrowValueRepo.arrowValuesForRound!!
        archerRoundWithRoundInfo =
                ArcherRoundsRepo(db.archerRoundDao()).getArcherRoundWithRoundInfoAndName(archerRoundId)
    }

    fun getArrowCountsForRound(roundId: Int): LiveData<List<RoundArrowCount>> {
        return roundRepo.getArrowCountsForRound(roundId)
    }

    fun getDistancesForRound(roundId: Int, subTypeId: Int?): LiveData<List<RoundDistance>> {
        return roundRepo.getDistancesForRound(roundId, subTypeId)
    }
}