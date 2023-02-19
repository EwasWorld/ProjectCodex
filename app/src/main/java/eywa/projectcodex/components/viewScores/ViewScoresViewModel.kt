package eywa.projectcodex.components.viewScores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcase
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.ui.multiSelectBar.MultiSelectBarIntent
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.archerRound.ArcherRoundsRepo
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.arrowValue.ArrowValuesRepo
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @see ArcherRoundScoreViewModel
 */
@HiltViewModel
class ViewScoresViewModel @Inject constructor(
        db: ScoresRoomDatabase,
        private val helpShowcase: HelpShowcase,
) : ViewModel() {
    private var _state = MutableStateFlow(ViewScoresState())
    val state = _state.asStateFlow()

    private val arrowValuesRepo: ArrowValuesRepo = ArrowValuesRepo(db.arrowValueDao())
    private val archerRoundsRepo: ArcherRoundsRepo = ArcherRoundsRepo(db.archerRoundDao())
    private val roundRepo: RoundRepo = RoundRepo(db)

    data class ViewScoresFlowData(
            val archerRounds: List<ArcherRoundWithRoundInfoAndName>? = null,
            val arrowValues: List<ArrowValue>? = null,
            val arrowCounts: List<RoundArrowCount>? = null,
            val distances: List<RoundDistance>? = null,
    )

    init {
        viewModelScope.launch {
            archerRoundsRepo.allArcherRoundsWithRoundInfoAndName.asFlow()
                    .combine(arrowValuesRepo.allArrowValues.asFlow()) { archerRoundInfo, arrowValues ->
                        ViewScoresFlowData(archerRoundInfo, arrowValues)
                    }
                    .combine(roundRepo.roundArrowCounts.asFlow()) { flowData, arrowCounts ->
                        flowData.copy(arrowCounts = arrowCounts)
                    }
                    .combine(roundRepo.roundDistances.asFlow()) { flowData, distances ->
                        flowData.copy(distances = distances)
                    }
                    .collect { flowData ->
                        val arrowValuesMap = flowData.arrowValues?.groupBy { arrow -> arrow.archerRoundId }
                        val arrowCountsMap = flowData.arrowCounts?.groupBy { arrowCount -> arrowCount.roundId }
                        val distancesMap = flowData.distances
                                ?.groupBy { distance -> distance.roundId }
                                ?.mapValues { entry ->
                                    entry.value.groupBy { distance -> distance.subTypeId }
                                }

                        _state.update {
                            val previousSelectedEntries = it.data.associate { entry -> entry.id to entry.isSelected }
                            it.copy(
                                    data = flowData.archerRounds?.map { roundInfo ->
                                        val roundId = roundInfo.round?.roundId
                                        val subtypeId = roundInfo.archerRound.roundSubTypeId ?: 1
                                        ViewScoresEntry(
                                                initialInfo = roundInfo,
                                                arrows = arrowValuesMap?.get(roundInfo.id),
                                                arrowCounts = roundId?.let { arrowCountsMap?.get(roundId) },
                                                distances = roundId?.let { distancesMap?.get(roundId)?.get(subtypeId) },
                                                isSelected = previousSelectedEntries[roundInfo.id] ?: false,
                                        )
                                    }?.sortedByDescending { entry -> entry.archerRound.dateShot } ?: listOf())
                        }
                    }
        }
    }

    fun handle(action: ViewScoresIntent) {
        when (action) {
            is ViewScoresIntent.ToggleEntrySelected -> {
                _state.update {
                    val entry = it.data[action.entryIndex]
                    val newList = it.data.toMutableList()
                    newList[action.entryIndex] = entry.copy(isSelected = !entry.isSelected)
                    it.copy(data = newList)
                }
            }
            is ViewScoresIntent.DeleteRound -> viewModelScope.launch {
                archerRoundsRepo.deleteRound(action.archerRoundId)
                arrowValuesRepo.deleteRoundsArrows(action.archerRoundId)
            }
            is ViewScoresIntent.UpdateArrowValues -> viewModelScope.launch {
                arrowValuesRepo.update(*action.arrows.toTypedArray())
            }
            is ViewScoresIntent.HelpShowcaseAction -> helpShowcase.handle(action.action, ViewScoresFragment::class)
            is ViewScoresIntent.MultiSelectAction -> handleMultiSelectIntent(action.action)
            is ViewScoresIntent.EffectComplete -> handleEffectComplete(action)
        }
    }

    private fun handleEffectComplete(action: ViewScoresIntent.EffectComplete) {
        when (action) {
            ViewScoresIntent.HandledEmailClicked -> _state.update { it.copy(multiSelectEmailClicked = false) }
            ViewScoresIntent.HandledEmailNoSelection -> _state.update { it.copy(multiSelectEmailNoSelection = false) }
        }
    }

    private fun handleMultiSelectIntent(action: MultiSelectBarIntent) {
        when (action) {
            MultiSelectBarIntent.ClickOpen -> _state.update { it.copy(isInMultiSelectMode = true) }
            MultiSelectBarIntent.ClickClose ->
                _state.update {
                    it.copy(
                            isInMultiSelectMode = false,
                            data = it.data.map { entry -> entry.copy(isSelected = false) }
                    )
                }
            MultiSelectBarIntent.ClickAllOrNone -> _state.update {
                val selectAll = !it.data.all { entry -> entry.isSelected }
                it.copy(data = it.data.map { entry -> entry.copy(isSelected = selectAll) })
            }
            MultiSelectBarIntent.ClickEmail -> _state.update {
                if (it.data.any { entry -> entry.isSelected }) {
                    it.copy(multiSelectEmailClicked = true)
                }
                else {
                    it.copy(multiSelectEmailNoSelection = true)
                }
            }
        }
    }
}
