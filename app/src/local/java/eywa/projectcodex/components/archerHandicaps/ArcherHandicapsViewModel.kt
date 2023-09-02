package eywa.projectcodex.components.archerHandicaps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsIntent.*
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.hiltModules.LocalNavRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArcherHandicapsViewModel @Inject constructor(
        db: ScoresRoomDatabase,
        private val helpShowcase: HelpShowcaseUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(ArcherHandicapsState())
    val state = _state.asStateFlow()

    private val archerRepo = db.archerRepo()

    init {
        viewModelScope.launch {
            archerRepo.latestHandicapsForDefaultArcher.collectLatest { dbArcherHandicaps ->
                _state.update { it.copy(archerHandicaps = dbArcherHandicaps) }
            }
        }
    }

    fun handle(action: ArcherHandicapsIntent) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, LocalNavRoute.ARCHER_HANDICAPS::class)
            is RowClicked ->
                _state.update {
                    if (it.archerHandicaps.none { item -> item.archerHandicapId == action.item.archerHandicapId }) {
                        return@update it
                    }
                    val menuShownFor = action.item.archerHandicapId.takeIf { id -> id != it.menuShownForId }
                    it.closeAllDialogs().copy(menuShownForId = menuShownFor)
                }
            EditClicked ->
                _state.update {
                    if (it.menuShownForId == null) return@update it
                    it.closeAllDialogs().copy(menuShownForId = it.menuShownForId, editDialogOpen = !it.editDialogOpen)
                }
            AddClicked -> _state.update { it.closeAllDialogs().copy(addDialogOpen = !it.addDialogOpen) }
            is AddHandicapTextUpdated -> _state.update {
                if (!it.addDialogOpen) return@update it
                it.copy(
                        addHandicap = action.value ?: "",
                        addHandicapIsDirty = true
                )
            }
            AddSubmit -> {
                val currentState = state.value
                if (!currentState.addDialogOpen) return
                submit(currentState)
            }
            EditSubmit -> {
                val currentState = state.value
                if (!currentState.editDialogOpen || currentState.menuShownForId == null) return

                val editing = currentState.getEditingHandicap ?: return
                submit(currentState.copy(addHandicapType = editing.handicapType))
            }
            SelectHandicapTypeOpen ->
                _state.update {
                    if (!it.addDialogOpen) return@update it
                    it.copy(selectHandicapTypeDialogOpen = true)
                }
            is SelectHandicapTypeDialogItemClicked ->
                _state.update {
                    if (!it.addDialogOpen || !it.selectHandicapTypeDialogOpen) return@update it
                    it.copy(addHandicapType = action.value, selectHandicapTypeDialogOpen = false)
                }
            SelectHandicapTypeDialogClose -> _state.update { it.copy(selectHandicapTypeDialogOpen = false) }
        }
    }

    private fun ArcherHandicapsState.closeAllDialogs() = copy(
            addDialogOpen = false,
            editDialogOpen = false,
            menuShownForId = null,
            addHandicap = "",
            addHandicapIsDirty = false,
    )

    private fun submit(state: ArcherHandicapsState) {
        if (state.addHandicap.isBlank()) {
            _state.update { it.copy(addHandicapIsDirty = true) }
            return
        }
        if (state.handicapValidatorError != null) {
            return
        }

        viewModelScope.launch {
            archerRepo.insert(state.addDatabaseValue)
        }
        _state.update { it.closeAllDialogs().copy(menuShownForId = it.menuShownForId) }
    }
}
