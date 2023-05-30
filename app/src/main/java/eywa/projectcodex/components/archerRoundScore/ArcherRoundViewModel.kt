package eywa.projectcodex.components.archerRoundScore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.archeryObjects.FullArcherRoundInfo
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent.*
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen.*
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState.Loaded
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState.Loading
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRoundsRepo
import eywa.projectcodex.database.arrowValue.ArrowValuesRepo
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArcherRoundViewModel @Inject constructor(
        val db: ScoresRoomDatabase,
        private val helpShowcase: HelpShowcaseUseCase,
        private val datastore: CodexDatastore,
) : ViewModel() {
    private val _state: MutableStateFlow<ArcherRoundState> = MutableStateFlow(Loading())
    val state = _state.asStateFlow()

    private val _effects: MutableSharedFlow<ArcherRoundEffect> =
            MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val effects: Flow<ArcherRoundEffect> = _effects

    private val arrowValuesRepo = ArrowValuesRepo(db.arrowValueDao())

    init {
        viewModelScope.launch {
            datastore.get(DatastoreKey.UseBetaFeatures).collect { useBeta ->
                _state.update {
                    when (it) {
                        is Loaded -> it.copy(useBetaFeatures = useBeta)
                        is Loading -> it.copy(useBetaFeatures = useBeta)
                    }
                }
            }
        }
    }

    fun handle(action: ArcherRoundIntent) {
        if (state.value is Loading && action !is Initialise) throw IllegalStateException()

        when (action) {
            is Initialise -> loadArcherRoundData(action)
            is NavBarClicked -> {
                _state.update {
                    it as Loaded
                    if (it.fullArcherRoundInfo.round != null && action.screen == INPUT_END
                        && (it.fullArcherRoundInfo.remainingArrows ?: 0) <= 0
                    ) {
                        it.copy(displayCannotInputEndDialog = true)
                    }
                    else {
                        helpShowcase.handle(HelpShowcaseIntent.Clear)
                        it.copy(currentScreen = action.screen)
                    }
                }
            }
            ScreenCancelClicked -> {
                helpShowcase.handle(HelpShowcaseIntent.Clear)
                _state.update { (it as Loaded).copy(currentScreen = SCORE_PAD, scorePadSelectedEnd = null) }
            }
            ScreenSubmitClicked -> (state.value as Loaded).let { state ->
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
            RoundCompleteDialogOkClicked -> {
                helpShowcase.handle(HelpShowcaseIntent.Clear)
                _state.update { (it as Loaded).copy(currentScreen = STATS, displayRoundCompletedDialog = false) }
            }
            CannotInputEndDialogOkClicked ->
                _state.update { (it as Loaded).copy(displayCannotInputEndDialog = false) }
            NoArrowsDialogOkClicked -> {
                helpShowcase.handle(HelpShowcaseIntent.Clear)
                _state.update { (it as Loaded).copy(currentScreen = INPUT_END) }
            }
            DeleteEndDialogCancelClicked ->
                _state.update {
                    (it as Loaded).copy(displayDeleteEndConfirmationDialog = false, scorePadSelectedEnd = null)
                }
            DeleteEndDialogOkClicked ->
                viewModelScope.launch {
                    _state.update {
                        it as Loaded
                        arrowValuesRepo.deleteEnd(
                                it.fullArcherRoundInfo.arrows!!,
                                it.scorePadSelectedEndFirstArrowNumber,
                                it.currentScreenEndSize,
                        )
                        it.copy(displayDeleteEndConfirmationDialog = false, scorePadSelectedEnd = null)
                    }
                }
            is HelpShowcaseAction -> helpShowcase.handle(action.action, ArcherRoundFragment::class)
        }
    }

    private fun loadArcherRoundData(action: Initialise) {
        fun Loading.tryToMoveFromLoading(): ArcherRoundState = when {
            currentScreen == null || fullArcherRoundInfo == null -> this
            // Cannot input an end into a completed round
            // TODO Investigate - not sure how this state is possible but have had multiple crashes on Google play
            (fullArcherRoundInfo.remainingArrows ?: 1) == 0 && currentScreen == INPUT_END ->
                Loaded(
                        currentScreen,
                        fullArcherRoundInfo,
                        useBetaFeatures = useBetaFeatures,
                        displayRoundCompletedDialog = true
                )
            else -> Loaded(currentScreen, fullArcherRoundInfo, useBetaFeatures = useBetaFeatures)
        }

        if (state.value is Loading) {
            check(action.screen.isMainScreen) { "Must navigate to a main screen" }
            helpShowcase.handle(HelpShowcaseIntent.Clear)
            _state.update { (it as Loading).copy(currentScreen = action.screen).tryToMoveFromLoading() }
        }

        viewModelScope.launch {
            ArcherRoundsRepo(db.archerRoundDao()).getFullArcherRoundInfo(action.archerRoundId)
                    .combine(datastore.get(DatastoreKey.Use2023HandicapSystem)) { info, system -> info to system }
                    .collect { (dbInfo, use2023System) ->
                        val info = FullArcherRoundInfo(dbInfo, use2023System)
                        _state.update {
                            when (it) {
                                is Loading -> it.copy(fullArcherRoundInfo = info).tryToMoveFromLoading()
                                is Loaded -> {
                                    var newState = it.copy(fullArcherRoundInfo = info)
                                    // If the round was just completed
                                    if ((info.remainingArrows ?: 1) <= 0
                                        && info.remainingArrows != it.fullArcherRoundInfo.remainingArrows
                                    ) {
                                        newState = newState.copy(displayRoundCompletedDialog = true)
                                    }
                                    newState
                                }
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
            _state.update { copy(newInputArrows = listOf()) }
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
            helpShowcase.handle(HelpShowcaseIntent.Clear)
            _state.update { copy(currentScreen = SCORE_PAD, scorePadSelectedEnd = null) }
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
            helpShowcase.handle(HelpShowcaseIntent.Clear)
            _state.update { copy(currentScreen = SCORE_PAD, scorePadSelectedEnd = null) }
        }
    }


    private fun handleArrowInputIntent(action: ArrowInputsIntent) {
        state.value as Loaded

        when (action) {
            is ArrowInputsIntent.ArrowInputted -> (state.value as Loaded).let {
                if (it.currentScreenInputArrows.size == it.currentScreenEndSize) {
                    viewModelScope.launch { _effects.emit(ArcherRoundEffect.Error.EndFullCannotAddMore) }
                }
                else {
                    _state.update { s ->
                        (s as Loaded).copy(newInputArrows = it.currentScreenInputArrows.plus(action.arrow))
                    }
                }
            }
            ArrowInputsIntent.BackspaceArrowsInputted -> (state.value as Loaded).let {
                if (it.currentScreenInputArrows.isEmpty()) {
                    viewModelScope.launch { _effects.emit(ArcherRoundEffect.Error.NoArrowsCannotBackSpace) }
                }
                else {
                    _state.update { s -> (s as Loaded).copy(newInputArrows = s.currentScreenInputArrows.dropLast(1)) }
                }
            }
            ArrowInputsIntent.ClearArrowsInputted -> _state.update { (it as Loaded).copy(newInputArrows = listOf()) }
            ArrowInputsIntent.ResetArrowsInputted -> _state.update { (it as Loaded).setupArrowInputsOnEditScreen() }
            is ArrowInputsIntent.HelpShowcaseAction -> helpShowcase.handle(action.action, ArcherRoundFragment::class)
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
        state.value as Loaded

        when (action) {
            ScorePadIntent.CloseDropdownMenu -> _state.update { (it as Loaded).copy(scorePadSelectedEnd = null) }
            ScorePadIntent.DeleteEndClicked ->
                _state.update { (it as Loaded).copy(displayDeleteEndConfirmationDialog = true) }
            ScorePadIntent.EditEndClicked -> {
                helpShowcase.handle(HelpShowcaseIntent.Clear)
                _state.update { (it as Loaded).copy(currentScreen = EDIT_END).setupArrowInputsOnEditScreen() }
            }
            ScorePadIntent.InsertEndClicked -> {
                helpShowcase.handle(HelpShowcaseIntent.Clear)
                _state.update { (it as Loaded).copy(currentScreen = INSERT_END, subScreenInputArrows = listOf()) }
            }
            is ScorePadIntent.RowClicked ->
                _state.update { (it as Loaded).copy(scorePadSelectedEnd = action.endNumber) }
            is ScorePadIntent.RowLongClicked ->
                _state.update { (it as Loaded).copy(scorePadSelectedEnd = action.endNumber) }
            is ScorePadIntent.HelpShowcaseAction -> helpShowcase.handle(action.action, ArcherRoundFragment::class)
        }
    }

    private fun handleSettingsIntent(action: SettingsIntent) {
        state.value as Loaded

        when (action) {
            is SettingsIntent.InputEndSizeChanged ->
                _state.update { (it as Loaded).copy(inputEndSize = action.endSize) }
            is SettingsIntent.ScorePadEndSizeChanged ->
                _state.update { (it as Loaded).copy(scorePadEndSize = action.endSize) }
            is SettingsIntent.HelpShowcaseAction -> helpShowcase.handle(action.action, ArcherRoundFragment::class)
        }
    }
}
