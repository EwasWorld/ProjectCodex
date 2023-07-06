package eywa.projectcodex.components.viewScores.emailScores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.archeryObjects.FullArcherRoundInfo
import eywa.projectcodex.common.diActivityHelpers.ArcherRoundIdsUseCase
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.components.viewScores.emailScores.EmailScoresIntent.*
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRoundsRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EmailScoresViewModel @Inject constructor(
        private val helpShowcase: HelpShowcaseUseCase,
        db: ScoresRoomDatabase,
        private val archerRoundIdsUseCase: ArcherRoundIdsUseCase,
) : ViewModel() {
    private val repo = ArcherRoundsRepo(db.archerRoundDao())

    // TODO_CURRENT add loading state
    private val _state = MutableStateFlow(EmailScoresState())
    val state = _state.asStateFlow()

    // TODO_CURRENT Remove effects
    private val _effects: MutableSharedFlow<EmailScoresEffect> =
            MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val effects: Flow<EmailScoresEffect> = _effects

    init {
        viewModelScope.launch {
            archerRoundIdsUseCase.getItems.flatMapLatest {
                if (it == null) emptyFlow<List<FullArcherRoundInfo>?>()
                else repo.getFullArcherRoundInfo(it)
            }
        }
    }

    fun handle(action: EmailScoresIntent) {
        when (action) {
            is UpdateText ->
                _state.update {
                    it.copy(
                            textFields = it.textFields.plus(action.type to action.value),
                            touchedFields = it.touchedFields.plus(action.type)
                    )
                }
            is UpdateBoolean ->
                _state.update {
                    it.copy(booleanFields = it.booleanFields.let { field ->
                        if (action.value) field.plus(action.type) else field.minus(action.type)
                    })
                }
            is SetInitialValues ->
                _state.update {
                    EmailScoresState(
                            textFields = mapOf(
                                    EmailScoresTextField.SUBJECT to action.subject,
                                    EmailScoresTextField.MESSAGE_HEADER to action.messageHeader,
                                    EmailScoresTextField.MESSAGE_FOOTER to action.messageFooter,
                            )
                    )
                }
            CloseError -> {
                if (state.value.error == EmailScoresError.NO_SELECTED_ENTRIES) {
                    viewModelScope.launch { _effects.emit(EmailScoresEffect.NavigateUp) }
                    _state.update { it.copy(error = null) }
                }
            }
            is OpenError -> _state.update { it.copy(error = action.error) }
            is HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.EMAIL_SCORE::class)
            SubmitClicked -> TODO()
        }
    }
}
