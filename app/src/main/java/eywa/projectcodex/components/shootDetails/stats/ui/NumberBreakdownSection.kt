package eywa.projectcodex.components.shootDetails.stats.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.grid.CodexGridColumnMetadata
import eywa.projectcodex.common.sharedUi.grid.CodexGridWithHeaders
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
import eywa.projectcodex.model.user.CodexUser

@Composable
internal fun NumberBreakdownSection(
        state: StatsState,
        modifier: Modifier = Modifier,
        listener: (StatsIntent) -> Unit = { },
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(StatsIntent.HelpShowcaseAction(it)) }
    val distanceUnit = state.fullShootInfo.distanceUnit ?: return

    state.numbersBreakdownRowStats ?: return
    ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        CodexGridWithHeaders(
                data = state.numbersBreakdownRowStats!!,
                columnMetadata = BreakdownColumn.entries.toList(),
                extraData = StringResource(distanceUnit),
                helpListener = helpListener,
                modifier = modifier.testTag(StatsTestTag.NUMBERS_BREAKDOWN)
        )
    }
}

enum class BreakdownColumn(
        override val primaryTitle: ResOrActual<String>?,
        override val secondaryTitle: ResOrActual<String>?,
        override val primaryTitleHorizontalSpan: Int,
        override val primaryTitleVerticalSpan: Int,
        override val helpTitle: ResOrActual<String>,
        override val helpBody: ResOrActual<String>,
        override val mapping: (NumbersBreakdownRowStats) -> ResOrActual<String>,
        override val testTag: StatsTestTag? = null,
        val contentDescription: (value: ResOrActual<String>, distance: ResOrActual<String>, distanceUnit: ResOrActual<String>) -> ResOrActual<String>,
) : CodexGridColumnMetadata<NumbersBreakdownRowStats, ResOrActual<String>> {
    Distance(
            primaryTitle = StringResource(R.string.archer_round_stats__breakdown_distance_heading),
            secondaryTitle = null,
            primaryTitleHorizontalSpan = 1,
            primaryTitleVerticalSpan = 2,
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
            contentDescription = { value, _, distanceUnit ->
                StringResource(
                        R.string.archer_round_stats__breakdown_distance_cont_desc,
                        listOf(value, distanceUnit),
                )
            }
    ),
    Handicap(
            primaryTitle = StringResource(R.string.archer_round_stats__breakdown_handicap_heading),
            secondaryTitle = null,
            primaryTitleHorizontalSpan = 1,
            primaryTitleVerticalSpan = 2,
            helpTitle = StringResource(R.string.help_archer_round_stats__breakdown_handicap_title),
            helpBody = StringResource(R.string.help_archer_round_stats__breakdown_handicap_body),
            mapping = { it.handicap.asDecimalFormat() },
            testTag = StatsTestTag.NUMBERS_BREAKDOWN_HANDICAP,
            contentDescription = { value, distance, distanceUnit ->
                StringResource(
                        R.string.archer_round_stats__breakdown_handicap_cont_desc,
                        listOf(value, distance, distanceUnit),
                )
            }
    ),
    AverageEnd(
            primaryTitle = StringResource(R.string.archer_round_stats__breakdown_average_heading),
            secondaryTitle = StringResource(R.string.archer_round_stats__breakdown_end_heading),
            primaryTitleHorizontalSpan = 2,
            primaryTitleVerticalSpan = 1,
            helpTitle = StringResource(R.string.help_archer_round_stats__breakdown_end_score_title),
            helpBody = StringResource(R.string.help_archer_round_stats__breakdown_end_score_body),
            mapping = { it.averageEnd.asDecimalFormat() },
            contentDescription = { value, distance, distanceUnit ->
                StringResource(
                        R.string.archer_round_stats__breakdown_end_score_cont_desc,
                        listOf(value, distance, distanceUnit),
                )
            }
    ),
    AverageArrow(
            primaryTitle = null,
            secondaryTitle = StringResource(R.string.archer_round_stats__breakdown_arrow_heading),
            primaryTitleHorizontalSpan = 1,
            primaryTitleVerticalSpan = 1,
            helpTitle = StringResource(R.string.help_archer_round_stats__breakdown_arrow_score_title),
            helpBody = StringResource(R.string.help_archer_round_stats__breakdown_arrow_score_body),
            mapping = { it.averageArrow.asDecimalFormat() },
            contentDescription = { value, distance, distanceUnit ->
                StringResource(
                        R.string.archer_round_stats__breakdown_arrow_score_cont_desc,
                        listOf(value, distance, distanceUnit),
                )
            }
    ),
    EndStDev(
            primaryTitle = StringResource(R.string.archer_round_stats__breakdown_st_dev_heading),
            secondaryTitle = StringResource(R.string.archer_round_stats__breakdown_end_heading),
            primaryTitleHorizontalSpan = 2,
            primaryTitleVerticalSpan = 1,
            helpTitle = StringResource(R.string.help_archer_round_stats__breakdown_end_standard_dev_title),
            helpBody = StringResource(R.string.help_archer_round_stats__breakdown_end_standard_dev_body),
            mapping = { it.endStDev.asDecimalFormat(2) },
            contentDescription = { value, distance, distanceUnit ->
                StringResource(
                        R.string.archer_round_stats__breakdown_end_standard_dev_cont_desc,
                        listOf(value, distance, distanceUnit),
                )
            }
    ),
    ArrowStDev(
            primaryTitle = null,
            secondaryTitle = StringResource(R.string.archer_round_stats__breakdown_arrow_heading),
            primaryTitleHorizontalSpan = 1,
            primaryTitleVerticalSpan = 1,
            helpTitle = StringResource(R.string.help_archer_round_stats__breakdown_arrow_standard_dev_title),
            helpBody = StringResource(R.string.help_archer_round_stats__breakdown_arrow_standard_dev_body),
            mapping = { it.arrowStdDev.asDecimalFormat(2) },
            contentDescription = { value, distance, distanceUnit ->
                StringResource(
                        R.string.archer_round_stats__breakdown_arrow_standard_dev_cont_desc,
                        listOf(value, distance, distanceUnit),
                )
            }
    ),
    ;

    override val cellContentDescription: (NumbersBreakdownRowStats, ResOrActual<String>) -> ResOrActual<String>
        get() = { rowData, distanceUnit ->
            contentDescription(mapping(rowData), Distance.mapping(rowData), distanceUnit)
        }
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
                                shootId = 1,
                                user = CodexUser(),
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.yorkRoundData
                                    completeRoundWithFullSet()
                                },
                                archerHandicaps = ArcherHandicapsPreviewHelper.handicaps.take(1),
                        ),
                        extras = StatsExtras(),
                        classificationTablesUseCase = ClassificationTablesPreviewHelper.get(LocalContext.current),
                ),
                modifier = Modifier.padding(10.dp)
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
                                shootId = 1,
                                user = CodexUser(),
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.wa25RoundData
                                    completeRoundWithFullSet()
                                },
                                archerHandicaps = ArcherHandicapsPreviewHelper.handicaps.take(1),
                        ),
                        extras = StatsExtras(),
                        classificationTablesUseCase = ClassificationTablesPreviewHelper.get(LocalContext.current),
                ),
                modifier = Modifier.padding(10.dp)
        )
    }
}
