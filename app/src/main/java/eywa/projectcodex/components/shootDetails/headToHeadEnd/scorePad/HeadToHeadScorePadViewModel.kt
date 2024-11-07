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
import eywa.projectcodex.database.ScoresRoomDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HeadToHeadScorePadViewModel @Inject constructor(
        private val db: ScoresRoomDatabase,
        private val repo: ShootDetailsRepo,
        savedStateHandle: SavedStateHandle,
        private val helpShowcase: HelpShowcaseUseCase,
) : ViewModel() {
    private val screen = CodexNavRoute.HEAD_TO_HEAD_SCORE_PAD

    val state = repo.getState {
        HeadToHeadScorePadState(it.fullShootInfo!!.h2h?.heats ?: throw ShootDetailsError())
    }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            ShootDetailsResponse.Loading as ShootDetailsResponse<HeadToHeadScorePadState>,
    )

    private val h2hRepo = db.h2hRepo()
    private val shootId = savedStateHandle.get<Int>(NavArgument.SHOOT_ID)!!

    fun handle(action: HeadToHeadScorePadIntent) {
        when (action) {
            is HeadToHeadScorePadIntent.HelpShowcaseAction -> helpShowcase.handle(action.action, screen::class)
            is HeadToHeadScorePadIntent.ShootDetailsAction -> repo.handle(action.action, screen)
        }
    }
}
