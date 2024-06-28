package eywa.projectcodex.components.shootDetails.stats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.CodexGrid
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.ResOrActual.StringResource
import eywa.projectcodex.common.utils.asDecimalFormat
import eywa.projectcodex.common.utils.classificationTables.ClassificationTablesPreviewHelper
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsPreviewHelper
import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.stats.DistanceBreakdownRow
import eywa.projectcodex.components.shootDetails.stats.GrandTotalBreakdownRow
import eywa.projectcodex.components.shootDetails.stats.NumbersBreakdownRowStats
import eywa.projectcodex.components.shootDetails.stats.StatsExtras
import eywa.projectcodex.components.shootDetails.stats.StatsIntent
import eywa.projectcodex.components.shootDetails.stats.StatsState

@Composable
internal fun NumberBreakdownSection(
        state: StatsState,
        modifier: Modifier = Modifier,
        listener: (StatsIntent) -> Unit = { },
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(StatsIntent.HelpShowcaseAction(it)) }
    val resource = LocalContext.current.resources

    state.numbersBreakdownRowStats?.let { statRows ->
        ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
            CodexGrid(
                    columns = 6,
                    alignment = Alignment.Center,
                    verticalSpacing = 4.dp,
                    horizontalSpacing = 4.dp,
                    modifier = modifier.testTag(StatsTestTag.NUMBERS_BREAKDOWN),
            ) {
                BreakdownColumn.values().forEach { column ->
                    if (column.mainTitle != null) {
                        val helpState =
                                if (column.mainTitleHorizontalSpan > 1) null
                                else {
                                    HelpShowcaseItem(
                                            helpTitle = column.helpTitle.get(resource),
                                            helpBody = column.helpBody.get(resource),
                                    ).asHelpState(helpListener)
                                }
                        item(
                                fillBox = true,
                                horizontalSpan = column.mainTitleHorizontalSpan,
                                verticalSpan = column.mainTitleVerticalSpan,
                        ) {
                            Text(
                                    text = column.mainTitle.get(),
                                    fontWeight = FontWeight.Bold,
                                    color = CodexTheme.colors.onListItemAppOnBackground,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                            .background(CodexTheme.colors.listAccentRowItemOnAppBackground)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                            .wrapContentHeight(Alignment.CenterVertically)
                                            .updateHelpDialogPosition(helpState)
                                            .clearAndSetSemantics { }
                            )
                        }
                    }
                }
                BreakdownColumn.values().forEach { column ->
                    if (column.secondaryTitle != null) {
                        val helpState =
                                if (column.mainTitleHorizontalSpan == 1 && column.mainTitle != null) null
                                else {
                                    HelpShowcaseItem(
                                            helpTitle = column.helpTitle.get(resource),
                                            helpBody = column.helpBody.get(resource),
                                    ).asHelpState(helpListener)
                                }
                        item(fillBox = true) {
                            Text(
                                    text = column.secondaryTitle.get(),
                                    fontWeight = FontWeight.Bold,
                                    color = CodexTheme.colors.onListItemAppOnBackground,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                            .background(CodexTheme.colors.listAccentRowItemOnAppBackground)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                            .updateHelpDialogPosition(helpState)
                                            .clearAndSetSemantics { }
                            )
                        }
                    }
                }

                val roundDistanceUnit = state.fullShootInfo.distanceUnit?.let { resource.getString(it) }
                statRows.forEach { row ->
                    val distance = BreakdownColumn.Distance.mapping(row).get(resource)
                    val rowDistanceUnit = roundDistanceUnit?.takeIf { row !is GrandTotalBreakdownRow } ?: ""

                    BreakdownColumn.values().forEach { column ->
                        val cellModifier = column.testTag?.let { Modifier.testTag(it) } ?: Modifier
                        val value = column.mapping(row).get(resource)

                        item(fillBox = true) {
                            Text(
                                    text = value,
                                    fontWeight = if (row is GrandTotalBreakdownRow) FontWeight.Bold else FontWeight.Normal,
                                    color = CodexTheme.colors.onListItemAppOnBackground,
                                    textAlign = TextAlign.Center,
                                    modifier = cellModifier
                                            .background(
                                                    if (row is GrandTotalBreakdownRow) CodexTheme.colors.listAccentRowItemOnAppBackground
                                                    else CodexTheme.colors.listItemOnAppBackground
                                            )
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                            .semantics {
                                                contentDescription = column
                                                        .cellContentDescription(value, distance, rowDistanceUnit)
                                                        .get(resource)
                                            }
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class BreakdownColumn(
        val mainTitle: ResOrActual<String>?,
        val secondaryTitle: ResOrActual<String>?,
        val mainTitleHorizontalSpan: Int,
        val mainTitleVerticalSpan: Int,
        val helpTitle: ResOrActual<String>,
        val helpBody: ResOrActual<String>,
        val mapping: (NumbersBreakdownRowStats) -> ResOrActual<String>,
        val testTag: StatsTestTag? = null,
        val cellContentDescription: (value: String, distance: String, distanceUnit: String) -> ResOrActual<String>,
) {
    Distance(
            mainTitle = StringResource(R.string.archer_round_stats__breakdown_distance_heading),
            secondaryTitle = null,
            mainTitleHorizontalSpan = 1,
            mainTitleVerticalSpan = 2,
            helpTitle = StringResource(R.string.help_archer_round_stats__breakdown_distance_title),
            helpBody = StringResource(R.string.help_archer_round_stats__breakdown_distance_body),
            mapping = {
                when (it) {
                    is DistanceBreakdownRow -> ResOrActual.Actual(it.distance.distance.toString())

                    is GrandTotalBreakdownRow ->
                        StringResource(R.string.archer_round_stats__breakdown_total_heading)

                    else -> throw NotImplementedError()
                }
            },
            testTag = StatsTestTag.NUMBERS_BREAKDOWN_DISTANCE,
            cellContentDescription = { value, _, distanceUnit ->
                StringResource(
                        R.string.archer_round_stats__breakdown_distance_cont_desc,
                        listOf(value, distanceUnit),
                )
            }
    ),
    Handicap(
            mainTitle = StringResource(R.string.archer_round_stats__breakdown_handicap_heading),
            secondaryTitle = null,
            mainTitleHorizontalSpan = 1,
            mainTitleVerticalSpan = 2,
            helpTitle = StringResource(R.string.help_archer_round_stats__breakdown_handicap_title),
            helpBody = StringResource(R.string.help_archer_round_stats__breakdown_handicap_body),
            mapping = { it.handicap.asDecimalFormat() },
            testTag = StatsTestTag.NUMBERS_BREAKDOWN_HANDICAP,
            cellContentDescription = { value, distance, distanceUnit ->
                StringResource(
                        R.string.archer_round_stats__breakdown_handicap_cont_desc,
                        listOf(value, distance, distanceUnit),
                )
            }
    ),
    AverageEnd(
            mainTitle = StringResource(R.string.archer_round_stats__breakdown_average_heading),
            secondaryTitle = StringResource(R.string.archer_round_stats__breakdown_end_heading),
            mainTitleHorizontalSpan = 2,
            mainTitleVerticalSpan = 1,
            helpTitle = StringResource(R.string.help_archer_round_stats__breakdown_end_score_title),
            helpBody = StringResource(R.string.help_archer_round_stats__breakdown_end_score_body),
            mapping = { it.averageEnd.asDecimalFormat() },
            cellContentDescription = { value, distance, distanceUnit ->
                StringResource(
                        R.string.archer_round_stats__breakdown_end_score_cont_desc,
                        listOf(value, distance, distanceUnit),
                )
            }
    ),
    AverageArrow(
            mainTitle = null,
            secondaryTitle = StringResource(R.string.archer_round_stats__breakdown_arrow_heading),
            mainTitleHorizontalSpan = 1,
            mainTitleVerticalSpan = 1,
            helpTitle = StringResource(R.string.help_archer_round_stats__breakdown_arrow_score_title),
            helpBody = StringResource(R.string.help_archer_round_stats__breakdown_arrow_score_body),
            mapping = { it.averageArrow.asDecimalFormat() },
            cellContentDescription = { value, distance, distanceUnit ->
                StringResource(
                        R.string.archer_round_stats__breakdown_arrow_score_cont_desc,
                        listOf(value, distance, distanceUnit),
                )
            }
    ),
    EndStDev(
            mainTitle = StringResource(R.string.archer_round_stats__breakdown_st_dev_heading),
            secondaryTitle = StringResource(R.string.archer_round_stats__breakdown_end_heading),
            mainTitleHorizontalSpan = 2,
            mainTitleVerticalSpan = 1,
            helpTitle = StringResource(R.string.help_archer_round_stats__breakdown_end_standard_dev_title),
            helpBody = StringResource(R.string.help_archer_round_stats__breakdown_end_standard_dev_body),
            mapping = { it.endStDev.asDecimalFormat(2) },
            cellContentDescription = { value, distance, distanceUnit ->
                StringResource(
                        R.string.archer_round_stats__breakdown_end_standard_dev_cont_desc,
                        listOf(value, distance, distanceUnit),
                )
            }
    ),
    ArrowStDev(
            mainTitle = null,
            secondaryTitle = StringResource(R.string.archer_round_stats__breakdown_arrow_heading),
            mainTitleHorizontalSpan = 1,
            mainTitleVerticalSpan = 1,
            helpTitle = StringResource(R.string.help_archer_round_stats__breakdown_arrow_standard_dev_title),
            helpBody = StringResource(R.string.help_archer_round_stats__breakdown_arrow_standard_dev_body),
            mapping = { it.arrowStdDev.asDecimalFormat(2) },
            cellContentDescription = { value, distance, distanceUnit ->
                StringResource(
                        R.string.archer_round_stats__breakdown_arrow_standard_dev_cont_desc,
                        listOf(value, distance, distanceUnit),
                )
            }
    ),
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        widthDp = 500,
)
@Composable
private fun NumberBreakdownSection_Preview() {
    CodexTheme {
        NumberBreakdownSection(
                StatsState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.yorkRoundData
                                    completeRoundWithFullSet()
                                },
                                archerHandicaps = ArcherHandicapsPreviewHelper.handicaps.take(1),
                        ),
                        extras = StatsExtras(),
                        classificationTablesUseCase = ClassificationTablesPreviewHelper.get(LocalContext.current),
                ),
                modifier = Modifier.padding(10.dp),
        )
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        widthDp = 500,
)
@Composable
private fun SingleDistance_NumberBreakdownSection_Preview() {
    CodexTheme {
        NumberBreakdownSection(
                StatsState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.wa25RoundData
                                    completeRoundWithFullSet()
                                },
                                archerHandicaps = ArcherHandicapsPreviewHelper.handicaps.take(1),
                        ),
                        extras = StatsExtras(),
                        classificationTablesUseCase = ClassificationTablesPreviewHelper.get(LocalContext.current),
                ),
                modifier = Modifier.padding(10.dp),
        )
    }
}
