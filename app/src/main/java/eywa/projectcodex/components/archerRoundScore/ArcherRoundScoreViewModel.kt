package eywa.projectcodex.components.archerRoundScore

import android.app.Application
import androidx.lifecycle.*
import eywa.projectcodex.components.app.App
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
import javax.inject.Inject

/**
 * With a ViewModel, the data is kept even if the activity is destroyed (e.g. in the event of a screen rotation)
 * You should make a ViewModel per screen rather than per entity
 *
 * If OS kills the app the View Model is destroyed
 * https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate
 * https://medium.com/androiddevelopers/viewmodels-persistence-onsaveinstancestate-restoring-ui-state-and-loaders-fc7cc4a6c090
 */
class ArcherRoundScoreViewModel(application: Application) : AndroidViewModel(application) {
    @Inject
    lateinit var db: ScoresRoomDatabase

    init {
        (application as App).appComponent.inject(this)
    }

    val archerRoundIdMutableLiveData = MutableLiveData<Int?>(null)
    private var roundRepo: RoundRepo = RoundRepo(db)
    private var arrowValueRepo: ArrowValuesRepo = ArrowValuesRepo(db.arrowValueDao())
    val archerRoundsRepo = ArcherRoundsRepo(db.archerRoundDao())

    val arrowsForRound: LiveData<List<ArrowValue>> =
            archerRoundIdMutableLiveData.switchMap { id ->
                if (id != null) arrowValueRepo.getArrowValuesForRound(id) else MutableLiveData(listOf())
            }.distinctUntilChanged()
    val roundInfo: LiveData<Round> =
            archerRoundIdMutableLiveData.switchMap { id ->
                if (id != null) archerRoundsRepo.getRoundInfo(id) else MutableLiveData()
            }.distinctUntilChanged()
    val archerRoundWithInfo: LiveData<ArcherRoundWithRoundInfoAndName> =
            archerRoundIdMutableLiveData.switchMap { id ->
                if (id != null) archerRoundsRepo.getArcherRoundWithRoundInfoAndName(id) else MutableLiveData()
            }.distinctUntilChanged()

    fun getArrowCountsForRound(roundId: Int): LiveData<List<RoundArrowCount>> {
        return roundRepo.getArrowCountsForRound(roundId)
    }

    fun getDistancesForRound(roundId: Int, subTypeId: Int?): LiveData<List<RoundDistance>> {
        return roundRepo.getDistancesForRound(roundId, subTypeId)
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

    /**
     * @param from zero indexed
     */
    fun deleteArrows(from: Int, count: Int) = viewModelScope.launch {
        arrowsForRound.value?.let { arrows ->
            arrowValueRepo.deleteEnd(arrows, from, count)
        }
    }
}