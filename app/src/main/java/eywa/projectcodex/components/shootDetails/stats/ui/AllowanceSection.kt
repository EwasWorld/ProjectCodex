package eywa.projectcodex.components.shootDetails.stats.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.ComposeUtils.semanticsWithContext
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.classificationTables.ClassificationTablesPreviewHelper
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsPreviewHelper
import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.stats.StatsExtras
import eywa.projectcodex.components.shootDetails.stats.StatsIntent
import eywa.projectcodex.components.shootDetails.stats.StatsState

val separatorMargin = 10.dp

@Composable
internal fun AllowanceSection(
        state: StatsState,
        modifier: Modifier = Modifier,
        listener: (StatsIntent) -> Unit,
) {
    if (state.fullShootInfo.round == null) return
    val helpListener = { it: HelpShowcaseIntent -> listener(StatsIntent.HelpShowcaseAction(it)) }

    ProvideTextStyle(value = CodexTypography.SMALL.copy(color = CodexTheme.colors.onAppBackground)) {
        if (state.allowance == null) {
            NoAllowance(listener = listener, modifier = modifier)
        }
        else {
            ConstraintLayout(modifier = modifier) {
                val (
                        handicapLabel,
                        allowanceLabel,
                        adjustedLabel,
                        handicapRef,
                        allowanceRef,
                        adjustedRef,
                        delim1Ref,
                        delim2Ref,
                ) = createRefs()

                Handicap(
                        archerHandicap = state.archerHandicap!!,
                        handicapRef = handicapRef,
                        handicapLabel = handicapLabel,
                        helpListener = helpListener,
                        listener = listener,
                )

                Delimiter(
                        modifier = Modifier.constrainAs(delim1Ref) {
                            top.linkTo(handicapRef.top)
                            bottom.linkTo(handicapRef.bottom)
                            start.linkTo(handicapLabel.end, margin = separatorMargin)
                        }
                )

                RoundAllowance(
                        allowance = state.allowance!!,
                        handicapRef = handicapRef,
                        handicapLabel = handicapLabel,
                        allowanceLabel = allowanceLabel,
                        allowanceRef = allowanceRef,
                        delim1Ref = delim1Ref,
                        helpListener = helpListener,
                )

                if ((state.adjustedFinalScore ?: state.predictedAdjustedScore) != null) {
                    Delimiter(
                            modifier = Modifier.constrainAs(delim2Ref) {
                                top.linkTo(handicapRef.top)
                                bottom.linkTo(handicapRef.bottom)
                                start.linkTo(allowanceLabel.end, margin = separatorMargin)
                            }
                    )

                    AdjustedScore(
                            adjustedFinalScore = state.adjustedFinalScore,
                            predictedAdjustedScore = state.predictedAdjustedScore,
                            handicapRef = handicapRef,
                            handicapLabel = handicapLabel,
                            delim2Ref = delim2Ref,
                            adjustedLabel = adjustedLabel,
                            adjustedRef = adjustedRef,
                            helpListener = helpListener,
                    )
                }
            }
        }
    }
}

@Composable
private fun NoAllowance(
        modifier: Modifier = Modifier,
        listener: (StatsIntent) -> Unit,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                    .updateHelpDialogPosition(
                            HelpShowcaseItem(
                                    helpTitle = stringResource(R.string.help_archer_round_stats__no_round_allowance_title),
                                    helpBody = stringResource(R.string.help_archer_round_stats__no_round_allowance_body),
                            ).asHelpState { listener(StatsIntent.HelpShowcaseAction(it)) },
                    )
    ) {
        Text(
                text = stringResource(R.string.archer_round_stats__round_allowance),
                textAlign = TextAlign.Center,
                modifier = Modifier.clearAndSetSemantics { }
        )
        Text(
                text = stringResource(R.string.archer_round_stats__round_allowance_no_handicap),
                textAlign = TextAlign.Center,
                style = LocalTextStyle.current.asClickableStyle(),
                modifier = Modifier.clickable { listener(StatsIntent.EditHandicapInfoClicked) }
        )
    }
}

