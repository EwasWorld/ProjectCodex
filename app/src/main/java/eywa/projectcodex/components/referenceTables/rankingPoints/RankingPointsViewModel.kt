package eywa.projectcodex.components.referenceTables.rankingPoints

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RankingPointsViewModel @Inject constructor(
        useCase: RankingPointsUseCase,
        private val helpShowcase: HelpShowcaseUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(RankingPointsState(useCase))
    val state = _state.asStateFlow()

    fun handleEvent(action: RankingPointsIntent) {
        when (action) {
            is RankingPointsIntent.HelpShowcaseAction -> helpShowcase.handle(action.action)
        }
    }
}
