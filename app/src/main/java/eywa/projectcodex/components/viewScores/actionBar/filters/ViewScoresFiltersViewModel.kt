package eywa.projectcodex.components.viewScores.actionBar.filters

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.get
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ViewScoresFiltersViewModel @Inject constructor(
        private val viewScoresFiltersUseCase: ViewScoresFiltersUseCase,
        savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val id = savedStateHandle.get<Int>(NavArgument.FILTERS_ID)!!

    val state = viewScoresFiltersUseCase.getState(id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ViewScoresFiltersState())

    fun handle(action: ViewScoresFiltersIntent) {
        viewScoresFiltersUseCase.handle(id, action)
    }
}
