package eywa.projectcodex.components.shootDetails.stats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.ComposeUtils.semanticsWithContext
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.model.GoldsType
import kotlin.math.abs

@Composable
internal fun HsgPbAndIncompleteRoundInfoSection(
        fullShootInfo: FullShootInfo,
        modifier: Modifier = Modifier,
        simpleView: Boolean = false,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    ProvideTextStyle(value = CodexTypography.SMALL.copy(color = CodexTheme.colors.onAppBackground)) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = modifier
        ) {
            val extraPadding = if (fullShootInfo.hits != fullShootInfo.arrowsShot) 10.dp else 0.dp
            val goldsType = fullShootInfo.goldsTypes[0]

            HsgSection(
                    hits = fullShootInfo.hits,
                    score = fullShootInfo.score,
                    golds = fullShootInfo.golds(goldsType),
                    arrowsShot = fullShootInfo.arrowsShot,
                    goldsType = goldsType,
                    helpListener = helpListener,
            )
            PbRow(
                    fullShootInfo = fullShootInfo,
                    helpListener = helpListener,
                    modifier = Modifier.padding(top = extraPadding)
            )
            IncompleteRoundInfoSection(
                    remaining = fullShootInfo.remainingArrows,
                    predictedScore = fullShootInfo.predictedScore.takeIf { !simpleView },
                    helpListener = helpListener,
                    modifier = Modifier.padding(top = extraPadding)
            )
        }
    }
}

@Composable
private fun PbRow(
        fullShootInfo: FullShootInfo,
        modifier: Modifier = Modifier,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    if (fullShootInfo.isPersonalBest) {
        val pbStringId =
                if (fullShootInfo.isTiedPersonalBest) R.string.archer_round_stats__is_tied_pb
                else R.string.archer_round_stats__is_pb
        Text(
                text = stringResource(pbStringId),
                color = CodexTheme.colors.onPersonalBestTag,
                style = CodexTypography.SMALL_PLUS,
                modifier = modifier
                        .background(
                                color = CodexTheme.colors.personalBestTag,
                                shape = RoundedCornerShape(100),
                        )
                        .padding(horizontal = 10.dp)
                        .testTag(StatsTestTag.PB_TEXT)
                        .updateHelpDialogPosition(
                                helpState = HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_archer_round_stats__personal_best_title),
                                        helpBody = stringResource(R.string.help_archer_round_stats__personal_best_body),
                                ).asHelpState(helpListener),
                        )
        )
    }
}

@Composable
private fun IncompleteRoundInfoSection(
        remaining: Int?,
        predictedScore: Int?,
        modifier: Modifier = Modifier,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    if (remaining != null && remaining != 0) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
        ) {
            if (remaining > 0) {
                DataRow(
                        title = stringResource(R.string.archer_round_stats__remaining_arrows),
                        text = abs(remaining).toString(),
                        helpState = HelpShowcaseItem(
                                helpTitle = stringResource(R.string.help_archer_round_stats__remaining_arrows_title),
                                helpBody = stringResource(R.string.help_archer_round_stats__remaining_arrows_body),
                        ).asHelpState(helpListener),
                        textModifier = Modifier.testTag(StatsTestTag.REMAINING_ARROWS_TEXT),
                )
            }
            else {
                DataRow(
                        title = stringResource(R.string.archer_round_stats__surplus_arrows),
                        text = abs(remaining).toString(),
                        helpState = HelpShowcaseItem(
                                helpTitle = stringResource(R.string.help_archer_round_stats__surplus_arrows_title),
                                helpBody = stringResource(R.string.help_archer_round_stats__surplus_arrows_body),
                        ).asHelpState(helpListener),
                        textModifier = Modifier.testTag(StatsTestTag.SURPLUS_ARROWS_TEXT),
                )
            }
            if (predictedScore != null) {
                DataRow(
                        title = stringResource(R.string.archer_round_stats__predicted_score),
                        text = predictedScore.toString(),
                        helpState = HelpShowcaseItem(
                                helpTitle = stringResource(R.string.help_archer_round_stats__predicted_score_title),
                                helpBody = stringResource(R.string.help_archer_round_stats__predicted_score_body),
                        ).asHelpState(helpListener),
                        textModifier = Modifier.testTag(StatsTestTag.PREDICTED_SCORE_TEXT),
                )
            }
        }
    }
}


