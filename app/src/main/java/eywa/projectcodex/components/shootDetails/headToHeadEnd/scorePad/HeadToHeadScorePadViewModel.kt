package eywa.projectcodex.components.shootDetails.headToHeadEnd.scorePad

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.get
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.model.FullHeadToHead
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeadToHeadScorePadViewModel @Inject constructor(
        private val db: ScoresRoomDatabase,
        savedStateHandle: SavedStateHandle,
        private val helpShowcaseUseCase: HelpShowcaseUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<HeadToHeadScorePadState>(HeadToHeadScorePadState.Loading)
    val state = _state.asStateFlow()

    private val h2hRepo = db.h2hRepo()
    private val shootId = savedStateHandle.get<Int>(NavArgument.SHOOT_ID)!!

    init {
        viewModelScope.launch {
            h2hRepo.get(shootId).collectLatest { dbFullInfo ->
                if (dbFullInfo == null) {
                    _state.update { HeadToHeadScorePadState.Error }
                    return@collectLatest
                }

                val fullH2hInfo = FullHeadToHead(
                        headToHead = dbFullInfo.headToHead,
                        heats = dbFullInfo.heats.orEmpty(),
                        details = dbFullInfo.details.orEmpty(),
                        isEditable = false,
                )

                _state.update {
                    HeadToHeadScorePadState.Loaded(entries = fullH2hInfo.heats)
                }
            }
        }
    }

    fun handle(action: HeadToHeadScorePadIntent) {

    }
}
