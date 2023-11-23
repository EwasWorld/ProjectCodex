package eywa.projectcodex.components.shootDetails.scorePad

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.get
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent.NavBarClicked
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent.SelectScorePadEnd
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadIntent.CloseDropdownMenu
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadIntent.DeleteEndClicked
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadIntent.DeleteEndDialogCancelClicked
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadIntent.DeleteEndDialogOkClicked
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadIntent.EditEndClicked
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadIntent.EditEndHandled
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadIntent.HelpShowcaseAction
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadIntent.InsertEndClicked
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadIntent.InsertEndHandled
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadIntent.NoArrowsDialogOkClicked
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadIntent.RowClicked
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadIntent.ShootDetailsAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScorePadViewModel @Inject constructor(
        private val repo: ShootDetailsRepo,
        savedStateHandle: SavedStateHandle,
        private val helpShowcase: HelpShowcaseUseCase,
) : ViewModel() {
    private val screen = CodexNavRoute.SHOOT_DETAILS_SCORE_PAD
    private val extraState = MutableStateFlow(ScorePadExtras())

    val state = repo.getState(
            savedStateHandle.get<Int>(NavArgument.SHOOT_ID),
            extraState,
    ) { main, extras -> ScorePadState(main, extras) }
            .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    ShootDetailsResponse.Loading as ShootDetailsResponse<ScorePadState>,
            )

    fun handle(action: ScorePadIntent) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, screen::class)
            is ShootDetailsAction -> repo.handle(action.action, screen)
            NoArrowsDialogOkClicked -> repo.handle(NavBarClicked(CodexNavRoute.SHOOT_DETAILS_ADD_END), screen)

            is RowClicked -> {
                repo.handle(SelectScorePadEnd(action.endNumber), screen)
                extraState.update { it.copy(isDropdownMenuOpen = true) }
            }
            CloseDropdownMenu -> repo.handle(SelectScorePadEnd(null), screen)

            is EditEndClicked -> {
                repo.handle(SelectScorePadEnd(action.endNumber), screen)
                extraState.update { it.copy(editEndClicked = true, isDropdownMenuOpen = false) }
            }
            is InsertEndClicked -> {
                repo.handle(SelectScorePadEnd(action.endNumber), screen)
                extraState.update { it.copy(insertEndClicked = true, isDropdownMenuOpen = false) }
            }
            EditEndHandled -> extraState.update { it.copy(editEndClicked = false) }
            InsertEndHandled -> extraState.update { it.copy(insertEndClicked = false) }

            is DeleteEndClicked -> {
                repo.handle(SelectScorePadEnd(action.endNumber), screen)
                extraState.update { it.copy(deleteEndDialogIsShown = true, isDropdownMenuOpen = false) }
            }
            DeleteEndDialogCancelClicked -> {
                repo.handle(SelectScorePadEnd(null), screen)
                extraState.update { it.copy(deleteEndDialogIsShown = false) }
            }
            DeleteEndDialogOkClicked -> {
                state.value.getData()?.let { currentState ->
                    if (
                        currentState.arrows == null
                        || currentState.firstArrowNumberInSelectedEnd == null
                        || currentState.selectedEndSize == null
                    ) return@let

                    viewModelScope.launch {
                        currentState.let {
                            repo.db.arrowScoresRepo().deleteEnd(
                                    it.arrows!!,
                                    it.firstArrowNumberInSelectedEnd!!,
                                    it.selectedEndSize!!,
                            )
                        }
                    }
                }
                handle(DeleteEndDialogCancelClicked)
            }
        }
    }
}
