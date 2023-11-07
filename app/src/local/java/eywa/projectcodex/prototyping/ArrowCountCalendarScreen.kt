package eywa.projectcodex.prototyping

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIf
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.prototyping.ArrowCountCalendarDisplayData.Entry
import eywa.projectcodex.prototyping.ArrowCountCalendarDisplayData.Entry.Day
import eywa.projectcodex.prototyping.ArrowCountCalendarDisplayData.Entry.WeeklyTotal
import java.util.Calendar

data class ArrowCountCalendarState(
        val monthDisplayed: Calendar = Calendar.getInstance(),
        val firstDayOfWeek: Int = Calendar.MONDAY,
        private val arrowsShot: List<ArrowCountCalendarRawData> = emptyList(),
) {
    val calendarHeadings =
            DayOfWeek.values()
                    // firstDayOfWeek is 1-indexed, hence the `-1`s
                    .let { it.drop(firstDayOfWeek - 1) + it.take(firstDayOfWeek - 1) }
                    .map { it.shortString }
                    .plus(ResOrActual.Actual("Total"))

    val data: ArrowCountCalendarDisplayData

    init {
        val entries = mutableListOf<Entry>()

        // Note mutable datatype, is incremented in for loop
        val dateToCheck = getFirstDateToDisplay()
        val (lastDay, lastMonth) = getLastDateToDisplayExclusive()
                .let { it.get(Calendar.DATE) to it.get(Calendar.MONTH) }
        val currentMonth = monthDisplayed.get(Calendar.MONTH)

        var monthlyTotal = 0
        var weeklyTotal = 0

        do {
            val day = dateToCheck.get(Calendar.DATE)
            val month = dateToCheck.get(Calendar.MONTH)
            val count = arrowsShot
                    .filter { day == it.date && month == it.month }
                    .takeIf { it.isNotEmpty() }
                    ?.sumOf { it.count }
            if (count != null) {
                weeklyTotal += count
                if (month == currentMonth) {
                    monthlyTotal += count
                }
            }

            entries.add(Day(day, month == currentMonth, count))

            dateToCheck.add(Calendar.DATE, 1)
            if (dateToCheck.get(Calendar.DAY_OF_WEEK) == firstDayOfWeek) {
                entries.add(WeeklyTotal(weeklyTotal))
                weeklyTotal = 0
            }
        } while (dateToCheck.get(Calendar.DATE) != lastDay || dateToCheck.get(Calendar.MONTH) != lastMonth)

        data = ArrowCountCalendarDisplayData(monthlyTotal, entries)
    }

    /**
     * The first day of the week that includes the first day of the month
     */
    private fun getFirstDateToDisplay(): Calendar {
        val firstDate = monthDisplayed.clone() as Calendar
        firstDate.firstDayOfWeek = firstDayOfWeek
        firstDate.set(Calendar.DATE, 1)
        // Roll back to first day of week
        while (firstDate.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
            firstDate.add(Calendar.DATE, -1)
        }
        return firstDate
    }

    /**
     * The last day of the week that includes the last day of the month
     */
    private fun getLastDateToDisplayExclusive(): Calendar {
        val lastDate = monthDisplayed.clone() as Calendar
        lastDate.set(Calendar.DATE, 28)
        // Roll forward to first day of next month
        while (lastDate.get(Calendar.MONTH) == monthDisplayed.get(Calendar.MONTH)
            || lastDate.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek
        ) {
            lastDate.add(Calendar.DATE, 1)
        }

        return lastDate
    }
}

sealed class ArrowCountCalendarIntent {
    object GoToNextMonth : ArrowCountCalendarIntent()
    object GoToPreviousMonth : ArrowCountCalendarIntent()
}