@Composable
private fun ConstraintLayoutScope.Handicap(
        archerHandicap: Int,
        handicapRef: ConstrainedLayoutReference,
        handicapLabel: ConstrainedLayoutReference,
        helpListener: (HelpShowcaseIntent) -> Unit,
        listener: (StatsIntent) -> Unit,
) {
    Text(
            text = stringResource(R.string.archer_round_stats__archer_handicap_two_lines),
            textAlign = TextAlign.Center,
            modifier = Modifier
                    .constrainAs(handicapLabel) {
                        top.linkTo(parent.top)
                        bottom.linkTo(handicapRef.top)
                        start.linkTo(parent.start)
                    }
                    .clearAndSetSemantics { }
    )
    Text(
            text = archerHandicap.toString(),
            style = CodexTypography.NORMAL_PLUS.asClickableStyle(),
            modifier = Modifier
                    .clickable { listener(StatsIntent.EditHandicapInfoClicked) }
                    .constrainAs(handicapRef) {
                        top.linkTo(handicapLabel.bottom, margin = 5.dp)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(handicapLabel.start)
                        end.linkTo(handicapLabel.end)
                    }
                    .updateHelpDialogPosition(
                            HelpShowcaseItem(
                                    helpTitle = stringResource(R.string.help_archer_round_stats__archer_handicap_title),
                                    helpBody = stringResource(R.string.help_archer_round_stats__archer_handicap_body),
                            ).asHelpState(helpListener)
                    )
                    .testTag(StatsTestTag.ARCHER_HANDICAP_TEXT)
                    .semanticsWithContext {
                        contentDescription = it.getString(
                                R.string.archer_round_stats__simple_content_description,
                                archerHandicap,
                                it.getString(R.string.archer_round_stats__archer_handicap),
                        )
                    }
    )
}

@Composable
private fun ConstraintLayoutScope.RoundAllowance(
        allowance: Int,
        handicapRef: ConstrainedLayoutReference,
        handicapLabel: ConstrainedLayoutReference,
        allowanceLabel: ConstrainedLayoutReference,
        allowanceRef: ConstrainedLayoutReference,
        delim1Ref: ConstrainedLayoutReference,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    Text(
            text = stringResource(R.string.archer_round_stats__round_allowance_two_lines),
            textAlign = TextAlign.Center,
            modifier = Modifier
                    .constrainAs(allowanceLabel) {
                        top.linkTo(handicapLabel.top)
                        bottom.linkTo(handicapLabel.bottom)
                        start.linkTo(delim1Ref.end, margin = separatorMargin)
                    }
                    .clearAndSetSemantics { }
    )
    Text(
            text = allowance.toString(),
            style = CodexTypography.NORMAL,
            color = CodexTheme.colors.onAppBackground,
            modifier = Modifier
                    .constrainAs(allowanceRef) {
                        top.linkTo(handicapRef.top)
                        bottom.linkTo(handicapRef.bottom)
                        start.linkTo(allowanceLabel.start)
                        end.linkTo(allowanceLabel.end)
                    }
                    .updateHelpDialogPosition(
                            HelpShowcaseItem(
                                    helpTitle = stringResource(R.string.help_archer_round_stats__allowance_title),
                                    helpBody = stringResource(R.string.help_archer_round_stats__allowance_body),
                            ).asHelpState(helpListener)
                    )
                    .testTag(StatsTestTag.ALLOWANCE_TEXT)
                    .semanticsWithContext {
                        contentDescription = it.getString(
                                R.string.archer_round_stats__simple_content_description,
                                allowance,
                                it.getString(R.string.archer_round_stats__round_allowance),
                        )
                    }
    )
}

