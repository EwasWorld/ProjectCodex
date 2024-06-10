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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.sharedUi.CodexGrid
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.utils.classificationTables.ClassificationTablesPreviewHelper
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsPreviewHelper
import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.stats.DistanceExtra
import eywa.projectcodex.components.shootDetails.stats.ExtraStats
import eywa.projectcodex.components.shootDetails.stats.GrandTotalExtra
import eywa.projectcodex.components.shootDetails.stats.StatsExtras
import eywa.projectcodex.components.shootDetails.stats.StatsState

@Composable
internal fun NumberBreakdownSection(
        state: StatsState,
        modifier: Modifier = Modifier,
) {
    state.extras?.let { extras ->
        ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
            CodexGrid(
                    columns = 6,
                    alignment = Alignment.Center,
                    verticalSpacing = 4.dp,
                    horizontalSpacing = 4.dp,
                    modifier = modifier,
            ) {
                BreakdownColumn.values().forEach { column ->
                    if (!column.mainTitle.isNullOrBlank()) {
                        item(
                                fillBox = true,
                                horizontalSpan = column.mainTitleHorizontalSpan,
                                verticalSpan = column.mainTitleVerticalSpan,
                        ) {
                            Text(
                                    text = column.mainTitle,
                                    fontWeight = FontWeight.Bold,
                                    color = CodexTheme.colors.onListItemAppOnBackground,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                            .background(CodexTheme.colors.listAccentRowItemOnAppBackground)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                            .wrapContentHeight(Alignment.CenterVertically)
                            )
                        }
                    }
                }
                BreakdownColumn.values().forEach { column ->
                    if (!column.secondaryTitle.isNullOrBlank()) {
                        item(fillBox = true) {
                            Text(
                                    text = column.secondaryTitle,
                                    fontWeight = FontWeight.Bold,
                                    color = CodexTheme.colors.onListItemAppOnBackground,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                            .background(CodexTheme.colors.listAccentRowItemOnAppBackground)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                extras.forEach { extra ->
                    BreakdownColumn.values().forEach { column ->
                        item(fillBox = true) {
                            Text(
                                    text = column.mapping(extra),
                                    fontWeight = if (extra is GrandTotalExtra) FontWeight.Bold else FontWeight.Normal,
                                    color = CodexTheme.colors.onListItemAppOnBackground,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                            .background(
                                                    if (extra is GrandTotalExtra) CodexTheme.colors.listAccentRowItemOnAppBackground
                                                    else CodexTheme.colors.listItemOnAppBackground
                                            )
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class BreakdownColumn(
        val mainTitle: String?,
        val secondaryTitle: String?,
        val mainTitleHorizontalSpan: Int,
        val mainTitleVerticalSpan: Int,
        val mapping: (ExtraStats) -> String,
) {
    Distance("Dist", null, 1, 2, {
        when (it) {
            is DistanceExtra -> it.distance.distance.toString()
            is GrandTotalExtra -> "Total"
            else -> throw NotImplementedError()
        }
    }),
    Handicap("HC", null, 1, 2, { it.handicap.format() }),
    AverageEnd("Average Score", "End", 2, 1, { it.averageEnd.format() }),
    AverageArrow("", "Arrow", 1, 1, { it.averageArrow.format() }),
    EndStDev("Std Dev", "End", 2, 1, { it.endStDev.format(2) }),
    ArrowStDev("", "Arrow", 1, 1, { it.arrowStdDev.format(2) }),
}

private fun Float?.format(decimalPlaces: Int = 1) = this?.let { "%.${decimalPlaces}f".format(this) } ?: "-"
private fun Double?.format(decimalPlaces: Int = 1) = this?.let { "%.${decimalPlaces}f".format(this) } ?: "-"

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
