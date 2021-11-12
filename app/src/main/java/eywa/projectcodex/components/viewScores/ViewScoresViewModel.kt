package eywa.projectcodex.components.viewScores

import android.app.Application
import androidx.lifecycle.*
import eywa.projectcodex.components.app.App
import eywa.projectcodex.components.archerRoundScore.ArcherRoundScoreViewModel
import eywa.projectcodex.components.viewScores.data.ViewScoreData
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.archerRound.ArcherRoundsRepo
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.arrowValue.ArrowValuesRepo
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundRepo
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @see ArcherRoundScoreViewModel
 */
class ViewScoresViewModel(application: Application) : AndroidViewModel(application),
        ConvertScore.ConvertScoreViewModel {
    @Inject
    lateinit var db: ScoresRoomDatabase

    init {
        (application as App).appComponent.inject(this)
    }

    private val arrowValuesRepo: ArrowValuesRepo = ArrowValuesRepo(db.arrowValueDao())
    private val archerRoundsRepo: ArcherRoundsRepo = ArcherRoundsRepo(db.archerRoundDao())
    private val roundRepo: RoundRepo = RoundRepo(db)

    private val allArrows = arrowValuesRepo.allArrowValues
    private val allArcherRounds = archerRoundsRepo.allArcherRoundsWithRoundInfoAndName
    private val allArrowCounts = roundRepo.roundArrowCounts
    private val allDistances = roundRepo.roundDistances
    val viewScoresData = ViewScoresLiveData(allArrows, allArcherRounds, allArrowCounts, allDistances)

    /**
     * Deletes the specified round and all its arrows
     */
    fun deleteRound(archerRoundId: Int) = viewModelScope.launch {
        archerRoundsRepo.deleteRound(archerRoundId)
        arrowValuesRepo.deleteRoundsArrows(archerRoundId)
    }

    override fun updateArrowValues(vararg arrows: ArrowValue) = viewModelScope.launch {
        arrowValuesRepo.update(*arrows)
    }

    /**
     * Creates [LiveData] for [ViewScoreData] out of the inputted LiveData
     */
    class ViewScoresLiveData(
            private val arrowsLiveData: LiveData<List<ArrowValue>>,
            private val archerRoundsLiveData: LiveData<List<ArcherRoundWithRoundInfoAndName>>,
            private val arrowCountsLiveData: LiveData<List<RoundArrowCount>>,
            private val distancesLiveData: LiveData<List<RoundDistance>>
    ) : MediatorLiveData<ViewScoreData>() {
        /**
         * True when all sources have been added to the live data
         */
        private var initialised = false
        private var arrows: List<ArrowValue>? = null
        private var arrowCounts: List<RoundArrowCount>? = null
        private var distances: List<RoundDistance>? = null

        init {
            value = ViewScoreData.getViewScoreData()
        }

        /**
         * Add sources here because [addSource] doesn't work when the [MediatorLiveData] doesn't have any observers.
         */
        override fun onActive() {
            super.onActive()
            if (initialised) return
            initialised = true

            /*
             * value = value lines because otherwise this.onChanged isn't called and the view doesn't update properly
             */
            super.addSource(arrowsLiveData) {
                arrows = it
                if (value?.updateArrows(it) == true) {
                    value = value
                }
            }
            super.addSource(archerRoundsLiveData) { archerRound ->
                if (value?.updateArcherRounds(archerRound) == true) {
                    /*
                     * Arrows, arrowCounts, and distances are only saved in the ViewScoreEntrys after archerRounds have
                     *   been set. This means whenever we update archerRounds we much make sure that these are also set
                     */
                    arrows?.let { value?.updateArrows(it) }
                    arrowCounts?.let { value?.updateArrowCounts(it) }
                    distances?.let { value?.updateDistances(it) }

                    value = value
                }
            }
            super.addSource(arrowCountsLiveData) {
                arrowCounts = it
                if (value?.updateArrowCounts(it) == true) {
                    value = value
                }
            }
            super.addSource(distancesLiveData) {
                distances = it
                if (value?.updateDistances(it) == true) {
                    value = value
                }
            }
        }

        override fun <S : Any?> addSource(source: LiveData<S>, onChanged: Observer<in S>) {
            throw UnsupportedOperationException()
        }

        override fun <S : Any?> removeSource(toRemote: LiveData<S>) {
            throw UnsupportedOperationException()
        }
    }
}