package eywa.projectcodex.components.viewScores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcase
import eywa.projectcodex.components.viewScores.ViewScoresIntent.*
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.ui.convertScoreDialog.ConvertScoreIntent
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
            is HelpShowcaseAction -> helpShowcase.handle(action.action, ViewScoresFragment::class)
            is MultiSelectAction -> handleMultiSelectIntent(action.action)
            is EffectComplete -> handleEffectComplete(action)
            is ConvertScoreAction -> handleConvertScoreIntent(action.action)
            is EntryClicked -> {
                _state.update {
                    if (it.isInMultiSelectMode) {
                        it.selectItem(action.archerRoundId)
                    }
                    else {
                        it.data
                                .find { entry -> entry.id == action.archerRoundId }
                                ?.getSingleClickAction()
                                ?.handleClick?.invoke(it.copy(lastClickedEntryId = action.archerRoundId))
                                ?: return@update it
                    }
                }
            }
            is EntryLongClicked -> {
                _state.update {
                    if (it.isInMultiSelectMode) {
                        it.selectItem(action.archerRoundId)
                    }
                    else {
                        val entry = it.data.find { entry -> entry.id == action.archerRoundId }
                                ?: return@update it
                        it.copy(
                                lastClickedEntryId = action.archerRoundId,
                                dropdownItems = entry.getDropdownMenuItems(),
                        )
                    }
                }
            }
            is DropdownMenuClicked -> _state.update { action.item.handleClick(it) }
            DropdownMenuClosed -> _state.update { it.copy(dropdownItems = emptyList()) }
            NoRoundsDialogOkClicked -> _state.update { it.copy(noRoundsDialogOkClicked = true) }
            DeleteDialogCancelClicked -> _state.update { it.copy(deleteDialogOpen = false) }
            DeleteDialogOkClicked -> {
                val id = _state.value.let { currentState ->
                    currentState.lastClickedEntryId?.takeIf { currentState.deleteDialogOpen }
                }
                _state.update { it.copy(deleteDialogOpen = false) }

                if (id != null) {
                    viewModelScope.launch { archerRoundsRepo.deleteRound(id) }
                }
            }
        }
    }

    private fun ViewScoresState.selectItem(archerRoundId: Int): ViewScoresState {
        if (!isInMultiSelectMode) return this

        val entryIndex = _state.value
                .data
                .indexOfFirst { entry -> entry.id == archerRoundId }
                .takeIf { index -> index >= 0 }
                ?: return this

        val entry = data[entryIndex]

        return copy(
                data = data
                        .take(entryIndex)
                        .plus(entry.copy(isSelected = !entry.isSelected))
                        .plus(data.drop(entryIndex + 1))
        )
    }

    private fun handleEffectComplete(action: EffectComplete) {
        when (action) {
            HandledEmailClicked -> _state.update { it.copy(multiSelectEmailClicked = false) }
            HandledEmailNoSelection -> _state.update { it.copy(multiSelectEmailNoSelection = false) }
            HandledScorePadOpened -> _state.update { it.copy(openScorePadClicked = false) }
            HandledEditInfoOpened -> _state.update { it.copy(openEditInfoClicked = false) }
            HandledEmailOpened -> _state.update { it.copy(openEmailClicked = false) }
            HandledInputEndOnCompletedRound ->
                _state.update { it.copy(openInputEndOnCompletedRound = false) }
            HandledInputEndOpened -> _state.update { it.copy(openInputEndClicked = false) }
            HandledNoRoundsDialogOkClicked -> _state.update { it.copy(noRoundsDialogOkClicked = false) }
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

    private fun handleConvertScoreIntent(action: ConvertScoreIntent) {
        when (action) {
            ConvertScoreIntent.Close -> _state.update { it.copy(convertScoreDialogOpen = false) }

            is ConvertScoreIntent.Ok -> _state.update {
                val arrows = it.data
                        .find { entry -> entry.id == it.lastClickedEntryId }
                        ?.arrows
                        ?.takeIf { arrows -> arrows.isNotEmpty() }
                        ?.let { oldArrows -> action.convertType.convertScore(oldArrows) }
                        ?: return@update it.copy(convertScoreDialogOpen = false)

                viewModelScope.launch {
                    arrowValuesRepo.update(*arrows.toTypedArray())
                }
                it.copy(convertScoreDialogOpen = false)
            }
        }
    }
}
