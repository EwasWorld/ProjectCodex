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
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen.*
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState.Loaded
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState.Loading
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
            is NavBarClicked -> {
                val s = state as Loaded
                if (action.screen == INPUT_END && (s.fullArcherRoundInfo.remainingArrows ?: 0) <= 0) {
                    state = s.copy(displayCannotInputEndDialog = true)
                }
                else {
                    state = s.copy(currentScreen = action.screen)
                }
            }
            ScreenCancelClicked -> state = (state as Loaded).copy(currentScreen = SCORE_PAD, scorePadSelectedEnd = null)
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
            CannotInputEndDialogOkClicked ->
                state = (state as Loaded).copy(displayCannotInputEndDialog = false)
            NoArrowsDialogOkClicked -> state = (state as Loaded).copy(currentScreen = INPUT_END)
            DeleteEndDialogCancelClicked ->
                state = (state as Loaded).copy(displayDeleteEndConfirmationDialog = false, scorePadSelectedEnd = null)
            DeleteEndDialogOkClicked -> viewModelScope.launch {
                (state as Loaded).let {
                    arrowValuesRepo.deleteEnd(
                            it.fullArcherRoundInfo.arrows!!,
                            it.scorePadSelectedEndFirstArrowNumber,
                            it.currentScreenEndSize,
                    )
                    state = it.copy(displayDeleteEndConfirmationDialog = false, scorePadSelectedEnd = null)
                }
            }
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
        val arrows = currentScreenInputArrows.map {
            it.toArrowValue(fullArcherRoundInfo.id, ++arrowNumber)
        }
        check(arrows.size <= currentScreenEndSize) { "Too many arrows have been inputted" }

        viewModelScope.launch {
            if (arrows.size < currentScreenEndSize) {
                _effects.emit(ArcherRoundEffect.Error.NotEnoughArrowsInputted)
                return@launch
            }

            arrowValuesRepo.insert(*arrows.toTypedArray())
            state = copy(newInputArrows = listOf())
        }
    }

    private fun Loaded.commitEditEndToDb() {
        check(currentScreen == EDIT_END)

        var arrowNumber = scorePadSelectedEndFirstArrowNumber
        val arrows = currentScreenInputArrows.map { arrow ->
            arrow.toArrowValue(fullArcherRoundInfo.id, arrowNumber++)
        }

        check(arrows.size <= currentScreenEndSize) { "Too many arrows have been marked for edit" }

        viewModelScope.launch {
            if (arrows.size < currentScreenEndSize) {
                _effects.emit(ArcherRoundEffect.Error.NotEnoughArrowsInputted)
                return@launch
            }

            arrowValuesRepo.update(*arrows.toTypedArray())
            state = copy(currentScreen = SCORE_PAD, scorePadSelectedEnd = null)
        }
    }

    private fun Loaded.commitInsertedEndToDb() {
        check(currentScreen == INSERT_END)

        var arrowNumber = scorePadSelectedEndFirstArrowNumber
        val arrows = currentScreenInputArrows.map { arrow ->
            arrow.toArrowValue(fullArcherRoundInfo.id, arrowNumber++)
        }
        check(arrows.size <= currentScreenEndSize) { "Too many arrows have been inputted" }

        viewModelScope.launch {
            arrowValuesRepo.insertEnd(fullArcherRoundInfo.arrows!!, arrows)
            state = copy(currentScreen = SCORE_PAD, scorePadSelectedEnd = null)
        }
    }


    private fun handleArrowInputIntent(action: ArrowInputsIntent) {
        state as Loaded

        when (action) {
            is ArrowInputsIntent.ArrowInputted -> (state as Loaded).let {
                if (it.currentScreenInputArrows.size == it.currentScreenEndSize) {
                    viewModelScope.launch { _effects.emit(ArcherRoundEffect.Error.EndFullCannotAddMore) }
                }
                else {
                    state = it.copy(newInputArrows = it.currentScreenInputArrows.plus(action.arrow))
                }
            }
            ArrowInputsIntent.BackspaceArrowsInputted -> (state as Loaded).let {
                if (it.currentScreenInputArrows.isEmpty()) {
                    viewModelScope.launch { _effects.emit(ArcherRoundEffect.Error.NoArrowsCannotBackSpace) }
                }
                else {
                    state = it.copy(newInputArrows = (state as Loaded).currentScreenInputArrows.dropLast(1))
                }
            }
            ArrowInputsIntent.ClearArrowsInputted -> state = (state as Loaded).copy(newInputArrows = listOf())
            ArrowInputsIntent.ResetArrowsInputted -> state = (state as Loaded).setupArrowInputsOnEditScreen()
        }
    }

    private fun Loaded.setupArrowInputsOnEditScreen(): Loaded {
        check(currentScreen == EDIT_END)

        // -1 because arrowNumbers are 1-indexed
        return copy(
                subScreenInputArrows = fullArcherRoundInfo.arrows!!.subList(
                        scorePadSelectedEndFirstArrowNumber - 1,
                        scorePadSelectedEndFirstArrowNumber - 1 + currentScreenEndSize
                ).map { arrow -> Arrow(arrow.score, arrow.isX) }
        )
    }

    private fun handleScorePadIntent(action: ScorePadIntent) {
        state as Loaded

        when (action) {
            ScorePadIntent.CloseDropdownMenu -> state = (state as Loaded).copy(scorePadSelectedEnd = null)
            ScorePadIntent.DeleteEndClicked -> state = (state as Loaded).copy(displayDeleteEndConfirmationDialog = true)
            ScorePadIntent.EditEndClicked -> state = (state as Loaded)
                    .copy(currentScreen = EDIT_END)
                    .setupArrowInputsOnEditScreen()
            ScorePadIntent.InsertEndClicked -> state = (state as Loaded)
                    .copy(currentScreen = INSERT_END, subScreenInputArrows = listOf())
            is ScorePadIntent.RowClicked ->
                state = (state as Loaded).copy(scorePadSelectedEnd = action.endNumber)
            is ScorePadIntent.RowLongClicked ->
                state = (state as Loaded).copy(scorePadSelectedEnd = action.endNumber)
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