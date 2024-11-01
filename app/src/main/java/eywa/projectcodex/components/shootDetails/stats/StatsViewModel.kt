package eywa.projectcodex.components.shootDetails.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.utils.classificationTables.ClassificationTablesUseCase
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
        private val repo: ShootDetailsRepo,
        private val helpShowcase: HelpShowcaseUseCase,
        private val classificationTables: ClassificationTablesUseCase,
) : ViewModel() {
    private val screen = CodexNavRoute.SHOOT_DETAILS_STATS
    private val extraState = MutableStateFlow(StatsExtras())

    val state = repo.getState(extraState) { main, extras -> StatsState(main, extras, classificationTables) }
            .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(1000),
                    ShootDetailsResponse.Loading as ShootDetailsResponse<StatsState>,
            )

    fun handle(action: StatsIntent) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, screen::class)
            is ShootDetailsAction -> repo.handle(action.action, screen, viewModelScope)
            EditShootClicked -> extraState.update { it.copy(openEditShootScreen = true) }
            EditShootHandled -> extraState.update { it.copy(openEditShootScreen = false) }
            EditArcherInfoClicked -> extraState.update { it.copy(openEditArcherInfoScreen = true) }
            EditArcherInfoHandled -> extraState.update { it.copy(openEditArcherInfoScreen = false) }
            EditHandicapInfoClicked -> extraState.update { it.copy(openEditHandicapInfoScreen = true) }
            EditHandicapInfoHandled -> extraState.update { it.copy(openEditHandicapInfoScreen = false) }
            PastRoundRecordsClicked -> extraState.update { it.copy(isPastRoundRecordsDialogOpen = true) }
            PastRoundRecordsDismissed -> extraState.update { it.copy(isPastRoundRecordsDialogOpen = false) }
            is PastRecordsTabClicked -> extraState.update { it.copy(pastRoundScoresTab = action.tab) }
            ExpandHandicapsClicked -> extraState.update { it.copy(openHandicapTablesScreen = true) }
            ExpandHandicapsHandled -> extraState.update { it.copy(openHandicapTablesScreen = false) }
            ExpandClassificationsClicked -> extraState.update { it.copy(openClassificationTablesScreen = true) }
            ExpandClassificationsHandled -> extraState.update { it.copy(openClassificationTablesScreen = false) }
        }
    }
}
