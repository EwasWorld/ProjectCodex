package eywa.projectcodex.prototyping

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
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
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIf
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.DateTimeFormat
import java.util.Calendar

data class ArrowCountCalendarState(
        val monthDisplayed: Calendar = Calendar.getInstance(),
        val firstDayOfWeek: Int = Calendar.MONDAY,
        val arrowsShot: List<ArrowCountCalendarEntry> = emptyList(),
) {
    /**
     * The first day of the week that includes the first day of the month
     */
    val firstDateToDisplay: Calendar
        get() {
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
    val lastDateToDisplayExclusive: Calendar
        get() {
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
    val date = state.firstDateToDisplay
    val lastDate = state.lastDateToDisplayExclusive
    val lastDateDay = lastDate.get(Calendar.DATE)
    val lastDateMonth = lastDate.get(Calendar.MONTH)
    val currentMonth = state.monthDisplayed.get(Calendar.MONTH)

    var height by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

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
                headers(state.firstDayOfWeek)

                var weeklyTotal = 0
                do {
                    val day = date.get(Calendar.DATE)
                    val month = date.get(Calendar.MONTH)
                    val entry = state.arrowsShot.find {
                        day == it.date && month == it.month
                    }
                    if (entry != null) {
                        weeklyTotal += entry.count
                    }
                    val alpha = if (month != currentMonth) 0.7f else 1f
                    val count = entry?.count?.toString()

                    item {
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
                                            if (height < h) {
                                                height = h
                                            }
                                        }
                        ) {
                            Text(
                                    text = day.toString(),
                                    textAlign = TextAlign.Center,
                            )
                            Text(
                                    text = count ?: "",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                            .background(
                                                    color = CodexTheme.colors.listAccentCellOnAppBackground,
                                                    shape = RoundedCornerShape(20),
                                            )
                                            .modifierIf(count != null, Modifier.padding(horizontal = 2.dp))
                            )
                        }
                    }
                    date.add(Calendar.DATE, 1)
                    if (date.get(Calendar.DAY_OF_WEEK) == state.firstDayOfWeek) {
                        item {
                            Column(
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                            .padding(2.dp)
                                            .background(CodexTheme.colors.listAccentRowItemOnAppBackground)
                                            .padding(3.dp)
                                            .height(height)
                            ) {
                                Text(
                                        text = weeklyTotal.toString(),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                )
                            }
                            weeklyTotal = 0
                        }
                    }
                } while (date.get(Calendar.DATE) != lastDateDay || date.get(Calendar.MONTH) != lastDateMonth)
            }
        }
    }
}

private fun LazyGridScope.headers(firstDayOfWeek: Int) {
    for (i in 0..6) {
        val dayOfWeek = DayOfWeek.values()[(i + firstDayOfWeek - 1) % 7]
        item {
            Text(
                    text = dayOfWeek.toString().take(1),
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
    item {
        Text(
                text = "Total",
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

data class ArrowCountCalendarEntry(
        val date: Int,
        val month: Int,
        val count: Int,
)

enum class DayOfWeek {
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
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
                                ArrowCountCalendarEntry(2, currentMonth, 24),
                        )
                )
        ) {}
    }
}
