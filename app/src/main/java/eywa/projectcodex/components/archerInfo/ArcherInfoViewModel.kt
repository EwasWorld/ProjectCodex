package eywa.projectcodex.components.archerInfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.components.archerInfo.ArcherInfoIntent.AgeClicked
import eywa.projectcodex.components.archerInfo.ArcherInfoIntent.AgeSelected
import eywa.projectcodex.components.archerInfo.ArcherInfoIntent.BowClicked
import eywa.projectcodex.components.archerInfo.ArcherInfoIntent.BowSelected
import eywa.projectcodex.components.archerInfo.ArcherInfoIntent.CloseDropdown
import eywa.projectcodex.components.archerInfo.ArcherInfoIntent.HelpShowcaseAction
import eywa.projectcodex.components.archerInfo.ArcherInfoIntent.SetIsGent
import eywa.projectcodex.database.ScoresRoomDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArcherInfoViewModel @Inject constructor(
        db: ScoresRoomDatabase,
        private val helpShowcase: HelpShowcaseUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(ArcherInfoState())
    val state = _state.asStateFlow()

    private val archerRepo = db.archerRepo()
    private val bowRepo = db.bowRepo()

    init {
        viewModelScope.launch {
            archerRepo.defaultArcher.collect { archer ->
                _state.update { it.copy(defaultArcher = archer) }
            }
        }
        viewModelScope.launch {
            bowRepo.defaultBow.collect { bow ->
                _state.update { it.copy(bow = bow?.type ?: ClassificationBow.RECURVE) }
            }
        }
    }

    fun handle(action: ArcherInfoIntent) {
        when (action) {
            is SetIsGent -> viewModelScope.launch { archerRepo.updateDefaultArcher(isGent = action.isGent) }

            is AgeSelected -> {
                viewModelScope.launch { archerRepo.updateDefaultArcher(age = action.age) }
                _state.update { it.copy(expanded = null) }
            }

            is BowSelected -> {
                viewModelScope.launch { bowRepo.updateDefaultBow(type = action.bow) }
                _state.update { it.copy(expanded = null) }
            }

            AgeClicked -> _state.update { it.copy(expanded = ArcherInfoState.Dropdown.AGE) }
            BowClicked -> _state.update { it.copy(expanded = ArcherInfoState.Dropdown.BOW) }
            CloseDropdown -> _state.update { it.copy(expanded = null) }
            is HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.ARCHER_INFO::class)
        }
    }
}
