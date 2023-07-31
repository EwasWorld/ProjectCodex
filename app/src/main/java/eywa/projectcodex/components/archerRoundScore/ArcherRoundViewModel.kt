package eywa.projectcodex.components.archerRoundScore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent.*
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen.*
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState.*
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.arrowValue.ArrowValuesRepo
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.FullArcherRoundInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArcherRoundViewModel @Inject constructor(
        db: ScoresRoomDatabase,
        private val helpShowcase: HelpShowcaseUseCase,
        datastore: CodexDatastore,
        savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state: MutableStateFlow<ArcherRoundState> = MutableStateFlow(
            Loading(savedStateHandle.get<String>("screen")
                    ?.let { ArcherRoundScreen.valueOf(it) } ?: SCORE_PAD)
    )
    val state = _state.asStateFlow()

    private val arrowValuesRepo = ArrowValuesRepo(db.arrowValueDao())

    init {
        val archerRoundId = savedStateHandle.get<Int>("archerRoundId")
        if (archerRoundId == null) {
            _state.update { InvalidArcherRoundError() }
        }
        else {
            viewModelScope.launch {
                db.archerRoundsRepo().getFullArcherRoundInfo(archerRoundId)
                        .combine(datastore.get(DatastoreKey.Use2023HandicapSystem)) { info, system -> info to system }
                        .collect { (dbInfo, use2023System) ->
                            if (dbInfo == null) {
                                _state.update { InvalidArcherRoundError() }
                                return@collect
                            }

                            val info = FullArcherRoundInfo(dbInfo, use2023System)
                            _state.update {
                                when (it) {
                                    is Loading -> {
                                        helpShowcase.handle(HelpShowcaseIntent.Clear)
                                        it.transitionToLoaded(info)
                                    }
                                    is Loaded -> {
                                        val remainingChanged =
                                                info.remainingArrows != it.fullArcherRoundInfo.remainingArrows
                                        it.copy(
                                                fullArcherRoundInfo = info,
                                                displayRoundCompletedDialog = info.isRoundComplete && remainingChanged,
                                        )
                                    }
                                    else -> throw IllegalStateException()
                                }
                            }
                        }
            }

            viewModelScope.launch {
                datastore.get(DatastoreKey.UseBetaFeatures).collect { useBeta ->
                    _state.update {
                        when (it) {
                            is Loaded -> it.copy(useBetaFeatures = useBeta)
                            is Loading -> it.copy(useBetaFeatures = useBeta)
                            else -> it
                        }
                    }
                }
            }
        }
    }

    fun handle(action: ArcherRoundIntent) {
        if (state.value is Loading) throw IllegalStateException()
        if (state.value is InvalidArcherRoundError && action !is InvalidArcherRoundIntent) throw IllegalStateException()

        when (action) {
            is NavBarClicked -> {
                _state.update {
                    it as Loaded
                    require(action.screen.isMainScreen) { "Trying to use nav bar intent for sub-screen" }

                    if (action.screen == INPUT_END && it.fullArcherRoundInfo.isRoundComplete) {
                        return@update it.copy(displayCannotInputEndDialog = true)
                    }
                    it.changeScreen(action.screen)
                }
            }
            is ArrowInputsIntent -> handleArrowInputIntent(action)
            is ScorePadIntent -> handleScorePadIntent(action)
            is SettingsIntent -> handleSettingsIntent(action)
            is StatsIntent -> handleStatsIntent(action)
            RoundCompleteDialogOkClicked ->
                _state.update { (it as Loaded).changeScreen(STATS).copy(displayRoundCompletedDialog = false) }
            CannotInputEndDialogOkClicked -> _state.update { (it as Loaded).copy(displayCannotInputEndDialog = false) }
            is HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.ARCHER_ROUND_SCORE::class)
            is ErrorHandled -> _state.update {
                it as Loaded
                it.copy(errors = it.errors.minus(action.error))
            }
            InvalidArcherRoundIntent.ReturnToMenuClicked -> _state.update { InvalidArcherRoundError(true) }
            InvalidArcherRoundIntent.ReturnToMenuHandled -> _state.update { InvalidArcherRoundError(false) }
        }
    }

    private fun Loaded.changeScreen(screen: ArcherRoundScreen): Loaded {
        helpShowcase.handle(HelpShowcaseIntent.Clear)
        return copy(currentScreen = screen)
    }

    private fun commitNewEndToDb(state: Loaded) {
        if (state.currentScreen != INPUT_END) return

        var arrowNumber = state.fullArcherRoundInfo.arrows?.maxOfOrNull { it.arrowNumber } ?: 0
        val arrows = state.currentScreenInputArrows.map {
            it.toArrowValue(state.fullArcherRoundInfo.id, ++arrowNumber)
        }

        check(arrows.size <= state.currentScreenEndSize) { "Too many arrows have been inputted" }
        if (arrows.size < state.currentScreenEndSize) {
            _state.update { (it as Loaded).copy(errors = it.errors.plus(ArcherRoundError.NotEnoughArrowsInputted)) }
            return
        }

        viewModelScope.launch { arrowValuesRepo.insert(*arrows.toTypedArray()) }
        _state.update { (it as Loaded).copy(newInputArrows = listOf()) }
    }

    private fun commitEditEndToDb(state: Loaded) {
        if (state.currentScreen != EDIT_END) return

        var arrowNumber = state.scorePadSelectedEndFirstArrowNumber
        val arrows = state.currentScreenInputArrows.map { arrow ->
            arrow.toArrowValue(state.fullArcherRoundInfo.id, arrowNumber++)
        }

        check(arrows.size <= state.currentScreenEndSize) { "Too many arrows have been marked for edit" }
        if (arrows.size < state.currentScreenEndSize) {
            _state.update { (it as Loaded).copy(errors = it.errors.plus(ArcherRoundError.NotEnoughArrowsInputted)) }
            return
        }

        viewModelScope.launch { arrowValuesRepo.update(*arrows.toTypedArray()) }
        _state.update {
            (it as Loaded).changeScreen(SCORE_PAD).copy(scorePadSelectedEnd = null, subScreenInputArrows = emptyList())
        }
    }

    private fun commitInsertedEndToDb(state: Loaded) {
        if (state.currentScreen != INSERT_END) return

        var arrowNumber = state.scorePadSelectedEndFirstArrowNumber
        val arrows = state.currentScreenInputArrows.map { arrow ->
            arrow.toArrowValue(state.fullArcherRoundInfo.id, arrowNumber++)
        }
        check(arrows.size <= state.currentScreenEndSize) { "Too many arrows have been inputted" }

        viewModelScope.launch { arrowValuesRepo.insertEnd(state.fullArcherRoundInfo.arrows!!, arrows) }
        _state.update {
            (it as Loaded).changeScreen(SCORE_PAD).copy(scorePadSelectedEnd = null, subScreenInputArrows = emptyList())
        }
    }


    private fun handleArrowInputIntent(action: ArrowInputsIntent) {
        state.value as Loaded

        when (action) {
            is ArrowInputsIntent.ArrowInputted ->
                _state.update { s ->
                    s as Loaded
                    if (s.currentScreenInputArrows.size == s.currentScreenEndSize) {
                        return@update s.copy(errors = s.errors.plus(ArcherRoundError.EndFullCannotAddMore))
                    }
                    s.copy(newInputArrows = s.currentScreenInputArrows.plus(action.arrow))
                }
            ArrowInputsIntent.BackspaceArrowsInputted ->
                _state.update { s ->
                    s as Loaded
                    if (s.currentScreenInputArrows.isEmpty()) {
                        return@update s.copy(errors = s.errors.plus(ArcherRoundError.NoArrowsCannotBackSpace))
                    }
                    s.copy(newInputArrows = s.currentScreenInputArrows.dropLast(1))
                }
            ArrowInputsIntent.ClearArrowsInputted -> _state.update { (it as Loaded).copy(newInputArrows = listOf()) }
            ArrowInputsIntent.ResetArrowsInputted -> _state.update { (it as Loaded).setupArrowInputsOnEditScreen() }
            is ArrowInputsIntent.HelpShowcaseAction ->
                helpShowcase.handle(action.action, CodexNavRoute.ARCHER_ROUND_SCORE::class)
            ArrowInputsIntent.CancelClicked ->
                _state.update {
                    it as Loaded
                    when (it.currentScreen) {
                        INSERT_END, EDIT_END -> {
                            it.changeScreen(SCORE_PAD)
                                    .copy(scorePadSelectedEnd = null, subScreenInputArrows = emptyList())
                        }
                        else -> it
                    }
                }
            ArrowInputsIntent.SubmitClicked ->
                (state.value as Loaded).let {
                    when (it.currentScreen) {
                        INPUT_END -> commitNewEndToDb(it)
                        EDIT_END -> commitEditEndToDb(it)
                        INSERT_END -> commitInsertedEndToDb(it)
                        else -> {}
                    }
                }
        }
    }

    private fun Loaded.setupArrowInputsOnEditScreen(): Loaded {
        if (currentScreen != EDIT_END) return this

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
            ScorePadIntent.EditEndClicked ->
                _state.update { (it as Loaded).changeScreen(EDIT_END).setupArrowInputsOnEditScreen() }
            ScorePadIntent.InsertEndClicked ->
                _state.update { (it as Loaded).changeScreen(INSERT_END).copy(subScreenInputArrows = listOf()) }
            is ScorePadIntent.RowClicked ->
                _state.update { (it as Loaded).copy(scorePadSelectedEnd = action.endNumber) }
            is ScorePadIntent.RowLongClicked ->
                _state.update { (it as Loaded).copy(scorePadSelectedEnd = action.endNumber) }
            is ScorePadIntent.HelpShowcaseAction ->
                helpShowcase.handle(action.action, CodexNavRoute.ARCHER_ROUND_SCORE::class)
            ScorePadIntent.DeleteEndDialogCancelClicked ->
                _state.update {
                    (it as Loaded).copy(displayDeleteEndConfirmationDialog = false, scorePadSelectedEnd = null)
                }
            ScorePadIntent.DeleteEndDialogOkClicked -> {
                val currentState = state.value as Loaded
                viewModelScope.launch {
                    currentState.let {
                        arrowValuesRepo.deleteEnd(
                                it.fullArcherRoundInfo.arrows!!,
                                it.scorePadSelectedEndFirstArrowNumber,
                                it.scorePadSelectedEndSize,
                        )
                    }
                }
                _state.update {
                    (it as Loaded).copy(displayDeleteEndConfirmationDialog = false, scorePadSelectedEnd = null)
                }
            }
            ScorePadIntent.NoArrowsDialogOkClicked -> _state.update { (it as Loaded).changeScreen(INPUT_END) }
        }
    }

    private fun handleSettingsIntent(action: SettingsIntent) {
        state.value as Loaded

        when (action) {
            is SettingsIntent.InputEndSizeChanged ->
                _state.update { (it as Loaded).copy(inputEndSizePartial = action.endSize) }
            is SettingsIntent.ScorePadEndSizeChanged ->
                _state.update { (it as Loaded).copy(scorePadEndSizePartial = action.endSize) }
            is SettingsIntent.HelpShowcaseAction ->
                helpShowcase.handle(action.action, CodexNavRoute.ARCHER_ROUND_SCORE::class)
        }
    }

    private fun handleStatsIntent(action: StatsIntent) {
        state.value as Loaded

        when (action) {
            StatsIntent.EditClicked -> _state.update { (it as Loaded).copy(openEditScoreScreen = true) }
            StatsIntent.EditHandled -> _state.update { (it as Loaded).copy(openEditScoreScreen = false) }
            is StatsIntent.HelpShowcaseAction ->
                helpShowcase.handle(action.action, CodexNavRoute.ARCHER_ROUND_SCORE::class)
        }
    }
}
