package eywa.projectcodex.components.shootDetails.stats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.get
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.EditArcherInfoClicked
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.EditArcherInfoHandled
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.EditShootClicked
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.EditShootHandled
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.HelpShowcaseAction
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.PastRecordsTabClicked
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.PastRoundRecordsClicked
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.PastRoundRecordsDismissed
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.ShootDetailsAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
        private val repo: ShootDetailsRepo,
        savedStateHandle: SavedStateHandle,
        private val helpShowcase: HelpShowcaseUseCase,
) : ViewModel() {
    private val screen = CodexNavRoute.SHOOT_DETAILS_STATS
    private val extraState = MutableStateFlow(StatsExtras())

    val state = repo.getState(
            savedStateHandle.get<Int>(NavArgument.SHOOT_ID),
            extraState,
    ) { main, extras -> StatsState(main, extras) }
            .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    ShootDetailsResponse.Loading as ShootDetailsResponse<StatsState>,
            )

    fun handle(action: StatsIntent) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, screen::class)
            is ShootDetailsAction -> repo.handle(action.action, screen)
            EditShootClicked -> extraState.update { it.copy(openEditShootScreen = true) }
            EditShootHandled -> extraState.update { it.copy(openEditShootScreen = false) }
            EditArcherInfoClicked -> extraState.update { it.copy(openEditArcherInfoScreen = true) }
            EditArcherInfoHandled -> extraState.update { it.copy(openEditArcherInfoScreen = false) }
            PastRoundRecordsClicked -> extraState.update { it.copy(isPastRoundRecordsDialogOpen = true) }
            PastRoundRecordsDismissed -> extraState.update { it.copy(isPastRoundRecordsDialogOpen = false) }
            is PastRecordsTabClicked -> extraState.update { it.copy(pastRoundScoresTab = action.tab) }
        }
    }
}
