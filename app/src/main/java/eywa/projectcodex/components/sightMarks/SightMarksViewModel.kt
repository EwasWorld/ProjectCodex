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
                SightMarksMenuIntent.ShiftAndScale -> handle(ToggleShiftAndScale)
            }
            is SightMarkClicked -> _state.update { (it as Loaded).copy(openSightMarkDetail = action.item.id) }
            CreateSightMarkClicked -> _state.update { (it as Loaded).copy(createNewSightMark = true) }

            CreateSightMarkHandled -> _state.update { (it as Loaded).copy(createNewSightMark = false) }
            OpenSightMarkHandled -> _state.update { (it as Loaded).copy(openSightMarkDetail = null) }

            is HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.SIGHT_MARKS::class)
            is Scale -> _state.update {
                (it as? Loaded)?.scaleAmount ?: return@update it
                var change = if (action.bigger) LARGE_SCALE_AMOUNT else SMALL_SCALE_AMOUNT
                if (!action.increased) change *= -1

                val newAmount = it.scaleAmount!! + change
                if (newAmount <= 0) return@update it
                it.copy(scaleAmount = newAmount)
            }
            is Shift -> _state.update {
                (it as? Loaded)?.shiftAmount ?: return@update it
                var change = if (action.bigger) LARGE_SHIFT_AMOUNT else SMALL_SHIFT_AMOUNT
                if (!action.increased) change *= -1
                it.copy(shiftAmount = it.shiftAmount!! + change)
            }
            ShiftAndScaleFlipClicked -> _state.update {
                (it as? Loaded)?.scaleAmount ?: return@update it
                it.copy(flipScale = !it.flipScale)
            }
            ToggleShiftAndScale -> _state.update {
                if (it !is Loaded) return@update it
                if (it.scaleAmount == null) {
                    it.copy(scaleAmount = 1f, shiftAmount = 0f)
                }
                else {
                    it.copy(scaleAmount = null, shiftAmount = null)
                }
            }
            ShiftAndScaleSubmitClicked -> {
                val currentState = (state.value as? Loaded) ?: return
                currentState.shiftAmount ?: return
                viewModelScope.launch {
                    sightMarkRepo.update(
                            *currentState.getShiftAndScaleState()
                                    .sightMarks.map { it.asDatabaseSightMark() }
                                    .toTypedArray()
                    )
                }
            }
        }
    }
}
