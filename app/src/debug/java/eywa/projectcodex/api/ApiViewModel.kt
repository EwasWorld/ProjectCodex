package eywa.projectcodex.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ApiState(
        val response: DataState<List<ScoresRf.Score>?, Exception> = DataState.Loading,
)

@HiltViewModel
class ApiViewModel @Inject constructor(
        private val scoresApi: ScoresRf.Service,
) : ViewModel() {
    private val _state = MutableStateFlow(ApiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { ApiState(scoresApi.getScores()) }
        }
    }
}