@Composable
private fun HsgSection(
        hits: Int,
        score: Int,
        golds: Int,
        arrowsShot: Int,
        goldsType: GoldsType,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
    ) {
        val (
                hitsLabel,
                scoreLabel,
                goldsLabel,
                hitsRef,
                scoreRef,
                goldsRef,
                delim1Ref,
                delim2Ref,
        ) = createRefs()

        val separatorMargin = 10.dp

        // Hits
        Text(
                text = stringResource(R.string.archer_round_stats__hits),
                modifier = Modifier
                        .constrainAs(hitsLabel) {
                            top.linkTo(scoreLabel.top)
                            bottom.linkTo(scoreLabel.bottom)
                            end.linkTo(hitsRef.end)
                        }
                        .clearAndSetSemantics { }
        )
        Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                        .constrainAs(hitsRef) {
                            top.linkTo(scoreRef.top)
                            bottom.linkTo(scoreRef.bottom)
                            end.linkTo(delim1Ref.start, margin = separatorMargin)
                        }
                        .updateHelpDialogPosition(
                                HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_archer_round_stats__hits_title),
                                        helpBody = stringResource(R.string.help_archer_round_stats__hits_body),
                                ).asHelpState(helpListener),
                        )
        ) {
            Text(
                    text = hits.toString(),
                    style = CodexTypography.LARGE,
                    color = CodexTheme.colors.onAppBackground,
                    modifier = Modifier
                            .testTag(StatsTestTag.HITS_TEXT)
                            .semanticsWithContext {
                                contentDescription =
                                        if (hits == arrowsShot) {
                                            it.getString(R.string.archer_round_stats__hits_content_description, hits)
                                        }
                                        else {
                                            it.getString(
                                                    R.string.archer_round_stats__hits_content_description_with_misses,
                                                    hits,
                                                    arrowsShot,
                                            )
                                        }
                            }
            )
            if (hits != arrowsShot) {
                Text(
                        text = stringResource(
                                R.string.archer_round_stats__hits_of_2,
                                arrowsShot,
                        ),
                        modifier = Modifier
                                .testTag(StatsTestTag.HITS_OF_TEXT)
                                .clearAndSetSemantics { }
                )
            }
        }

        // Separators
        Delimiter(
                modifier = Modifier.constrainAs(delim1Ref) {
                    top.linkTo(scoreRef.top)
                    bottom.linkTo(scoreRef.bottom)
                    end.linkTo(scoreRef.start, margin = separatorMargin)
                }
        )

        // Score
        Text(
                text = stringResource(R.string.archer_round_stats__score),
                modifier = Modifier
                        .constrainAs(scoreLabel) {
                            top.linkTo(parent.top)
                            bottom.linkTo(scoreRef.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .clearAndSetSemantics { }
        )
        Text(
                text = score.toString(),
                style = CodexTypography.X_LARGE,
                color = CodexTheme.colors.onAppBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                        .constrainAs(scoreRef) {
                            top.linkTo(scoreLabel.bottom)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .updateHelpDialogPosition(
                                HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_archer_round_stats__score_title),
                                        helpBody = stringResource(R.string.help_archer_round_stats__score_body),
                                ).asHelpState(helpListener),
                        )
                        .testTag(StatsTestTag.SCORE_TEXT)
                        .semanticsWithContext {
                            contentDescription =
                                    it.getString(R.string.archer_round_stats__score_content_description, score)
                        }
        )

        // Separators
        Delimiter(
                modifier = Modifier.constrainAs(delim2Ref) {
                    top.linkTo(scoreRef.top)
                    bottom.linkTo(scoreRef.bottom)
                    start.linkTo(scoreRef.end, margin = separatorMargin)
                }
        )

        // Golds
        Text(
                text = stringResource(goldsType.longStringId),
                modifier = Modifier
                        .constrainAs(goldsLabel) {
                            top.linkTo(scoreLabel.top)
                            bottom.linkTo(scoreLabel.bottom)
                            start.linkTo(goldsRef.start)
                        }
                        .clearAndSetSemantics { }
        )
        Text(
                text = golds.toString(),
                style = CodexTypography.LARGE,
                color = CodexTheme.colors.onAppBackground,
                modifier = Modifier
                        .constrainAs(goldsRef) {
                            top.linkTo(scoreRef.top)
                            bottom.linkTo(scoreRef.bottom)
                            start.linkTo(delim2Ref.end, margin = separatorMargin)
                        }
                        .updateHelpDialogPosition(
                                HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_archer_round_stats__golds_title),
                                        helpBody = stringResource(R.string.help_archer_round_stats__golds_body),
                                ).asHelpState(helpListener),
                        )
                        .testTag(StatsTestTag.GOLDS_TEXT)
                        .semanticsWithContext {
                            contentDescription = it.getString(
                                    R.string.archer_round_stats__simple_content_description,
                                    golds,
                                    it.getString(goldsType.longStringId),
                            )
                        }
        )
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
private fun HsgSection_Preview() {
    CodexTheme {
        HsgPbAndIncompleteRoundInfoSection(
                fullShootInfo = ShootPreviewHelperDsl.create {
                    addIdenticalArrows(24, 10)
                },
                modifier = Modifier.padding(10.dp)
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
private fun WithMisses_HsgSection_Preview() {
    CodexTheme {
        HsgPbAndIncompleteRoundInfoSection(
                fullShootInfo = ShootPreviewHelperDsl.create {
                    round = RoundPreviewHelper.indoorMetricRoundData
                    completeRoundWithFinalScore(240)
                },
                modifier = Modifier.padding(10.dp)
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
private fun Pb_HsgSection_Preview() {
    CodexTheme {
        HsgPbAndIncompleteRoundInfoSection(
                fullShootInfo = ShootPreviewHelperDsl.create {
                    addIdenticalArrows(48, 5)
                    appendArrows(List(2) { Arrow(0) })
                    isPersonalBest = true
                },
                modifier = Modifier.padding(10.dp)
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
private fun PbWithMisses_HsgSection_Preview() {
    CodexTheme {
        HsgPbAndIncompleteRoundInfoSection(
                fullShootInfo = ShootPreviewHelperDsl.create {
                    round = RoundPreviewHelper.indoorMetricRoundData
                    completeRoundWithFinalScore(240)
                    isPersonalBest = true
                    isTiedPersonalBest = true
                },
                modifier = Modifier.padding(10.dp)
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
private fun Incomplete_HsgSection_Preview() {
    CodexTheme {
        HsgPbAndIncompleteRoundInfoSection(
                fullShootInfo = ShootPreviewHelperDsl.create {
                    round = RoundPreviewHelper.indoorMetricRoundData
                    completeRound(5)
                    deleteLastArrow()
                },
                modifier = Modifier.padding(10.dp)
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
private fun IncompleteSimple_HsgSection_Preview() {
    CodexTheme {
        HsgPbAndIncompleteRoundInfoSection(
                fullShootInfo = ShootPreviewHelperDsl.create {
                    round = RoundPreviewHelper.indoorMetricRoundData
                    completeRound(5)
                    deleteLastArrow()
                },
                simpleView = true,
                modifier = Modifier.padding(10.dp)
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
private fun IncompleteWithMisses_HsgSection_Preview() {
    CodexTheme {
        HsgPbAndIncompleteRoundInfoSection(
                fullShootInfo = ShootPreviewHelperDsl.create {
                    round = RoundPreviewHelper.indoorMetricRoundData
                    completeRoundWithFinalScore(240)
                    deleteLastArrow()
                },
                modifier = Modifier.padding(10.dp)
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
private fun Surplus_HsgSection_Preview() {
    CodexTheme {
        HsgPbAndIncompleteRoundInfoSection(
                fullShootInfo = ShootPreviewHelperDsl.create {
                    round = RoundPreviewHelper.indoorMetricRoundData
                    completeRoundWithFinalScore(240)
                    appendArrows(List(2) { Arrow(0) })
                },
                modifier = Modifier.padding(10.dp)
        ) {}
    }
}
