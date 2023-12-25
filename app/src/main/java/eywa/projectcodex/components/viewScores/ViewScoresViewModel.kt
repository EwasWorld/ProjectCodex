package eywa.projectcodex.components.viewScores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.diActivityHelpers.ShootIdsUseCase
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.components.viewScores.ViewScoresIntent.*
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.ui.convertScoreDialog.ConvertScoreIntent
import eywa.projectcodex.components.viewScores.ui.filters.ViewScoresFiltersIntent
import eywa.projectcodex.components.viewScores.ui.multiSelectBar.MultiSelectBarIntent
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.shootData.ShootsRepo
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.model.FullShootInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ViewScoresViewModel @Inject constructor(
        db: ScoresRoomDatabase,
        private val helpShowcase: HelpShowcaseUseCase,
        private val customLogger: CustomLogger,
        private val datastore: CodexDatastore,
        private val shootIdsUseCase: ShootIdsUseCase,
) : ViewModel() {
    private var _state = MutableStateFlow(ViewScoresState())
    val state = _state.asStateFlow()

    private val arrowScoresRepo = db.arrowScoresRepo()
    private val roundRepo = db.roundsRepo()
    private val shootsRepo: ShootsRepo = db.shootsRepo()

    init {
        viewModelScope.launch {
            state.map { it.filters }
                    .distinctUntilChanged()
                    .flatMapLatest { shootsRepo.getFullShootInfo(it) }
                    .combine(datastore.get(DatastoreKey.Use2023HandicapSystem)) { info, system -> info to system }
                    .collect { (flowData, use2023System) ->
                        _state.update {
                            val previousSelectedEntries = it.data.associate { entry -> entry.id to entry.isSelected }
                            it.copy(
                                    data = flowData.map { roundInfo ->
                                        val info = FullShootInfo(roundInfo, use2023System)
                                        ViewScoresEntry(
                                                info = info,
                                                isSelected = previousSelectedEntries[info.id] ?: false,
                                                customLogger = customLogger,
                                        )
                                    }.sortedByDescending { entry -> entry.info.shoot.dateShot }
                            )
                        }
                    }
        }
        viewModelScope.launch {
            roundRepo.fullRoundsInfo.collect { data ->
                val action = ViewScoresFiltersIntent.UpdateRoundsFilter(SelectRoundDialogIntent.SetRounds(data))
                handle(FiltersAction(action))
            }
        }
    }

    fun handle(action: ViewScoresIntent) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.VIEW_SCORES::class)
            is MultiSelectAction -> handleMultiSelectIntent(action.action)
            is FiltersAction ->
                _state.update { it.copy(viewScoresFiltersState = action.action.handle(it.viewScoresFiltersState)) }

            is EffectComplete -> handleEffectComplete(action)
            is ConvertScoreAction -> handleConvertScoreIntent(action.action)
            is EntryClicked -> {
                _state.update {
                    if (it.isInMultiSelectMode) {
                        it.selectItem(action.shootId)
                    }
                    else {
                        it.data
                                .find { entry -> entry.id == action.shootId }
                                ?.getSingleClickAction()
                                ?.handleClick
                                ?.invoke(it.copy(lastClickedEntryId = action.shootId), shootIdsUseCase)
                                ?: return@update it
                    }
                }
            }

            is EntryLongClicked ->
                _state.update {
                    if (it.isInMultiSelectMode) it.selectItem(action.shootId)
                    else it.copy(lastClickedEntryId = action.shootId, dropdownMenuOpen = true)
                }

            is DropdownMenuClicked ->
                _state.update {
                    action.item
                            .handleClick(it.copy(lastClickedEntryId = action.shootId), shootIdsUseCase)
                            .copy(dropdownMenuOpen = false)
                }

            DropdownMenuClosed -> _state.update { it.copy(dropdownMenuOpen = false) }
            NoRoundsDialogOkClicked -> _state.update { it.copy(noRoundsDialogOkClicked = true) }
            DeleteDialogCancelClicked -> _state.update { it.copy(deleteDialogOpen = false) }
            DeleteDialogOkClicked -> {
                val id = _state.value.let { currentState ->
                    currentState.lastClickedEntryId?.takeIf { currentState.deleteDialogOpen }
                }
                _state.update { it.copy(deleteDialogOpen = false) }

                if (id != null) {
                    viewModelScope.launch { shootsRepo.deleteRound(id) }
                }
            }
        }
    }

    private fun ViewScoresState.selectItem(shootId: Int): ViewScoresState {
        if (!isInMultiSelectMode) return this

        val entryIndex = _state.value
                .data
                .indexOfFirst { entry -> entry.id == shootId }
                .takeIf { index -> index >= 0 }
                ?: return this

        val entry = data[entryIndex]

        return copy(
                data = data
                        .take(entryIndex)
                        .plus(entry.copy(isSelected = !entry.isSelected))
                        .plus(data.drop(entryIndex + 1))
        )
    }

    private fun handleEffectComplete(action: EffectComplete) {
        when (action) {
            HandledEmailNoSelection -> _state.update { it.copy(multiSelectEmailNoSelection = false) }
            HandledScorePadOpened -> _state.update { it.copy(openScorePadClicked = false) }
            HandledEditInfoOpened -> _state.update { it.copy(openEditInfoClicked = false) }
            HandledEmailOpened -> _state.update { it.copy(openEmailClicked = false) }
            HandledAddCountOpened -> _state.update { it.copy(openAddCountClicked = false) }
            HandledAddEndOnCompletedRound -> _state.update { it.copy(openAddEndOnCompletedRound = false) }
            HandledAddEndOpened -> _state.update { it.copy(openAddEndClicked = false) }
            HandledNoRoundsDialogOkClicked -> _state.update { it.copy(noRoundsDialogOkClicked = false) }
        }
    }

    private fun handleMultiSelectIntent(action: MultiSelectBarIntent) {
        check(action is MultiSelectBarIntent.ClickOpen || state.value.isInMultiSelectMode) {
            "Tried to invoke a multi-select action while not in multi-select mode"
        }

        when (action) {
            MultiSelectBarIntent.ClickOpen -> _state.update { it.copy(isInMultiSelectMode = true) }
            MultiSelectBarIntent.ClickClose ->
                _state.update {
                    shootIdsUseCase.clear()
                    it.copy(
                            isInMultiSelectMode = false,
                            data = it.data.map { entry -> entry.copy(isSelected = false) },
                    )
                }

            MultiSelectBarIntent.ClickAllOrNone -> _state.update {
                val selectAll = !it.data.all { entry -> entry.isSelected }
                it.copy(data = it.data.map { entry -> entry.copy(isSelected = selectAll) })
            }

            MultiSelectBarIntent.ClickEmail -> _state.update {
                val selectedItems = it.data
                        .filter { entry -> entry.isSelected }
                if (selectedItems.isEmpty()) {
                    return@update it.copy(multiSelectEmailNoSelection = true)
                }

                shootIdsUseCase.setItems(selectedItems.map { item -> item.id })
                it.copy(openEmailClicked = true)
            }
        }
    }

    private fun handleConvertScoreIntent(action: ConvertScoreIntent) {
        when (action) {
            ConvertScoreIntent.Close -> _state.update { it.copy(convertScoreDialogOpen = false) }

            is ConvertScoreIntent.Ok -> _state.update {
                val arrows = it.data
                        .find { entry -> entry.id == it.lastClickedEntryId }
                        ?.info
                        ?.arrows
                        ?.takeIf { arrows -> arrows.isNotEmpty() }
                        ?.let { oldArrows -> action.convertType.convertScore(oldArrows) }
                        ?: return@update it.copy(convertScoreDialogOpen = false)

                viewModelScope.launch {
                    arrowScoresRepo.update(*arrows.toTypedArray())
                }
                it.copy(convertScoreDialogOpen = false)
            }
        }
    }
}
