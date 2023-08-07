package eywa.projectcodex.components.sightMarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.components.sightMarks.SightMarksIntent.*
import eywa.projectcodex.components.sightMarks.SightMarksState.Companion.LARGE_SCALE_AMOUNT
import eywa.projectcodex.components.sightMarks.SightMarksState.Companion.LARGE_SHIFT_AMOUNT
import eywa.projectcodex.components.sightMarks.SightMarksState.Companion.SMALL_SCALE_AMOUNT
import eywa.projectcodex.components.sightMarks.SightMarksState.Companion.SMALL_SHIFT_AMOUNT
import eywa.projectcodex.components.sightMarks.SightMarksState.Companion.ZERO_SCALE_VALUE
import eywa.projectcodex.components.sightMarks.SightMarksState.Companion.ZERO_SHIFT_VALUE
import eywa.projectcodex.components.sightMarks.SightMarksState.Loaded
import eywa.projectcodex.components.sightMarks.SightMarksState.Loading
import eywa.projectcodex.components.sightMarks.menu.SightMarksMenuIntent
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.bow.BowRepo
import eywa.projectcodex.database.sightMarks.SightMarkRepo
import eywa.projectcodex.model.SightMark
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SightMarksViewModel @Inject constructor(
        db: ScoresRoomDatabase,
        private val helpShowcase: HelpShowcaseUseCase,
) : ViewModel() {
    private val _state: MutableStateFlow<SightMarksState> = MutableStateFlow(Loading())
    val state = _state.asStateFlow()

    private val sightMarkRepo = SightMarkRepo(db.sightMarkDao())
    private val bowRepo = BowRepo(db.bowDao())

    init {
        viewModelScope.launch {
            sightMarkRepo.allSightMarks.collectLatest { dbSightMarks ->
                _state.update {
                    it.updateSightMarks(dbSightMarks.map { dbSightMark -> SightMark(dbSightMark) })
                }
            }
        }
        viewModelScope.launch {
            bowRepo.defaultBow.mapNotNull { it }.collectLatest { bow ->
                _state.update { it.updateIsHighestNumberAtTheTop(bow.isSightMarkDiagramHighestAtTop) }
            }
        }
    }

    fun handle(action: SightMarksIntent) {
        when (action) {
            is MenuAction -> when (action.action) {
                SightMarksMenuIntent.ArchiveAll -> viewModelScope.launch { sightMarkRepo.archiveAll() }
                SightMarksMenuIntent.FlipDiagram ->
                    viewModelScope.launch {
                        val current = (state.value as Loaded)
                        bowRepo.updateDefaultBow(!current.isHighestNumberAtTheTop)
                    }
                SightMarksMenuIntent.ShiftAndScale -> handle(StartShiftAndScale)
            }
            is SightMarkClicked -> _state.update { (it as Loaded).copy(openSightMarkDetail = action.item.id) }
            CreateSightMarkClicked -> _state.update { (it as Loaded).copy(createNewSightMark = true) }

            CreateSightMarkHandled -> _state.update { (it as Loaded).copy(createNewSightMark = false) }
            OpenSightMarkHandled -> _state.update { (it as Loaded).copy(openSightMarkDetail = null) }

            is HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.SIGHT_MARKS::class)
            StartShiftAndScale -> _state.update {
                if (it !is Loaded) return@update it
                it.copy(
                        scaleAmount = ZERO_SCALE_VALUE,
                        shiftAmount = ZERO_SHIFT_VALUE,
                        flipScale = false,
                        isConfirmShiftAndScaleDialogOpen = false,
                )
            }
            EndShiftAndScale -> _state.update {
                if (it !is Loaded) return@update it
                it.copy(
                        scaleAmount = null,
                        shiftAmount = null,
                        flipScale = false,
                        isConfirmShiftAndScaleDialogOpen = false,
                )
            }
            is ShiftAndScaleIntent -> handleShiftAndScaleIntent(action)
        }
    }

    private fun handleShiftAndScaleIntent(action: ShiftAndScaleIntent) {
        when (action) {
            is ShiftAndScaleIntent.Scale -> _state.update {
                (it as? Loaded)?.scaleAmount ?: return@update it
                var change = if (action.bigger) LARGE_SCALE_AMOUNT else SMALL_SCALE_AMOUNT
                if (!action.increased) change *= -1

                val newAmount = it.scaleAmount!! + change
                if (newAmount <= 0) return@update it
                it.copy(scaleAmount = newAmount)
            }
            is ShiftAndScaleIntent.Shift -> _state.update {
                (it as? Loaded)?.shiftAmount ?: return@update it
                var change = if (action.bigger) LARGE_SHIFT_AMOUNT else SMALL_SHIFT_AMOUNT
                if (!action.increased) change *= -1
                it.copy(shiftAmount = it.shiftAmount!! + change)
            }
            ShiftAndScaleIntent.FlipClicked -> _state.update {
                (it as? Loaded)?.scaleAmount ?: return@update it
                it.copy(flipScale = !it.flipScale)
            }
            ShiftAndScaleIntent.SubmitClicked ->
                _state.update { (it as? Loaded)?.copy(isConfirmShiftAndScaleDialogOpen = true) ?: it }
            ShiftAndScaleIntent.CancelSubmitClicked ->
                _state.update { (it as? Loaded)?.copy(isConfirmShiftAndScaleDialogOpen = false) ?: it }
            ShiftAndScaleIntent.ScaleReset ->
                _state.update { (it as? Loaded)?.copy(scaleAmount = ZERO_SCALE_VALUE) ?: it }
            ShiftAndScaleIntent.ShiftReset ->
                _state.update { (it as? Loaded)?.copy(shiftAmount = ZERO_SHIFT_VALUE) ?: it }
            ShiftAndScaleIntent.ConfirmSubmitClicked -> {
                val currentState = (state.value as? Loaded) ?: return
                currentState.shiftAmount ?: return
                viewModelScope.launch {
                    sightMarkRepo.update(
                            *currentState.getShiftAndScaleState()
                                    .sightMarks.map { it.asDatabaseSightMark() }
                                    .toTypedArray()
                    )
                }
                handle(EndShiftAndScale)
            }
        }
    }
}
