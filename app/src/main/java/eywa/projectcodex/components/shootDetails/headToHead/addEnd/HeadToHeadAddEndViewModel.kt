package eywa.projectcodex.components.shootDetails.headToHead.addEnd

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.get
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.ShootDetailsError
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadArcherType.*
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHead.addEnd.HeadToHeadAddEndIntent.*
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGridRowData
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGridRowData.*
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadMatch
import eywa.projectcodex.model.headToHead.FullHeadToHeadSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeadToHeadAddEndViewModel @Inject constructor(
        private val repo: ShootDetailsRepo,
        savedStateHandle: SavedStateHandle,
        private val helpShowcaseUseCase: HelpShowcaseUseCase,
) : ViewModel() {
    private val screen = CodexNavRoute.HEAD_TO_HEAD_ADD_END
    private val extraState = MutableStateFlow<HeadToHeadAddEndExtras?>(null)

    val state = repo.getStateNullableExtra(extraState, ::stateConverter)
            .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    ShootDetailsResponse.Loading as ShootDetailsResponse<HeadToHeadAddEndState>,
            )

    private val h2hRepo = repo.db.h2hRepo()
    val shootId = savedStateHandle.get<Int>(NavArgument.SHOOT_ID)!!
    private val editingMatchNumber = savedStateHandle.get<Int>(NavArgument.MATCH_NUMBER)
    private val editingSetNumber = savedStateHandle.get<Int>(NavArgument.SET_NUMBER)
    private val isInserting = savedStateHandle.get<Boolean>(NavArgument.IS_INSERT) ?: false

    private fun stateConverter(
            main: ShootDetailsState,
            extras: HeadToHeadAddEndExtras?,
    ): HeadToHeadAddEndState {
        val shoot = main.fullShootInfo!!
        val fullH2hInfo = shoot.h2h ?: throw ShootDetailsError()

        val endSize = fullH2hInfo.headToHead.endSize ?: HeadToHeadUseCase.endSize(
                teamSize = fullH2hInfo.headToHead.teamSize,
                isShootOff = false,
        )
        val roundInfo = HeadToHeadRoundInfo(
                round = shoot.fullRoundInfo?.round,
                face = shoot.faces?.first(),
                distance = shoot.fullRoundInfo?.roundDistances?.maxOfOrNull { it.distance }
                        ?: shoot.shootDetail?.distance,
                sightMark = main.sightMark,
                isMetric = shoot.fullRoundInfo?.round?.isMetric ?: shoot.shootDetail?.isDistanceInMeters,
                endSize = endSize,
                isStandardFormat = fullH2hInfo.headToHead.isStandardFormat,
        )

        val teamSize = fullH2hInfo.headToHead.teamSize
        val editingMatch = fullH2hInfo.matches.find { it.match.matchNumber == editingMatchNumber }
        val editingSet = editingMatch?.sets?.find { it.setNumber == editingSetNumber }
                ?.let { fullSet ->
                    val data = fullSet.data.map {
                        when (it) {
                            is Total -> {
                                val field = EditableTotal(it.type, it.expectedArrowCount, dbId = it.dbId)
                                field.copy(text = field.text.copy(text = it.total?.toString() ?: ""))
                            }

                            is EditableTotal -> throw IllegalStateException()
                            else -> it
                        }
                    }
                    fullSet.copy(data = data)
                }

        // Editing
        if (editingSet != null && !isInserting) {

            if (extras == null || extras.set.setNumber != editingSetNumber) {
                extraState.update { HeadToHeadAddEndExtras().resetEditInfo(editingSet) }
            }

            return HeadToHeadAddEndState(
                    roundInfo = roundInfo,
                    extras = extras ?: HeadToHeadAddEndExtras().resetEditInfo(editingSet),
                    match = editingMatch.match,
                    isSetPoints = fullH2hInfo.headToHead.isSetPoints,
                    teamRunningTotal = null,
                    opponentRunningTotal = null,
                    editingSet = editingSet,
            )
        }

        val blankSet = FullHeadToHeadSet(
                setNumber = 0,
                data = listOf(),
                teamSize = fullH2hInfo.headToHead.teamSize,
                isSetPoints = fullH2hInfo.headToHead.isSetPoints,
                endSize = endSize,
        )

        // Create new set with fixed set number
        if (editingSetNumber != null) {
            check(editingMatch != null)

            if (extras == null || extras.set.setNumber != editingSetNumber) {
                val data = generateEmptyDataRows(
                        endSize = endSize,
                        teamSize = teamSize,
                        previous = editingMatch.sets
                                .find { it.setNumber == editingSetNumber - 1 }
                                ?.data
                                ?.filter { it !is ShootOff },
                )

                extraState.update {
                    HeadToHeadAddEndExtras(
                            set = blankSet.copy(setNumber = editingSetNumber, data = data)
                                    .setIsShootOff(fullH2hInfo.headToHead.endSize),
                            selected = null,
                    )
                }
            }

            val scores = editingMatch
                    .takeIf { match ->
                        editingSetNumber == 1 || match.sets.maxOfOrNull { it.setNumber } == editingSetNumber - 1
                    }
                    ?.runningTotals?.lastOrNull()?.left
            return HeadToHeadAddEndState(
                    roundInfo = roundInfo,
                    extras = extras ?: HeadToHeadAddEndExtras(),
                    match = editingMatch.match,
                    isSetPoints = fullH2hInfo.headToHead.isSetPoints,
                    // Default to zeros on the first set only
                    teamRunningTotal = scores?.first ?: 0.takeIf { editingSetNumber == 1 },
                    // Default to zeros on the first set only
                    opponentRunningTotal = scores?.second ?: 0.takeIf { editingSetNumber == 1 },
                    editingSet = null,
                    isInserting = isInserting,
            )
        }

        check(!isInserting) { "Must provide a set number when inserting" }
        val match = editingMatch ?: fullH2hInfo.matches.maxByOrNull { it.match.matchNumber }

        // Invalid match
        if (match == null || (match.isComplete && !match.match.isBye)) {
            extraState.update { (it ?: HeadToHeadAddEndExtras()).copy(openAddMatchScreen = true) }
            return HeadToHeadAddEndState(
                    match = DatabaseHeadToHeadMatch(
                            shootId = 1,
                            matchNumber = 1,
                            heat = null,
                            maxPossibleRank = 1,
                            opponent = null,
                            opponentQualificationRank = null,
                            sightersCount = 0,
                            isBye = false,
                    ),
                    extras = extras ?: HeadToHeadAddEndExtras(),
            )
        }

        val lastSet = match.sets.maxByOrNull { it.setNumber }

        // Creating new set
        if (lastSet == null || lastSet.isComplete) {
            val setNumber = (lastSet?.setNumber?.plus(1)) ?: 1

            fun getPreviousMatchFinalSetData() = fullH2hInfo
                    .matches
                    .find { it.match.matchNumber == match.match.matchNumber - 1 }
                    ?.sets
                    ?.maxByOrNull { it.setNumber }
                    ?.data


            if (extras == null || extras.set.setNumber != setNumber) {
                val set = blankSet.copy(
                        setNumber = setNumber,
                        data = generateEmptyDataRows(
                                endSize = endSize,
                                teamSize = teamSize,
                                previous = (lastSet?.data ?: getPreviousMatchFinalSetData())
                                        ?.filter { it !is ShootOff },
                        ),
                ).setIsShootOff(fullH2hInfo.headToHead.endSize)

                extraState.update { HeadToHeadAddEndExtras(set = set, selected = SELF) }
            }

            val scores = match.runningTotals.lastOrNull()?.left
            return HeadToHeadAddEndState(
                    roundInfo = roundInfo,
                    extras = extras ?: HeadToHeadAddEndExtras(),
                    match = match.match,
                    isSetPoints = fullH2hInfo.headToHead.isSetPoints,
                    // Default to zeros on the first set only
                    teamRunningTotal = scores?.first ?: 0.takeIf { setNumber == 1 },
                    // Default to zeros on the first set only
                    opponentRunningTotal = scores?.second ?: 0.takeIf { setNumber == 1 },
            )
        }

        // Editing old set
        if (extras == null || extras.set.setNumber != lastSet.setNumber) {
            extraState.update {
                HeadToHeadAddEndExtras(set = lastSet, selected = lastSet.getDefaultSelected())
            }
        }

        val scores = match.runningTotals.lastOrNull()?.left
        return HeadToHeadAddEndState(
                roundInfo = roundInfo,
                extras = extras ?: HeadToHeadAddEndExtras(),
                match = match.match,
                isSetPoints = fullH2hInfo.headToHead.isSetPoints,
                // Default to zeros on the first set only
                teamRunningTotal = scores?.first ?: 0.takeIf { extras?.set?.setNumber == 1 },
                // Default to zeros on the first set only
                opponentRunningTotal = scores?.second ?: 0.takeIf { extras?.set?.setNumber == 1 },
        )
    }

    private fun getRow(
            type: HeadToHeadArcherType,
            isTotal: Boolean,
            endSize: Int,
            teamSize: Int
    ): HeadToHeadGridRowData {
        val expectedArrowCount = type.expectedArrowCount(endSize = endSize, teamSize = teamSize)
        return when {
            type == RESULT -> Result()
            type == SHOOT_OFF -> {
                ShootOff(null)
            }

            isTotal -> EditableTotal(type = type, expectedArrowCount = expectedArrowCount)
            else -> Arrows(type = type, expectedArrowCount = expectedArrowCount)
        }
    }

    /**
     * Copy types of the previous set or give a default set of rows
     */
    private fun generateEmptyDataRows(
            endSize: Int,
            teamSize: Int,
            previous: List<HeadToHeadGridRowData>? = null
    ): List<HeadToHeadGridRowData> {
        fun getRow(type: HeadToHeadArcherType, isTotal: Boolean) = getRow(type, isTotal, endSize, teamSize)

        return when {
            previous != null -> previous.map { getRow(it.type, it.isTotalRow) }
            teamSize == 1 -> listOf(getRow(SELF, false), getRow(OPPONENT, true))
            else -> listOf(getRow(SELF, false), getRow(TEAM, true), getRow(OPPONENT, true))
        }
    }

    private fun FullHeadToHeadSet.getDefaultSelected(): HeadToHeadArcherType? {
        val selectedRow = data
                .sortedBy { it.type.ordinal }
                .firstOrNull { !it.isComplete }
                ?: data.firstOrNull()
        return selectedRow?.type
    }

    private fun HeadToHeadAddEndExtras.resetEditInfo(editingSet: FullHeadToHeadSet) = copy(
            set = editingSet,
            selected = editingSet.getDefaultSelected(),
            arrowInputsError = setOf(),
            incompleteError = false,
    )

    private fun FullHeadToHeadSet.setIsShootOff(matchEndSize: Int?): FullHeadToHeadSet {
        val isShootOff = matchEndSize == null && HeadToHeadUseCase.shootOffSet(teamSize) <= setNumber
        val endSize = if (isShootOff) 1 else (matchEndSize ?: HeadToHeadUseCase.endSize(teamSize, false))
        return copy(
                endSize = endSize,
                data = if (!isShootOff) data else generateEmptyDataRows(endSize, teamSize, data + ShootOff(null)),
        )
    }

    private fun FullHeadToHeadSet.updateShootOffRow(shootOffRow: ShootOff? = null): FullHeadToHeadSet {
        val shootOff = shootOffRow ?: data.find { it is ShootOff } ?: return this
        shootOff as ShootOff

        val without = this.copy(data = data.filter { it !is ShootOff })

        val result =
                if (without.result == HeadToHeadResult.TIE) shootOff.result ?: HeadToHeadResult.LOSS
                else if (without.result.isComplete) null
                else shootOff.result

        return without.copy(data = without.data + shootOff.copy(result))
    }

    fun handle(action: HeadToHeadAddEndIntent) {
        fun updateState(block: (HeadToHeadAddEndExtras) -> HeadToHeadAddEndExtras) =
                extraState.update { block(it!!) }

        when (action) {
            is HelpShowcaseAction -> helpShowcaseUseCase.handle(action.action, screen::class)

            is ShootDetailsAction -> repo.handle(action.action, screen)

            PressBackHandled -> updateState { it.copy(pressBack = false) }
            EditSightMarkClicked -> updateState { it.copy(openEditSightMark = true) }
            ExpandSightMarkClicked -> updateState { it.copy(openAllSightMarks = true) }
            EditSightMarkHandled -> updateState { it.copy(openEditSightMark = false) }
            ExpandSightMarkHandled -> updateState { it.copy(openAllSightMarks = false) }
            OpenAddMatchScreenHandled -> updateState { it.copy(openAddMatchScreen = false) }
            is ArrowInputsErrorHandled ->
                updateState { it.copy(arrowInputsError = it.arrowInputsError.minus(action.error)) }

            is ArrowInputAction -> {
                val currentState = state.value.getData() ?: return
                val row = currentState.extras.set.data.find { it.type == currentState.extras.selected } ?: return
                if (row !is Arrows) return

                action.action.handle(
                        enteredArrows = row.arrows,
                        endSize = currentState.extras.set.endSize,
                        dbArrows = null,
                        setEnteredArrows = { arrows, error ->
                            updateState { s ->
                                val data = s.set.data
                                        .filter { it.type != currentState.extras.selected }
                                        .plus(row.copy(arrows = arrows))
                                s.copy(
                                        set = s.set.copy(data = data).updateShootOffRow(),
                                        arrowInputsError = error?.let { s.arrowInputsError.plus(error) }
                                                ?: s.arrowInputsError,
                                )
                            }
                        },
                        onSubmit = { throw NotImplementedError() },
                        helpListener = { handle(HelpShowcaseAction(it)) },
                )
            }

            SightersClicked -> updateState { it.copy(openSighters = true) }
            SightersHandled -> updateState { it.copy(openSighters = false) }
            is GridRowClicked -> {
                if (action.row == RESULT) {
                    updateState { currentState ->
                        val resultRow = currentState.set.data
                                .find { it.type == RESULT }
                                ?.let { it as Result }
                                ?: return@updateState currentState
                        val newData = currentState.set.data.minus(resultRow).plus(resultRow.next())
                        currentState.copy(set = currentState.set.copy(data = newData).updateShootOffRow())
                    }
                }
                else if (action.row == SHOOT_OFF) {
                    updateState { currentState ->
                        val resultRow = currentState.set.data
                                .find { it.type == SHOOT_OFF }
                                ?.let { it as ShootOff }
                                ?.takeIf { it.result != null }
                                ?: return@updateState currentState
                        val newData = currentState.set.data.minus(resultRow).plus(resultRow.next())
                        currentState.copy(set = currentState.set.copy(data = newData))
                    }
                }
                updateState { it.copy(selected = action.row) }
            }

            ToggleShootOff -> updateState { s ->
                val isShootOff = !s.set.isShootOff
                val endSize = if (isShootOff) 1 else state.value.getData()!!.roundInfo.endSize

                // Add or remove shoot off row
                val previous =
                        if (!isShootOff) {
                            s.set.data.filter { it !is ShootOff }
                        }
                        else {
                            val dbId = state.value.getData()!!.editingSet?.shootOffRow?.dbId
                            s.set.data + ShootOff(null, dbId)
                        }

                s.copy(
                        set = s.set.copy(
                                endSize = endSize,
                                data = generateEmptyDataRows(
                                        endSize = endSize,
                                        teamSize = s.set.teamSize,
                                        previous = previous,
                                ),
                        ).updateShootOffRow(),
                )
            }

            SubmitClicked -> state.value.getData().let { state ->
                if (state == null) {
                    return
                }

                val set = state.extras.set
                if (!set.isComplete) {
                    updateState { it.copy(incompleteError = true) }
                    return
                }
                viewModelScope.launch {
                    if (state.editingSet == null || isInserting) {
                        h2hRepo.insert(details = state.setToDbDetails().toTypedArray())
                    }
                    else h2hRepo.update(
                            newDetails = state.setToDbDetails(),
                            oldDetails = state.editingToDbDetails()!!,
                    )

                    if (state.editingSet != null || isInserting) {
                        extraState.update { it!!.copy(pressBack = true) }
                    }
                }
            }

            is GridTextValueChanged -> updateState { s ->
                val row = s.set.data.find { it.type == action.type } ?: return@updateState s
                if (row !is EditableTotal) return@updateState s

                val shootOffRow = s.set.shootOffRow
                var newData = s.set.data.minus(row).plus(row.copy(text = row.text.onTextChanged(action.text)))
                if (shootOffRow != null) {
                    newData = newData.minus(shootOffRow)
                }

                s.copy(set = s.set.copy(data = newData).updateShootOffRow(shootOffRow))
            }

            CreateNextMatchClicked -> updateState { it.copy(openCreateNextMatch = true) }
            CreateNextMatchHandled -> updateState { it.copy(openCreateNextMatch = false) }
            EditTypesClicked -> updateState { s ->
                s.copy(
                        selectRowTypesDialogState = s.set.data.associate { it.type to it.isTotalRow },
                        selectRowTypesDialogUnknownWarning = s.set.requiredRowsString,
                )
            }

            is EditTypesItemClicked -> updateState {
                val dialogState = it.selectRowTypesDialogState ?: return@updateState it
                val item = dialogState[action.item]

                // null -> false -> true -> null
                val newDialogState =
                        when (item) {
                            // RESULT skips false because can't have arrow values for it
                            null -> dialogState.plus(action.item to (action.item == RESULT))
                            false -> dialogState.plus(action.item to true)
                            true -> dialogState.minus(action.item)
                        }

                val updatedSetData = FullHeadToHeadSet(
                        setNumber = it.set.setNumber,
                        data = newDialogState.map { (type, isTotal) ->
                            getRow(type, isTotal, it.set.endSize, it.set.teamSize)
                        },
                        teamSize = it.set.teamSize,
                        isSetPoints = it.set.isSetPoints,
                        endSize = it.set.endSize,
                )

                it.copy(
                        selectRowTypesDialogState = newDialogState,
                        selectRowTypesDialogUnknownWarning = updatedSetData.requiredRowsString,
                )
            }

            is CompleteEditTypesDialog -> {
                updateState { s ->
                    val requiredRows = s.selectRowTypesDialogState ?: return@updateState s
                    val currentData = s.set.data.associateBy { it.type }

                    val isTeam = s.set.teamSize > 1
                    val types = requiredRows.keys.toList()

                    val newRows = requiredRows.map { (type, isTotal) ->
                        val current = currentData[type]
                        if (
                            current != null
                            && current.isTotalRow == isTotal
                            && type.enabledOnSelectorDialog(isTeam, types)
                        ) {
                            current
                        }
                        else {
                            getRow(type, isTotal, s.set.endSize, s.set.teamSize)
                        }
                    }

                    s.copy(set = s.set.copy(data = newRows), selectRowTypesDialogState = null)
                }
            }

            CloseEditTypesDialog -> updateState { it.copy(selectRowTypesDialogState = null) }

            DeleteClicked -> viewModelScope.launch {
                val currentState = state.value.getData() ?: return@launch
                val setNumber = currentState.editingSet?.setNumber ?: return@launch

                repo.db.h2hRepo().delete(
                        shootId = shootId,
                        matchNumber = currentState.match.matchNumber,
                        setNumber = setNumber,
                )
            }

            ResetClicked -> extraState.update { null }
        }
    }
}
