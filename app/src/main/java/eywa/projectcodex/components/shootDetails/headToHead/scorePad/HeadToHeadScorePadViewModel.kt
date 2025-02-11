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
import eywa.projectcodex.components.shootDetails.headToHead.grid.SetDropdownMenuItem
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
            is EditSighters -> extraState.update { it.copy(openEditSightersForMatch = action.match) }
            EditSightersHandled -> extraState.update { it.copy(openEditSightersForMatch = null) }

            is SetClicked -> extraState.update { it.copy(menuOpenForSet = action.match to action.setNumber) }
            is CloseSetOptionsMenu -> extraState.update {
                if (it.menuOpenForSet != action.match to action.setNumber) it
                else it.copy(menuOpenForSet = null, setMenuActionClicked = null)
            }

            SetOptionsMenuActionHandled -> extraState.update {
                it.copy(menuOpenForSet = null, setMenuActionClicked = null)
            }

            is SetOptionsMenuClicked -> extraState.update {
                if (it.menuOpenForSet != action.match to action.setNumber) it
                else it.copy(setMenuActionClicked = action.dropdownItem.asAction())
            }

            DeleteConfirmationCancelClicked -> extraState.update {
                if (it.menuOpenForSet != null) it.copy(setMenuActionClicked = null)
                else if (it.menuOpenForMatchNumber != null) it.copy(matchMenuActionClicked = null)
                else it
            }

            DeleteConfirmationOkClicked -> extraState.update {

                if (it.menuOpenForSet != null && it.setMenuActionClicked == MenuAction.DELETE) {
                    viewModelScope.launch {
                        db.h2hRepo().delete(
                                shootId = shootId,
                                matchNumber = it.menuOpenForSet.first,
                                setNumber = it.menuOpenForSet.second,
                        )
                    }
                    it.copy(menuOpenForSet = null, setMenuActionClicked = null)

                }
                else if (it.menuOpenForMatchNumber != null && it.matchMenuActionClicked == MenuAction.DELETE) {
                    viewModelScope.launch {
                        db.h2hRepo().delete(shootId = shootId, matchNumber = it.menuOpenForMatchNumber)
                    }
                    it.copy(menuOpenForMatchNumber = null, matchMenuActionClicked = null)

                }
                else {
                    it
                }
            }

            is CloseMatchOptionsMenu -> extraState.update {
                if (it.menuOpenForMatchNumber != action.match) it
                else it.copy(menuOpenForMatchNumber = null, matchMenuActionClicked = null)
            }

            MatchOptionsMenuActionHandled ->
                extraState.update { it.copy(menuOpenForMatchNumber = null, matchMenuActionClicked = null) }

            is MatchOptionsMenuClicked -> extraState.update {
                if (it.menuOpenForMatchNumber != action.match) it
                else it.copy(matchMenuActionClicked = action.dropdownItem.asAction())
            }

            is OpenMatchOptionsClicked -> extraState.update { it.copy(menuOpenForMatchNumber = action.match) }
        }
    }

    private fun SetDropdownMenuItem.asAction() = when (this) {
        SetDropdownMenuItem.EDIT -> MenuAction.EDIT
        SetDropdownMenuItem.DELETE -> MenuAction.DELETE
        SetDropdownMenuItem.INSERT -> MenuAction.INSERT
    }

    private fun MatchDropdownMenuItem.asAction() = when (this) {
        MatchDropdownMenuItem.EDIT -> MenuAction.EDIT
        MatchDropdownMenuItem.DELETE -> MenuAction.DELETE
        MatchDropdownMenuItem.INSERT -> MenuAction.INSERT
        MatchDropdownMenuItem.CONTINUE -> MenuAction.NEW_SET
    }
}
