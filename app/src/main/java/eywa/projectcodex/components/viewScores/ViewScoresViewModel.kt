package eywa.projectcodex.components.viewScores

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.archerRound.ArcherRoundsRepo
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.arrowValue.ArrowValuesRepo
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundRepo
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @see ArcherRoundScoreViewModel
 */
@HiltViewModel
class ViewScoresViewModel @Inject constructor(val db: ScoresRoomDatabase) : ViewModel() {
    var state by mutableStateOf(ViewScoresState())
        private set

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
                        val previousSelectedEntries = state.data.associate { it.id to it.isSelected }

                        state = state.copy(
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
                                }?.sortedByDescending { it.archerRound.dateShot } ?: listOf()
                        )
                    }
        }
    }

    fun handle(action: ViewScoresIntent) {
        when (action) {
            is ViewScoresIntent.ToggleMultiSelectMode -> {
                var newState = state.copy(isInMultiSelectMode = !state.isInMultiSelectMode)
                // If currently in multiselect mode (thus toggling off), deselect all items
                if (state.isInMultiSelectMode) {
                    newState = newState.copy(data = state.data.map { it.copy(isSelected = false) })
                }
                state = newState
            }
            is ViewScoresIntent.ToggleEntrySelected -> {
                val entry = state.data[action.entryIndex]
                val newList = state.data.toMutableList()
                newList[action.entryIndex] = entry.copy(isSelected = !entry.isSelected)
                state = state.copy(data = newList)
            }
            is ViewScoresIntent.SelectAllOrNone -> {
                val selectAll = action.forceIsSelectedTo ?: !state.data.all { it.isSelected }
                state = state.copy(data = state.data.map { it.copy(isSelected = selectAll) })
            }
            is ViewScoresIntent.DeleteRound -> viewModelScope.launch {
                archerRoundsRepo.deleteRound(action.archerRoundId)
                arrowValuesRepo.deleteRoundsArrows(action.archerRoundId)
            }
            is ViewScoresIntent.UpdateArrowValues -> viewModelScope.launch {
                arrowValuesRepo.update(*action.arrows.toTypedArray())
            }
        }
    }
}