@Composable
fun ArrowCountCalendarScreen(
        state: ArrowCountCalendarState,
        listener: (ArrowCountCalendarIntent) -> Unit,
) {
    var height by remember { mutableStateOf(0.dp) }

    ProvideTextStyle(CodexTypography.SMALL) {
        Column(
                modifier = Modifier.padding(CodexTheme.dimens.screenPadding),
        ) {
            Text(
                    text = "Beta feature",
                    style = CodexTypography.LARGE,
                    color = CodexTheme.colors.onAppBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                            .padding(5.dp)
                            .align(Alignment.CenterHorizontally)
            )

            Row {
                Text(
                        text = DateTimeFormat.LONG_MONTH_YEAR.format(state.monthDisplayed),
                        style = CodexTypography.LARGE,
                        color = CodexTheme.colors.onAppBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(5.dp)
                )
                CodexIconButton(
                        icon = CodexIconInfo.VectorIcon(imageVector = Icons.Default.ChevronLeft),
                        onClick = { listener(ArrowCountCalendarIntent.GoToPreviousMonth) },
                )
                CodexIconButton(
                        icon = CodexIconInfo.VectorIcon(imageVector = Icons.Default.ChevronRight),
                        onClick = { listener(ArrowCountCalendarIntent.GoToNextMonth) },
                )
            }

            LazyVerticalGrid(columns = GridCells.Fixed(8)) {
                state.calendarHeadings.forEach {
                    item {
                        Text(
                                text = it.get(),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(2.dp)
                                        .background(CodexTheme.colors.listAccentRowItemOnAppBackground)
                                        .padding(vertical = 5.dp)
                        )
                    }
                }

                state.data.entries.forEach { entry ->
                    item {
                        when (entry) {
                            is Day -> Day(entry, height) { height = it }
                            is WeeklyTotal -> WeeklyTotal(entry, height)
                        }
                    }
                }
            }

            Text(
                    text = "Month's total: ${state.data.totalForMonth}",
                    style = CodexTypography.NORMAL,
                    color = CodexTheme.colors.onAppBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                            .padding(5.dp)
                            .align(Alignment.End)
            )
        }
    }
}

@Composable
private fun Day(
        entry: Day,
        itemHeight: Dp,
        onHeightChanged: (Dp) -> Unit,
) {
    val alpha = if (!entry.isCurrentMonth) 0.7f else 1f
    val density = LocalDensity.current

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                    .alpha(alpha)
                    .padding(2.dp)
                    .background(CodexTheme.colors.listItemOnAppBackground)
                    .padding(3.dp)
                    .onSizeChanged {
                        val h = with(density) { it.height.toDp() }
                        if (itemHeight < h) onHeightChanged(h)
                    }
    ) {
        Text(
                text = entry.date.toString(),
                textAlign = TextAlign.Center,
        )
        Text(
                text = entry.count?.toString() ?: "",
                textAlign = TextAlign.Center,
                modifier = Modifier
                        .background(
                                color = CodexTheme.colors.listAccentCellOnAppBackground,
                                shape = RoundedCornerShape(20),
                        )
                        .modifierIf(entry.count != null, Modifier.padding(horizontal = 2.dp))
        )
    }
}

@Composable
private fun WeeklyTotal(entry: WeeklyTotal, itemHeight: Dp) {
    Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                    .padding(2.dp)
                    .background(CodexTheme.colors.listAccentRowItemOnAppBackground)
                    .padding(3.dp)
                    .height(itemHeight)
    ) {
        Text(
                text = entry.count.toString(),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
        )
    }
}

data class ArrowCountCalendarRawData(
        val date: Int,
        val month: Int,
        val count: Int,
)

data class ArrowCountCalendarDisplayData(
        val totalForMonth: Int,
        val entries: List<Entry>,
) {
    sealed class Entry {
        data class Day(
                val date: Int,
                val isCurrentMonth: Boolean,
                val count: Int?,
        ) : Entry()

        data class WeeklyTotal(val count: Int) : Entry()
    }
}


/**
 * Note DO NOT change the order of these, they match the order of [Calendar.SUNDAY] etc.
 */
enum class DayOfWeek {
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
    ;

    val shortString: ResOrActual<String> = ResOrActual.Actual(name.take(1))
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun ArrowCountCalendarScreen_Preview() {
    val date = Calendar.getInstance()
    val firstDayOfWeek = Calendar.MONDAY
    date.firstDayOfWeek = firstDayOfWeek
    val currentMonth = date.get(Calendar.MONTH)
    CodexTheme {
        ArrowCountCalendarScreen(
                ArrowCountCalendarState(
                        monthDisplayed = date,
                        firstDayOfWeek = firstDayOfWeek,
                        arrowsShot = listOf(
                                ArrowCountCalendarRawData(2, currentMonth, 24),
                                ArrowCountCalendarRawData(31, currentMonth - 1, 6),
                        )
                )
        ) {}
    }
}
