package eywa.projectcodex.components.archerHandicaps.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.components.archerHandicaps.add.ArcherHandicapsAddIntent.CloseHandled
import eywa.projectcodex.components.archerHandicaps.add.ArcherHandicapsAddIntent.DateChanged
import eywa.projectcodex.components.archerHandicaps.add.ArcherHandicapsAddIntent.HandicapTextUpdated
import eywa.projectcodex.components.archerHandicaps.add.ArcherHandicapsAddIntent.SubmitPressed
import eywa.projectcodex.database.ScoresRoomDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ArcherHandicapsAddViewModel @Inject constructor(
        db: ScoresRoomDatabase,
) : ViewModel() {
    private val _state = MutableStateFlow(ArcherHandicapsAddState())
    val state = _state.asStateFlow()

    private val archerRepo = db.archerRepo()

    fun handle(action: ArcherHandicapsAddIntent) {
        when (action) {
            is HandicapTextUpdated -> _state.update {
                it.copy(handicap = it.handicap.onTextChanged(action.value))
            }

            SubmitPressed -> submit(state.value)
            is DateChanged -> _state.update { it.copy(date = action.info.updateCalendar(it.date)) }
            CloseHandled -> _state.update { it.copy(shouldCloseDialog = false) }
        }
    }

    private fun submit(state: ArcherHandicapsAddState) {
        if (state.handicap.error != null) {
            return
        }
        if (!state.handicap.isDirty && state.handicap.parsed == null) {
            _state.update { it.copy(handicap = it.handicap.markDirty()) }
            return
        }

        viewModelScope.launch {
            archerRepo.insert(state.addDatabaseValue)
        }
        _state.update { it.copy(shouldCloseDialog = true) }
    }
}
