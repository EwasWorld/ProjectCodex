package eywa.projectcodex.components.arrowCountCalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eywa.projectcodex.common.sharedUi.CodexGrid
import eywa.projectcodex.common.sharedUi.CodexGridColumn.Match
import eywa.projectcodex.common.sharedUi.CodexGridColumn.WrapContent
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIf
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.arrowCountCalendar.ArrowCountCalendarDisplayData.Entry.Day
import eywa.projectcodex.components.arrowCountCalendar.ArrowCountCalendarDisplayData.Entry.WeeklyTotal
import eywa.projectcodex.database.shootData.DatabaseArrowCountCalendarData
import java.util.Calendar

private val betweenCellPadding = 2.dp
private val arrowCountTextPadding = 5.dp
private val totalCountTextPadding = 8.dp

@Composable
fun ArrowCountCalendarScreen(
        viewModel: ArrowCountCalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    ArrowCountCalendarScreen(state) { viewModel.handle(it) }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun ArrowCountCalendarScreen(
        state: ArrowCountCalendarState,
        listener: (ArrowCountCalendarIntent) -> Unit,
) {
    ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onListItemAppOnBackground)) {
        Column(
                modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = CodexTheme.dimens.screenPadding),
        ) {
            Text(
                    text = "Beta feature",
                    style = CodexTypography.LARGE,
                    color = CodexTheme.colors.onAppBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                            .padding(5.dp)
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = CodexTheme.dimens.screenPadding)
            )

            Row(
                    modifier = Modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
            ) {
                Text(
                        text = DateTimeFormat.LONG_MONTH_YEAR.format(state.monthDisplayed),
                        style = CodexTypography.LARGE,
                        color = CodexTheme.colors.onAppBackground,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                                .padding(5.dp)
                                .weight(1f)
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

            val measurer = rememberTextMeasurer()
            val minColWidth = with(LocalDensity.current) {
                measurer.measure("000").size.width.toDp() + (arrowCountTextPadding + betweenCellPadding) * 2
            }
            val minTotalColumnWidth = with(LocalDensity.current) {
                measurer.measure("000", CodexTypography.LARGE).size.width.toDp() +
                        (totalCountTextPadding + betweenCellPadding) * 2
            }

            CodexGrid(
                    columns = List(8) { if (it < 7) Match(1) else WrapContent },
                    alignment = Alignment.Center,
                    modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(10.dp)
                            .align(Alignment.CenterHorizontally)
            ) {
                state.calendarHeadings.forEachIndexed { index, text ->
                    val minWidth = if (index == state.calendarHeadings.lastIndex) minTotalColumnWidth else minColWidth
                    item(fillBox = true) {
                        Text(
                                text = text.get(),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                        .padding(betweenCellPadding)
                                        .background(CodexTheme.colors.listAccentRowItemOnAppBackground)
                                        .padding(arrowCountTextPadding)
                                        .widthIn(min = minWidth)
                        )
                    }
                }

                state.data.entries.forEach { entry ->
                    item(fillBox = true) {
                        when (entry) {
                            is Day -> Day(entry)
                            is WeeklyTotal -> WeeklyTotal(entry)
                        }
                    }
                }
            }

            Text(
                    text = "Month's total: ${state.data.totalForMonth}",
                    style = CodexTypography.LARGE,
                    color = CodexTheme.colors.onAppBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                            .padding(5.dp)
                            .align(Alignment.End)
                            .padding(horizontal = CodexTheme.dimens.screenPadding)

            )
            Text(
                    text = "Note: amounts include sighters",
                    style = CodexTypography.SMALL,
                    color = CodexTheme.colors.onAppBackground,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                            .align(Alignment.End)
                            .padding(horizontal = CodexTheme.dimens.screenPadding)
            )
        }
    }
}

@Composable
private fun Day(
        entry: Day,
) {
    val alpha = if (!entry.isCurrentMonth) 0.7f else 1f

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                    .alpha(alpha)
                    .padding(betweenCellPadding)
                    .background(CodexTheme.colors.listItemOnAppBackground)
                    .padding(arrowCountTextPadding)
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
private fun WeeklyTotal(entry: WeeklyTotal) {
    Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                    .padding(betweenCellPadding)
                    .background(CodexTheme.colors.listAccentRowItemOnAppBackground)
                    .padding(totalCountTextPadding)
    ) {
        Text(
                text = entry.count.toString(),
                textAlign = TextAlign.Center,
                style = CodexTypography.LARGE,
                color = CodexTheme.colors.onListItemAppOnBackground,
        )
    }
}


@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        widthDp = 600
)
@Composable
fun ArrowCountCalendarScreen_Preview() {
    val date = DateTimeFormat.SHORT_DATE.parse("15/4/23")
    val firstDayOfWeek = Calendar.MONDAY
    date.firstDayOfWeek = firstDayOfWeek
    CodexTheme {
        ArrowCountCalendarScreen(
                ArrowCountCalendarState(
                        monthDisplayed = date,
                        firstDayOfWeek = firstDayOfWeek,
                        arrowsShot = listOf(
                                DatabaseArrowCountCalendarData("02-04", 24),
                                DatabaseArrowCountCalendarData("31-03", 6),
                                DatabaseArrowCountCalendarData("04-04", 1000),
                        )
                )
        ) {}
    }
}
