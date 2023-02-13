package eywa.projectcodex.components.viewScores.emailScores

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcase
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailScoresViewModel @Inject constructor(
        private val helpShowcase: HelpShowcase,
) : ViewModel() {
    var state by mutableStateOf(EmailScoresState())
        private set

    private val _effects: MutableSharedFlow<EmailScoresEffect> =
            MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val effects: Flow<EmailScoresEffect> = _effects

    fun handle(action: EmailScoresIntent) {
        when (action) {
            is EmailScoresIntent.UpdateText -> {
                state = state.copy(
                        textFields = state.textFields.plus(action.type to action.value),
                        touchedFields = state.touchedFields.plus(action.type)
                )
            }
            is EmailScoresIntent.UpdateBoolean -> {
                state = state.copy(booleanFields = state.booleanFields.let {
                    if (action.value) it.plus(action.type) else it.minus(action.type)
                })
            }
            is EmailScoresIntent.SetInitialValues -> {
                state = EmailScoresState(
                        textFields = mapOf(
                                EmailScoresTextField.SUBJECT to action.subject,
                                EmailScoresTextField.MESSAGE_HEADER to action.messageHeader,
                                EmailScoresTextField.MESSAGE_FOOTER to action.messageFooter,
                        )
                )
            }
            EmailScoresIntent.CloseError -> {
                if (state.error == EmailScoresError.NO_SELECTED_ENTRIES) {
                    viewModelScope.launch { _effects.emit(EmailScoresEffect.NavigateUp) }
                }
                state = state.copy(error = null)
            }
            is EmailScoresIntent.OpenError -> state = state.copy(error = action.error)
            is EmailScoresIntent.HelpShowcaseAction -> helpShowcase.handle(action.action, EmailScoresFragment::class)
        }
    }
}
