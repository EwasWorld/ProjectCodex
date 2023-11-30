package eywa.projectcodex.components.arrowCountCalendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.components.arrowCountCalendar.ArrowCountCalendarIntent.GoToNextMonth
import eywa.projectcodex.components.arrowCountCalendar.ArrowCountCalendarIntent.GoToPreviousMonth
import eywa.projectcodex.components.arrowCountCalendar.ArrowCountCalendarIntent.HelpShowcaseAction
import eywa.projectcodex.database.ScoresRoomDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ArrowCountCalendarViewModel @Inject constructor(
        private val db: ScoresRoomDatabase,
        private val helpShowcase: HelpShowcaseUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(ArrowCountCalendarState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            state.map { it.monthDisplayed }.distinctUntilChanged().collectLatest { date ->
                db.shootsRepo().getCountsForCalendar(date).collectLatest { data ->
                    _state.update { it.copy(arrowsShot = data) }
                }
            }
        }
    }

    fun handle(action: ArrowCountCalendarIntent) {
        when (action) {
            GoToNextMonth -> addMonths(1)
            GoToPreviousMonth -> addMonths(-1)
            is HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.ARROW_COUNT_CALENDAR::class)
        }
    }

    private fun addMonths(numberToAdd: Int) {
        _state.update {
            val newMonthDisplayed = (it.monthDisplayed.clone() as Calendar)
            newMonthDisplayed.add(Calendar.MONTH, numberToAdd)
            it.copy(monthDisplayed = newMonthDisplayed)
        }
    }
}
