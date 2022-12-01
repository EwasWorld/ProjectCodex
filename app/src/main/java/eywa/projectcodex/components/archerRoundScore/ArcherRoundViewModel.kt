package eywa.projectcodex.components.archerRoundScore

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.archeryObjects.FullArcherRoundInfo
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent.*
import eywa.projectcodex.components.archerRoundScore.ArcherRoundScreen.*
import eywa.projectcodex.components.archerRoundScore.ArcherRoundState.Loaded
import eywa.projectcodex.components.archerRoundScore.ArcherRoundState.Loading
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRoundsRepo
import eywa.projectcodex.database.arrowValue.ArrowValuesRepo
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArcherRoundViewModel @Inject constructor(
        val db: ScoresRoomDatabase
) : ViewModel() {
    var state: ArcherRoundState by mutableStateOf(Loading())
        private set

    private val _effects: MutableSharedFlow<ArcherRoundEffect> =
            MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val effects: Flow<ArcherRoundEffect> = _effects

    private val arrowValuesRepo = ArrowValuesRepo(db.arrowValueDao())

    fun handle(action: ArcherRoundIntent) {
        if (state is Loading && action !is Initialise) throw IllegalStateException()
        if (state is Loaded && action is Initialise) throw IllegalStateException()

        when (action) {
            is Initialise -> loadArcherRoundData(action)
            is NavBarClicked -> state = (state as Loaded).copy(currentScreen = action.screen)
            ScreenCancelClicked -> state = (state as Loaded).copy(currentScreen = SCORE_PAD, scorePadSelectedRow = null)
            ScreenSubmitClicked -> (state as Loaded).let { state ->
                when (state.currentScreen) {
                    INPUT_END -> state.commitNewEndToDb()
                    EDIT_END -> state.commitEditEndToDb()
                    INSERT_END -> state.commitInsertedEndToDb()
                    else -> throw IllegalStateException()
                }
            }
            is ArrowInputsIntent -> handleArrowInputIntent(action)
            is ScorePadIntent -> handleScorePadIntent(action)
            is SettingsIntent -> handleSettingsIntent(action)
            RoundCompleteDialogOkClicked ->
                state = (state as Loaded).copy(currentScreen = STATS, displayRoundCompletedDialog = false)
        }
    }

    private fun loadArcherRoundData(action: Initialise) {
        fun Loading.tryToMoveFromLoading(): ArcherRoundState = when {
            currentScreen == null || fullArcherRoundInfo == null -> this
            // Cannot input an end into a completed round
            (fullArcherRoundInfo.remainingArrows
                    ?: 1) == 0 && currentScreen == INPUT_END -> throw IllegalStateException()
            else -> Loaded(currentScreen, fullArcherRoundInfo)
        }

        check(action.screen.isMainScreen) { "Must navigate to a main screen" }
        state = (state as Loading).copy(currentScreen = action.screen).tryToMoveFromLoading()

        viewModelScope.launch {
            ArcherRoundsRepo(db.archerRoundDao())
                    .getFullArcherRoundInfo(action.archerRoundId)
                    .collect {
                        val info = FullArcherRoundInfo(it)
                        if (state is Loading) {
                            state = (state as Loading).copy(fullArcherRoundInfo = info).tryToMoveFromLoading()
                        }
                        else {
                            (state as Loaded).let { currentState ->
                                var newState = (state as Loaded).copy(fullArcherRoundInfo = info)
                                // If the round was just completed
                                if ((info.remainingArrows ?: 1) <= 0
                                    && info.remainingArrows != currentState.fullArcherRoundInfo.remainingArrows
                                ) {
                                    newState = newState.copy(displayRoundCompletedDialog = true)
                                }
                                state = newState
                            }
                        }
                    }
        }
    }

    private fun Loaded.commitNewEndToDb() {
        check(currentScreen == INPUT_END)

        var arrowNumber = fullArcherRoundInfo.arrows?.maxOfOrNull { it.arrowNumber } ?: 0
        val arrows = inputArrows.map {
            it.toArrowValue(fullArcherRoundInfo.id, ++arrowNumber)
        }
        check(arrows.size <= currentScreenEndSize) { "Too many arrows have been inputted" }

        viewModelScope.launch {
            arrowValuesRepo.insert(*arrows.toTypedArray())
            _effects.emit(ArcherRoundEffect.NavigateUp)
        }
    }

    private fun Loaded.commitEditEndToDb() {
        check(currentScreen == EDIT_END)

        var arrowNumber = scorePadSelectedRowFirstArrowNumber
        val arrows = subScreenInputArrows.map { arrow ->
            arrow.toArrowValue(fullArcherRoundInfo.id, arrowNumber++)
        }

        check(arrows.size <= currentScreenEndSize) { "Too many arrows have been marked for edit" }

        viewModelScope.launch {
            arrowValuesRepo.update(*arrows.toTypedArray())
            _effects.emit(ArcherRoundEffect.NavigateUp)
        }
    }

    private fun Loaded.commitInsertedEndToDb() {
        check(currentScreen == INSERT_END)

        var arrowNumber = scorePadSelectedRowFirstArrowNumber
        val arrows = subScreenInputArrows.map { arrow ->
            arrow.toArrowValue(fullArcherRoundInfo.id, arrowNumber++)
        }
        check(arrows.size <= currentScreenEndSize) { "Too many arrows have been inputted" }

        viewModelScope.launch {
            arrowValuesRepo.insertEnd(fullArcherRoundInfo.arrows!!, arrows)
            _effects.emit(ArcherRoundEffect.NavigateUp)
        }
    }


    private fun handleArrowInputIntent(action: ArrowInputsIntent) {
        state as Loaded

        when (action) {
            is ArrowInputsIntent.ArrowInputted -> (state as Loaded).let {
                if (it.inputArrows.size == it.currentScreenEndSize) {
                    viewModelScope.launch { _effects.emit(ArcherRoundEffect.Error.EndFullCannotAddMore) }
                }
                else {
                    state = it.copy(newInputArrows = it.inputArrows.plus(action.arrow))
                }
            }
            ArrowInputsIntent.BackspaceArrowsInputted -> (state as Loaded).let {
                if (it.currentScreenEndSize == 0) {
                    viewModelScope.launch { _effects.emit(ArcherRoundEffect.Error.NoArrowsCannotBackSpace) }
                }
                else {
                    state = it.copy(newInputArrows = (state as Loaded).inputArrows.dropLast(1))
                }
            }
            ArrowInputsIntent.ClearArrowsInputted -> state = (state as Loaded).copy(newInputArrows = listOf())
            ArrowInputsIntent.ResetArrowsInputted -> state = (state as Loaded).setupArrowInputsOnEditScreen()
        }
    }

    private fun Loaded.setupArrowInputsOnEditScreen(): Loaded {
        check(currentScreen == EDIT_END)
        return copy(
                subScreenInputArrows = fullArcherRoundInfo.arrows!!.subList(
                        scorePadSelectedRowFirstArrowNumber,
                        scorePadSelectedRowFirstArrowNumber + currentScreenEndSize
                ).map { arrow -> Arrow(arrow.score, arrow.isX) }
        )
    }

    private fun handleScorePadIntent(action: ScorePadIntent) {
        state as Loaded

        when (action) {
            ScorePadIntent.CloseDropdownMenu ->
                state = (state as Loaded).copy(scorePadDropdownOpenForEndNumber = null)
            ScorePadIntent.DeleteEndClicked -> viewModelScope.launch {
                (state as Loaded).let {
                    arrowValuesRepo.deleteEnd(
                            it.fullArcherRoundInfo.arrows!!,
                            it.scorePadSelectedRowFirstArrowNumber,
                            it.currentScreenEndSize,
                    )
                }
                _effects.emit(ArcherRoundEffect.NavigateUp)
            }
            ScorePadIntent.EditEndClicked -> state = (state as Loaded)
                    .copy(currentScreen = EDIT_END)
                    .setupArrowInputsOnEditScreen()
            ScorePadIntent.InsertEndClicked -> state = (state as Loaded)
                    .copy(currentScreen = EDIT_END, subScreenInputArrows = listOf())
            is ScorePadIntent.RowClicked ->
                state = (state as Loaded).copy(scorePadSelectedRow = action.endNumber)
            is ScorePadIntent.RowLongClicked ->
                state = (state as Loaded).copy(scorePadSelectedRow = action.endNumber)
        }
    }

    private fun handleSettingsIntent(action: SettingsIntent) {
        state as Loaded

        when (action) {
            is SettingsIntent.InputEndSizeChanged -> state = (state as Loaded).copy(inputEndSize = action.endSize)
            is SettingsIntent.ScorePadEndSizeChanged -> state = (state as Loaded).copy(scorePadEndSize = action.endSize)
        }
    }
}