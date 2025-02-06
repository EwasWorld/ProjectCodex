package eywa.projectcodex.components.shootDetails.headToHead.scorePad

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
import eywa.projectcodex.components.shootDetails.headToHead.grid.DropdownMenuItem
import eywa.projectcodex.components.shootDetails.headToHead.scorePad.HeadToHeadScorePadIntent.*
import eywa.projectcodex.database.ScoresRoomDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
                entries = main.fullShootInfo!!.h2h?.matches ?: throw ShootDetailsError(),
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
            GoToAddEnd -> extraState.update { it.copy(openAddMatch = true) }
            GoToAddEndHandled -> extraState.update { it.copy(openAddMatch = false) }
            is EditMatchInfo -> extraState.update { it.copy(openEditMatchInfo = action.match) }
            is EditSighters -> extraState.update { it.copy(openEditSightersForMatch = action.match) }
            EditMatchInfoHandled -> extraState.update { it.copy(openEditMatchInfo = null) }
            EditSightersHandled -> extraState.update { it.copy(openEditSightersForMatch = null) }
            is AddNewSet -> extraState.update {
                it.copy(menuOpenForSet = action.match to -1, menuActionClicked = MenuAction.NEW_SET)
            }

            is SetClicked -> extraState.update { it.copy(menuOpenForSet = action.match to action.setNumber) }
            is CloseSetOptionsMenu -> extraState.update {
                if (it.menuOpenForSet != action.match to action.setNumber) it
                else it.copy(menuOpenForSet = null, menuActionClicked = null)
            }

            OptionsMenuActionHandled -> extraState.update { it.copy(menuOpenForSet = null, menuActionClicked = null) }
            is OptionsMenuClicked -> extraState.update {
                if (it.menuOpenForSet != action.match to action.setNumber) it
                else it.copy(menuActionClicked = action.dropdownItem.asAction())
            }

            DeleteConfirmationCancelClicked -> extraState.update {
                if (it.menuOpenForSet == null) it
                else it.copy(menuActionClicked = null)
            }

            DeleteConfirmationOkClicked -> extraState.update {
                if (it.menuOpenForSet == null || it.menuActionClicked != MenuAction.DELETE) return@update it

                viewModelScope.launch {
                    db.h2hRepo().delete(shootId, it.menuOpenForSet.first, it.menuOpenForSet.second)
                }
                it.copy(menuOpenForSet = null, menuActionClicked = null)
            }

        }
    }

    private fun DropdownMenuItem.asAction() = when (this) {
        DropdownMenuItem.EDIT -> MenuAction.EDIT
        DropdownMenuItem.DELETE -> MenuAction.DELETE
        DropdownMenuItem.INSERT -> MenuAction.INSERT
    }
}
