package eywa.projectcodex.components.referenceTables.headToHead

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HeadToHeadReferenceViewModel @Inject constructor(
        val helpShowcase: HelpShowcaseUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(HeadToHeadReferenceState())
    val state = _state.asStateFlow()

    fun handleEvent(action: HeadToHeadReferenceIntent) {
        when (action) {
            is HeadToHeadReferenceIntent.ArcherRankChanged ->
                _state.update { it.copy(archerRank = it.archerRank.onTextChanged(action.value)) }

            is HeadToHeadReferenceIntent.OpponentRankChanged ->
                _state.update { it.copy(opponentRank = it.opponentRank.onTextChanged(action.value)) }

            is HeadToHeadReferenceIntent.TotalArchersChanged ->
                _state.update { it.copy(totalArchers = it.totalArchers.onTextChanged(action.value)) }

            is HeadToHeadReferenceIntent.HelpShowcaseAction ->
                helpShowcase.handle(action.action, CodexNavRoute.HEAD_TO_HEAD_REF::class)
        }
    }
}
