package eywa.projectcodex.components.shootDetails.headToHead.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.utils.classificationTables.ClassificationTablesUseCase
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.headToHead.stats.HeadToHeadStatsIntent.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HeadToHeadStatsViewModel @Inject constructor(
        private val repo: ShootDetailsRepo,
        private val helpShowcaseUseCase: HelpShowcaseUseCase,
        private val classificationTablesUseCase: ClassificationTablesUseCase,
) : ViewModel() {
    private val screen = CodexNavRoute.HEAD_TO_HEAD_STATS
    private val extraState = MutableStateFlow(HeadToHeadStatsState.Extras())

    val state = repo.getState(extraState, ::stateConverter)
            .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    ShootDetailsResponse.Loading as ShootDetailsResponse<HeadToHeadStatsState>,
            )

    init {
        viewModelScope.launch {
            state
                    .map { s ->
                        s.getData()
                                ?.let { it.fullShootInfo.shoot.dateShot to it.fullShootInfo.shootRound?.roundId }
                                ?: (null to null)
                    }
                    .distinctUntilChanged()
                    .flatMapLatest { (date, roundId) ->
                        if (date != null && roundId != null) repo.db.shootsRepo().getQualifyingRoundId(date, roundId)
                        else flow { emit(null) }
                    }
                    .collectLatest { extraState.update { s -> s.copy(qualifyingRoundId = it) } }
        }
    }

    private fun stateConverter(
            main: ShootDetailsState,
            extras: HeadToHeadStatsState.Extras,
    ): HeadToHeadStatsState {
        return HeadToHeadStatsState(
                fullShootInfo = main.fullShootInfo!!,
                classificationTablesUseCase = classificationTablesUseCase,
                extras = extras,
                archerInfo = main.archerInfo,
                bow = main.bow,
                wa1440FullRoundInfo = main.wa1440FullRoundInfo,
        )
    }

    fun handle(action: HeadToHeadStatsIntent) {
        when (action) {
            is HelpShowcaseAction -> helpShowcaseUseCase.handle(action.action, screen::class)
            is ShootDetailsAction -> repo.handle(action.action, screen, viewModelScope)

            EditArcherInfoClicked -> extraState.update { it.copy(editArcherInfo = true) }
            EditArcherInfoHandled -> extraState.update { it.copy(editArcherInfo = false) }
            EditMainInfoClicked -> extraState.update { it.copy(editMainInfo = true) }
            EditMainInfoHandled -> extraState.update { it.copy(editMainInfo = false) }
            ExpandClassificationsClicked -> extraState.update { it.copy(expandClassifications = true) }
            ExpandClassificationsHandled -> extraState.update { it.copy(expandClassifications = false) }
            ExpandHandicapsClicked -> extraState.update { it.copy(expandClassifications = true) }
            ExpandHandicapsHandled -> extraState.update { it.copy(expandClassifications = false) }
            ViewQuailfyingRoundClicked -> extraState.update { it.copy(viewQualifyingRound = true) }
            ViewQuailfyingRoundHandled -> extraState.update { it.copy(viewQualifyingRound = false) }
        }
    }
}
