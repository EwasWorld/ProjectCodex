package eywa.projectcodex.components.shootDetails.headToHeadEnd.scorePad

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.get
import eywa.projectcodex.components.shootDetails.ShootDetailsError
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.headToHeadEnd.scorePad.HeadToHeadScorePadIntent.*
import eywa.projectcodex.database.ScoresRoomDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HeadToHeadScorePadViewModel @Inject constructor(
        private val db: ScoresRoomDatabase,
        private val repo: ShootDetailsRepo,
        savedStateHandle: SavedStateHandle,
        private val helpShowcase: HelpShowcaseUseCase,
) : ViewModel() {
    private val screen = CodexNavRoute.HEAD_TO_HEAD_SCORE_PAD
    private val extraState = MutableStateFlow(HeadToHeadScorePadExtras())
    val shootId = savedStateHandle.get<Int>(NavArgument.SHOOT_ID)!!

    val state = repo.getState(extraState) { main, extras ->
        HeadToHeadScorePadState(
                entries = main.fullShootInfo!!.h2h?.heats?.sortedByDescending { it.heat.heat }
                        ?: throw ShootDetailsError(),
                extras = extras,
        )
    }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            ShootDetailsResponse.Loading as ShootDetailsResponse<HeadToHeadScorePadState>,
    )

    fun handle(action: HeadToHeadScorePadIntent) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, screen::class)
            is ShootDetailsAction -> repo.handle(action.action, screen)
            GoToAddEnd -> extraState.update { it.copy(openAddHeat = true) }
            GoToAddEndHandled -> extraState.update { it.copy(openAddHeat = false) }
            is EditHeatInfo -> extraState.update { it.copy(openEditHeatInfo = action.heat) }
            is EditSighters -> extraState.update { it.copy(openSightersForHeat = action.heat) }
            EditHeatInfoHandled -> extraState.update { it.copy(openEditHeatInfo = null) }
            EditSightersHandled -> extraState.update { it.copy(openSightersForHeat = null) }
            is EditSet -> extraState.update { it.copy(openEditSetInfo = action.match to action.setNumber) }
            EditSetHandled -> extraState.update { it.copy(openEditSetInfo = null) }
        }
    }
}
