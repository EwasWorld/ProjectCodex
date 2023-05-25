package eywa.projectcodex.components.sightMarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
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
    private val _state = MutableStateFlow(SightMarksState())
    val state = _state.asStateFlow()

    private val sightMarkRepo = SightMarkRepo(db.sightMarkDao())
    private val bowRepo = BowRepo(db.bowDao())

    init {
        viewModelScope.launch {
            sightMarkRepo.allSightMarks.collectLatest { sightMarks ->
                _state.update {
                    it.copy(sightMarks = sightMarks.map { dbSightMark -> SightMark(dbSightMark) })
                }
            }
        }
        viewModelScope.launch {
            bowRepo.defaultBow.mapNotNull { it }.collectLatest { bow ->
                _state.update { it.copy(isHighestNumberAtTheTop = bow.isSightMarkDiagramHighestAtTop) }
            }
        }
    }

    fun handle(action: SightMarksIntent) {
        when (action) {
            is SightMarksIntent.MenuAction -> when (action.action) {
                SightMarksMenuIntent.ArchiveAll -> viewModelScope.launch { sightMarkRepo.archiveAll() }
                SightMarksMenuIntent.FlipDiagram ->
                    viewModelScope.launch { bowRepo.updateDefaultBow(!state.value.isHighestNumberAtTheTop) }
            }
            is SightMarksIntent.SightMarkClicked -> _state.update { it.copy(openSightMarkDetail = action.item.id) }
            SightMarksIntent.CreateSightMarkClicked -> _state.update { it.copy(createNewSightMark = true) }

            SightMarksIntent.CreateSightMarkHandled -> _state.update { it.copy(createNewSightMark = false) }
            SightMarksIntent.OpenSightMarkHandled -> _state.update { it.copy(openSightMarkDetail = null) }
            is SightMarksIntent.HelpShowcaseAction -> helpShowcase.handle(action.action, SightMarksFragment::class)
        }
    }
}