@Composable
private fun ConstraintLayoutScope.AdjustedScore(
        adjustedFinalScore: Int?,
        predictedAdjustedScore: Int?,
        handicapRef: ConstrainedLayoutReference,
        handicapLabel: ConstrainedLayoutReference,
        delim2Ref: ConstrainedLayoutReference,
        adjustedLabel: ConstrainedLayoutReference,
        adjustedRef: ConstrainedLayoutReference,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    val adjustedScoreHelpState: HelpState
    val title: String
    val description: String

    if (adjustedFinalScore != null) {
        title = stringResource(R.string.archer_round_stats__adjusted_score_two_lines)
        description = stringResource(R.string.archer_round_stats__adjusted_score)
        adjustedScoreHelpState = HelpShowcaseItem(
                helpTitle = stringResource(R.string.help_archer_round_stats__adjusted_score_title),
                helpBody = stringResource(R.string.help_archer_round_stats__adjusted_score_body),
        ).asHelpState(helpListener)
    }
    else {
        title = stringResource(R.string.archer_round_stats__predicted_adjusted_score_two_lines)
        description = stringResource(R.string.archer_round_stats__predicted_adjusted_score)
        adjustedScoreHelpState = HelpShowcaseItem(
                helpTitle = stringResource(R.string.help_archer_round_stats__predicted_adjusted_score_title),
                helpBody = stringResource(R.string.help_archer_round_stats__predicted_adjusted_score_body),
        ).asHelpState(helpListener)
    }

    Text(
            text = title,
            textAlign = TextAlign.Center,
            modifier = Modifier
                    .constrainAs(adjustedLabel) {
                        top.linkTo(handicapLabel.top)
                        bottom.linkTo(handicapLabel.bottom)
                        start.linkTo(delim2Ref.end, margin = separatorMargin)
                    }
                    .clearAndSetSemantics { }
    )

    Text(
            text = (adjustedFinalScore ?: predictedAdjustedScore).toString(),
            style = CodexTypography.NORMAL,
            color = CodexTheme.colors.onAppBackground,
            modifier = Modifier
                    .updateHelpDialogPosition(adjustedScoreHelpState)
                    .constrainAs(adjustedRef) {
                        top.linkTo(handicapRef.top)
                        bottom.linkTo(handicapRef.bottom)
                        start.linkTo(adjustedLabel.start)
                        end.linkTo(adjustedLabel.end)
                    }
                    .testTag(StatsTestTag.ADJUSTED_SCORE_TEXT)
                    .semanticsWithContext {
                        contentDescription = it.getString(
                                R.string.archer_round_stats__simple_content_description,
                                adjustedFinalScore ?: predictedAdjustedScore,
                                description,
                        )
                    }
    )
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
private fun AllowanceSection_Preview() {
    CodexTheme {
        AllowanceSection(
                StatsState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.yorkRoundData
                                    completeRoundWithFinalScore(1000)
                                },
                                archerHandicaps = ArcherHandicapsPreviewHelper.handicaps.take(1),
                        ),
                        extras = StatsExtras(),
                        classificationTablesUseCase = ClassificationTablesPreviewHelper.get(LocalContext.current),
                ),
                modifier = Modifier.padding(10.dp),
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
private fun Predicted_AllowanceSection_Preview() {
    CodexTheme {
        AllowanceSection(
                StatsState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.yorkRoundData
                                    addIdenticalArrows(1, 9)
                                },
                                archerHandicaps = ArcherHandicapsPreviewHelper.handicaps.take(1),
                        ),
                        extras = StatsExtras(),
                        classificationTablesUseCase = ClassificationTablesPreviewHelper.get(LocalContext.current),
                ),
                modifier = Modifier.padding(10.dp),
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
private fun NoHandicap_AllowanceSection_Preview() {
    CodexTheme {
        AllowanceSection(
                StatsState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.yorkRoundData
                                    addIdenticalArrows(1, 9)
                                },
                        ),
                        extras = StatsExtras(),
                        classificationTablesUseCase = ClassificationTablesPreviewHelper.get(LocalContext.current),
                ),
                modifier = Modifier.padding(10.dp),
        ) {}
    }
}
