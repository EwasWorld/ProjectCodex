package eywa.projectcodex.components.archerHandicaps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsIntent.AddClicked
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsIntent.AddHandled
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsIntent.DeleteClicked
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsIntent.DeleteDialogCancelClicked
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsIntent.DeleteDialogOkClicked
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsIntent.HelpShowcaseAction
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsIntent.RowClicked
import eywa.projectcodex.database.ScoresRoomDatabase
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
    private val bowRepo = db.bowRepo()

    init {
        viewModelScope.launch {
            archerRepo.latestHandicapsForDefaultArcher.collectLatest { dbArcherHandicaps ->
                _state.update { it.copy(currentHandicaps = dbArcherHandicaps) }
            }
        }
        viewModelScope.launch {
            archerRepo.allHandicapsForDefaultArcher.collectLatest { dbArcherHandicaps ->
                _state.update { it.copy(allHandicaps = dbArcherHandicaps) }
            }
        }
        viewModelScope.launch {
            bowRepo.defaultBow.collectLatest { dbBow ->
                dbBow?.let { bow ->
                    _state.update { it.copy(selectedBowStyle = bow.type) }
                }
            }
        }
    }

    fun handle(action: ArcherHandicapsIntent) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.ARCHER_HANDICAPS::class)
            is RowClicked ->
                _state.update {
                    if (it.allHandicaps.orEmpty()
                                .none { item -> item.archerHandicapId == action.item.archerHandicapId }
                    ) {
                        return@update it
                    }
                    val menuShownFor = action.item.archerHandicapId.takeIf { id -> id != it.menuShownForId }
                    it.closeAllDialogs().copy(menuShownForId = menuShownFor)
                }

            AddClicked -> _state.update { it.closeAllDialogs().copy(openAddDialog = true) }
            AddHandled -> _state.update { it.copy(openAddDialog = false) }

            DeleteClicked -> _state.update { it.copy(deleteDialogOpen = true) }
            DeleteDialogCancelClicked -> _state.update { it.closeAllDialogs() }
            DeleteDialogOkClicked -> {
                viewModelScope.launch {
                    archerRepo.deleteHandicap(state.value.menuShownForId!!)
                }
                _state.update { it.closeAllDialogs() }
            }
        }
    }

    private fun ArcherHandicapsState.closeAllDialogs() = copy(
            deleteDialogOpen = false,
            menuShownForId = null,
    )
}
