package eywa.projectcodex.components.viewScores.actionBar.filters

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.sharedUi.UpdateCalendarInfo
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.*
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.viewScores.actionBar.filters.ViewScoresFiltersIntent.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.Calendar
import javax.inject.Singleton
import kotlin.random.Random

typealias Id = Int

/**
 * Allows [ViewScoresFiltersState] to be accessed from ViewScores and ViewScoresFilters which are separate destinations
 * in the nav graph
 */
class ViewScoresFiltersUseCase(
        private val helpShowcaseUseCase: HelpShowcaseUseCase,
) {
    private val state = MutableStateFlow(emptyMap<Id, ViewScoresFiltersState>())

    fun getState(id: Id) = state.map { it[id] ?: ViewScoresFiltersState() }

    fun clearState(id: Id) {
        state.update { it.minus(id) }
    }

    fun handle(id: Id, action: ViewScoresFiltersIntent) {
        state.update {
            val state = it[id] ?: return@update it
            it.plus(id to action.handle(state))
        }
    }

    /**
     * Generates new id and state
     */
    fun initialiseNew(): Id {
        synchronized(this) {
            var id: Int
            do {
                id = Random.nextInt()
            } while (state.value.containsKey(id))

            state.update { it.plus(id to ViewScoresFiltersState()) }
            return id
        }
    }

    fun ViewScoresFiltersIntent.handle(state: ViewScoresFiltersState): ViewScoresFiltersState = when (this) {
        CloseFilters -> state.copy(shouldCloseDialog = true)
        CloseFiltersHandled -> state.copy(shouldCloseDialog = false)
        ClearFromFilter -> state.copy(fromDate = null)
        ClearUntilFilter -> state.copy(untilDate = null)
        ClearRoundsFilter -> state.copy(roundFilter = false)
        ClearSubtypeFilter ->
            state.copy(selectRoundDialogState = state.selectRoundDialogState.copy(selectedSubTypeId = null))

        ClickPbFilter -> state.copy(personalBestsFilter = !state.personalBestsFilter)
        ClickCompleteFilter -> state.copy(completedRoundsFilter = !state.completedRoundsFilter)
        ClickFirstOfDayFilter -> state.copy(firstRoundOfDayFilter = !state.firstRoundOfDayFilter)
        ClickTypeFilter -> {
            val all = ViewScoresFiltersTypes.values()
            state.copy(typeFilter = all[(state.typeFilter.ordinal + 1) % all.size])
        }

        is UpdateFromFilter -> state.copy(fromDate = state.fromDate.update(info, "00:00"))
        is UpdateUntilFilter -> state.copy(untilDate = state.untilDate.update(info, "23:59"))
        is UpdateRoundsFilter -> {
            val newState = state.copy(selectRoundDialogState = action.handle(state.selectRoundDialogState).first)
            val roundSelectedAction = action is RoundIntent.RoundSelected || action is RoundIntent.NoRoundSelected
            newState.copy(roundFilter = roundSelectedAction || state.roundFilter)
        }

        ClearScoreMaxFilter -> state.copy(maxScore = state.maxScore.onTextChanged(null))
        ClearScoreMinFilter -> state.copy(minScore = state.minScore.onTextChanged(null))
        is UpdateScoreMaxFilter -> state.copy(maxScore = state.maxScore.onTextChanged(value))
        is UpdateScoreMinFilter -> state.copy(minScore = state.minScore.onTextChanged(value))
        ClearAllFilters -> ViewScoresFiltersState(
                selectRoundDialogState = state.selectRoundDialogState,
                roundFilter = false,
                updateDefaultRoundsState = state.updateDefaultRoundsState,
        )

        is SetUpdateRoundsState -> state.copy(updateDefaultRoundsState = updateState)
        is HelpShowcaseAction -> {
            helpShowcaseUseCase.handle(action, screen)
            state
        }

        StartHelpShowcase -> {
            helpShowcaseUseCase.startShowcase(ViewScoresBottomSheetFilters::class)
            state
        }
    }

    private fun Calendar?.update(info: UpdateCalendarInfo, time: String): Calendar {
        var calendar = this
        if (calendar == null) {
            val todaysDate = DateTimeFormat.SHORT_DATE.format(Calendar.getInstance())
            calendar = DateTimeFormat.SHORT_DATE_TIME.parse("$todaysDate $time")
        }
        return info.updateCalendar(calendar)
    }
}

@InstallIn(SingletonComponent::class)
@Module
object ViewScoresModule {
    @Singleton
    @Provides
    fun provideViewScoresFiltersRepo(
            helpShowcaseUseCase: HelpShowcaseUseCase,
    ) = ViewScoresFiltersUseCase(helpShowcaseUseCase)
}
