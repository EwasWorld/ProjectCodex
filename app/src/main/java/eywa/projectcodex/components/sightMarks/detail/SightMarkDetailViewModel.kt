package eywa.projectcodex.components.sightMarks.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.DEFAULT_INT_NAV_ARG
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.sightMarks.SightMarkRepo
import eywa.projectcodex.model.SightMark
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SightMarkDetailViewModel @Inject constructor(
        val db: ScoresRoomDatabase,
        savedStateHandle: SavedStateHandle,
        private val helpShowcase: HelpShowcaseUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<SightMarkDetailState?>(null)
    val state = _state.asStateFlow()

    private val sightMarkRepo = SightMarkRepo(db.sightMarkDao())

    /**
     * When a sight mark is being edited, this job is listening for database changes and updating [_state]
     */
    private var collectSightMarkJob: Job? = null

    init {
        val editId =
                savedStateHandle.get<Int>(NavArgument.SIGHT_MARK_ID.toArgName())?.takeIf { it != DEFAULT_INT_NAV_ARG }
        if (editId == null) {
            _state.update { SightMarkDetailState() }
        }
        else {
            collectSightMarkJob = viewModelScope.launch {
                sightMarkRepo.getSightMark(editId).collectLatest { dbSightMark ->
                    val sightMark = SightMark(dbSightMark)
                    _state.update { SightMarkDetailState.fromOriginalSightMark(sightMark) }
                }
            }
        }
    }

    fun handle(action: SightMarkDetailIntent) {
        when (action) {
            is SightMarkDetailIntent.HelpShowcaseAction ->
                helpShowcase.handle(action.action, CodexNavRoute.SIGHT_MARK_DETAIL::class)

            is SightMarkDetailIntent.DistanceUpdated ->
                _state.update { it?.copy(distance = action.value, distanceIsDirty = true) }
            is SightMarkDetailIntent.SightMarkUpdated ->
                _state.update { it?.copy(sightMark = action.value, sightMarkIsDirty = true) }
            is SightMarkDetailIntent.NoteUpdated -> _state.update { it?.copy(note = action.value) }

            SightMarkDetailIntent.CloseHandled -> _state.update { it?.copy(closeScreen = false) }
            SightMarkDetailIntent.DeleteClicked -> {
                val id = state.value?.originalSightMark?.id ?: return
                collectSightMarkJob?.cancel()

                viewModelScope.launch { sightMarkRepo.delete(id) }
                _state.update { it?.copy(closeScreen = true) }
            }
            SightMarkDetailIntent.ResetClicked ->
                _state.update {
                    it?.originalSightMark?.let { original -> SightMarkDetailState.fromOriginalSightMark(original) }
                            ?: return@update it
                }
            SightMarkDetailIntent.SaveClicked -> {
                val currentState = state.value ?: return
                collectSightMarkJob?.cancel()

                val dbSightMark = currentState.asDatabaseSightMark()
                viewModelScope.launch {
                    if (currentState.originalSightMark == null) sightMarkRepo.insert(dbSightMark)
                    else sightMarkRepo.update(dbSightMark)
                }
                _state.update { it?.copy(closeScreen = true) }
            }

            SightMarkDetailIntent.ToggleIsMetric -> _state.update { it?.copy(isMetric = !it.isMetric) }
            SightMarkDetailIntent.ToggleIsArchived -> _state.update { it?.copy(isArchived = !it.isArchived) }
            SightMarkDetailIntent.ToggleIsMarked -> _state.update { it?.copy(isMarked = !it.isMarked) }
        }
    }
}
