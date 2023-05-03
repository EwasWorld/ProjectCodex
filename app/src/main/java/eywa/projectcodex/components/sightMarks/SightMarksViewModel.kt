package eywa.projectcodex.components.sightMarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.components.sightMarks.menu.SightMarksMenuIntent
import eywa.projectcodex.database.ScoresRoomDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SightMarksViewModel @Inject constructor(
        val db: ScoresRoomDatabase,
) : ViewModel() {
    @Deprecated("Remove when DB added")
    private val fakeSightMarkIndex = MutableStateFlow(0)

    private val _state = MutableStateFlow(SightMarksState())
    val state = _state.asStateFlow()

    init {
        // TODO Replace with DB fetch
        viewModelScope.launch {
            fakeSightMarkIndex.asStateFlow().collect { index ->
                _state.update { it.copy(sightMarks = fakeSightMarks[index]) }
            }
        }
    }

    fun handle(action: SightMarksIntent) {
        when (action) {
            is SightMarksIntent.MenuAction -> when (action.action) {
                SightMarksMenuIntent.ArchiveAll -> TODO()
                SightMarksMenuIntent.FlipDiagram ->
                    _state.update { it.copy(isHighestNumberAtTheTop = !it.isHighestNumberAtTheTop) }
                SightMarksMenuIntent.SwitchDataset ->
                    fakeSightMarkIndex.update {
                        val new = it + 1
                        if (new !in fakeSightMarks.indices) 0 else new
                    }
            }
            is SightMarksIntent.SightMarkClicked -> _state.update { it.copy(openSightMarkDetail = action.item.id) }
            SightMarksIntent.CreateSightMarkClicked -> _state.update { it.copy(createNewSightMark = true) }

            SightMarksIntent.CreateSightMarkHandled -> _state.update { it.copy(createNewSightMark = false) }
            SightMarksIntent.OpenSightMarkHandled -> _state.update { it.copy(openSightMarkDetail = null) }
        }
    }
}
