package eywa.projectcodex.components.shootDetails.stats.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectFaceRow
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.model.FullShootInfo

@Composable
fun DateAndRoundSection(
        fullShootInfo: FullShootInfo,
        modifier: Modifier = Modifier,
        editClickedListener: () -> Unit,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        EditBox(
                testTag = StatsTestTag.SHOOT_DETAIL_SECTION,
                editContentDescription = stringResource(R.string.archer_round_stats__edit_shoot_content_description),
                editListener = editClickedListener,
                editHelpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_archer_round_stats__edit_round_info_title),
                        helpBody = stringResource(R.string.help_archer_round_stats__edit_round_info_body),
                ).asHelpState(helpListener),
                modifier = modifier
        ) {
            Text(
                    text = DateTimeFormat.LONG_DATE_TIME.format(fullShootInfo.shoot.dateShot),
                    modifier = Modifier
                            .testTag(StatsTestTag.DATE_TEXT)
                            .updateHelpDialogPosition(
                                    helpState = HelpShowcaseItem(
                                            helpTitle = stringResource(R.string.help_archer_round_stats__date_title),
                                            helpBody = stringResource(R.string.help_archer_round_stats__date_body),
                                    ).asHelpState(helpListener),
                            )
            )
            fullShootInfo.h2h?.headToHead?.let {
                Spacer(modifier = Modifier)
            }
            DataRow(
                    title = stringResource(R.string.archer_round_stats__round),
                    text = fullShootInfo.displayName
                            ?: stringResource(R.string.archer_round_stats__no_round),
                    textModifier = Modifier.testTag(StatsTestTag.ROUND_TEXT),
                    helpState = HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_archer_round_stats__round_title),
                            helpBody = stringResource(R.string.help_archer_round_stats__round_body),
                    ).asHelpState(helpListener),
                    titleStyle = CodexTypography.SMALL.copy(color = CodexTheme.colors.onAppBackground),
            )
            ProvideTextStyle(value = CodexTypography.SMALL.copy(color = CodexTheme.colors.onAppBackground)) {
                fullShootInfo.h2h?.headToHead?.let {
                    val team = if (it.teamSize > 1) "Teams of ${it.teamSize}" else "Individual"
                    val style = if (it.isRecurveStyle) "Set points" else "Total score"
                    val rank = if (it.qualificationRank != null) ", Rank ${it.qualificationRank}" else ""
                    val format = if (it.isStandardFormat) "" else "\nNon-standard format"
                    val totalArchers =
                            if (it.totalArchers == null) ""
                            else if (it.qualificationRank == null) ", ${it.totalArchers} archers"
                            else " of ${it.totalArchers}"
                    Text(
                            text = "$team, $style$rank$totalArchers$format",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag(StatsTestTag.ROUND_H2H_INFO_TEXT)
                    )
                    Spacer(modifier = Modifier)
                }
                SelectFaceRow(
                        selectedFaces = fullShootInfo.faces,
                        helpListener = helpListener,
                        onClick = null,
                )
            }
        }
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY
)
@Composable
fun DateAndRoundSection_Preview() {
    CodexTheme {
        DateAndRoundSection(
                fullShootInfo = ShootPreviewHelperDsl.create {
                },
                editClickedListener = {},
                helpListener = {},
        )
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY
)
@Composable
fun Round_DateAndRoundSection_Preview() {
    CodexTheme {
        DateAndRoundSection(
                fullShootInfo = ShootPreviewHelperDsl.create {
                    round = RoundPreviewHelper.wa1440RoundData
                },
                editClickedListener = {},
                helpListener = {},
        )
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY
)
@Composable
fun RoundAndFaces_DateAndRoundSection_Preview() {
    CodexTheme {
        DateAndRoundSection(
                fullShootInfo = ShootPreviewHelperDsl.create {
                    round = RoundPreviewHelper.wa1440RoundData
                    faces = listOf(RoundFace.FULL, RoundFace.FULL, RoundFace.HALF, RoundFace.HALF)
                },
                editClickedListener = {},
                helpListener = {},
        )
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY
)
@Composable
fun H2h_DateAndRoundSection_Preview() {
    CodexTheme {
        DateAndRoundSection(
                fullShootInfo = ShootPreviewHelperDsl.create {
                    round = RoundPreviewHelper.wa1440RoundData
                    faces = listOf(RoundFace.FULL, RoundFace.FULL, RoundFace.HALF, RoundFace.HALF)
                    addH2h {
                        headToHead = headToHead.copy(isRecurveStyle = true, teamSize = 1, qualificationRank = 5)
                    }
                },
                editClickedListener = {},
                helpListener = {},
        )
    }
}
