package eywa.projectcodex.components.shootDetails.stats.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.classificationTables.ClassificationTablesPreviewHelper
import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.stats.StatsExtras
import eywa.projectcodex.components.shootDetails.stats.StatsIntent
import eywa.projectcodex.components.shootDetails.stats.StatsState
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag
import eywa.projectcodex.database.archer.DatabaseArcher
import eywa.projectcodex.database.archer.DatabaseArcherPreviewHelper
import eywa.projectcodex.database.bow.DatabaseBow
import eywa.projectcodex.database.bow.DatabaseBowPreviewHelper

@Composable
internal fun HandicapAndClassificationSection(
        state: StatsState,
        modifier: Modifier = Modifier,
        helpListener: (HelpShowcaseIntent) -> Unit,
        listener: (StatsIntent) -> Unit,
) {

    val (classification, isOfficialClassification) = state.classification.let { it ?: (null to false) }
    val showClassificationSection = state.archerInfo != null && state.bow != null

    ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier,
        ) {
            HandicapSection(state.fullShootInfo.handicap, helpListener, listener)

            if (showClassificationSection) {
                Delimiter(modifier = Modifier.padding(horizontal = 20.dp))

                ClassificationSection(
                        archerInfo = state.archerInfo,
                        bow = state.bow,
                        classification = classification,
                        isPredicted = !state.fullShootInfo.isRoundComplete,
                        isOfficial = isOfficialClassification,
                        helpListener = helpListener,
                        listener = listener,
                )
            }
        }
    }
}

@Composable
private fun SimpleClassificationSection(
        state: StatsState,
        modifier: Modifier = Modifier,
        listener: (StatsIntent) -> Unit,
) {
    val handicap = state.fullShootInfo.handicap ?: return
    val helpListener = { it: HelpShowcaseIntent -> listener(StatsIntent.HelpShowcaseAction(it)) }

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = modifier,
    ) {
        DataRow(
                title = stringResource(R.string.archer_round_stats__handicap),
                text = handicap.toString(),
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_archer_round_stats__round_handicap_title),
                        helpBody = stringResource(R.string.help_archer_round_stats__round_handicap_body),
                ).asHelpState(helpListener),
                textModifier = Modifier.testTag(StatsTestTag.HANDICAP_TEXT.getTestTag()),
        )


        if (
            state.fullShootInfo.round == null
            || state.fullShootInfo.arrowsShot == 0
            || state.archerInfo == null
            || state.bow == null
        ) return

        val unofficialSuffix =
                if (state.classification?.second == true) ""
                else stringResource(R.string.archer_round_stats__archer_info_classification_unofficial).let { " $it" }

        if (state.fullShootInfo.isRoundComplete) {
            Section(modifier = Modifier.testTag(StatsTestTag.CLASSIFICATION_SECTION)) {
                DataRow(
                        title = stringResource(R.string.archer_round_stats__archer_info_category),
                        text = listOf(
                                state.archerInfo.age.rawName,
                                state.archerInfo.genderString.get(),
                                state.bow.type.rawName,
                        ).joinToString(" "),
                        helpState = HelpShowcaseItem(
                                helpTitle = stringResource(R.string.help_archer_round_stats__archer_info_title),
                                helpBody = stringResource(R.string.help_archer_round_stats__archer_info_body),
                        ).asHelpState(helpListener),
                        textModifier = Modifier.testTag(StatsTestTag.CLASSIFICATION_CATEGORY.getTestTag()),
                        onClick = { listener(StatsIntent.EditArcherInfoClicked) },
                )
                DataRow(
                        title = stringResource(
                                R.string.archer_round_stats__archer_info_classification
                        ),
                        text = state.classification?.first?.fullStringId?.get()
                                ?.plus(unofficialSuffix)
                                ?: stringResource(R.string.archer_round_stats__archer_info_classification_none),
                        helpState = HelpShowcaseItem(
                                helpTitle = stringResource(R.string.help_archer_round_stats__archer_info_classification_title),
                                helpBody = stringResource(R.string.help_archer_round_stats__archer_info_classification_body),
                        ).asHelpState(helpListener),
                        textModifier = Modifier.testTag(StatsTestTag.CLASSIFICATION.getTestTag()),
                )
            }
        }
    }
}

@Composable
private fun HandicapSection(
        handicap: Int?,
        helpListener: (HelpShowcaseIntent) -> Unit,
        listener: (StatsIntent) -> Unit,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
                text = stringResource(R.string.archer_round_stats__round_handicap),
                style = CodexTypography.SMALL,
                color = CodexTheme.colors.onAppBackground,
                textAlign = TextAlign.Center,
        )
        Text(
                text = handicap?.toString() ?: "--",
                style = CodexTypography.LARGE,
                color = CodexTheme.colors.onAppBackground,
                modifier = Modifier
                        .updateHelpDialogPosition(
                                helpState = HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_archer_round_stats__round_handicap_title),
                                        helpBody = stringResource(R.string.help_archer_round_stats__round_handicap_body),
                                ).asHelpState(helpListener),
                        )
                        .padding(vertical = 5.dp)
        )
        Text(
                text = "Handicap tables",
                textAlign = TextAlign.Center,
                style = CodexTypography.SMALL.asClickableStyle(),
                modifier = Modifier
                        .updateHelpDialogPosition(
                                helpState = HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_archer_round_stats__expand_classification_title),
                                        helpBody = stringResource(R.string.help_archer_round_stats__expand_classification_body),
                                ).asHelpState(helpListener),
                        )
                        .clickable { listener(StatsIntent.ExpandHandicapsClicked) }
        )
    }
}

@Composable
private fun ClassificationSection(
        archerInfo: DatabaseArcher?,
        bow: DatabaseBow?,
        classification: Classification?,
        isPredicted: Boolean,
        isOfficial: Boolean,
        helpListener: (HelpShowcaseIntent) -> Unit,
        listener: (StatsIntent) -> Unit,
) {
    val labelResId = when {
        classification == null || isOfficial -> R.string.archer_round_stats__archer_info_classification_v2
        else -> R.string.archer_round_stats__archer_info_classification_v2_unofficial
    }

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
                text = stringResource(labelResId),
                textAlign = TextAlign.Center,
                style = CodexTypography.SMALL,
                color = CodexTheme.colors.onAppBackground,
        )
        Text(
                text = listOf(
                        archerInfo?.age?.rawName,
                        archerInfo?.genderString?.get(),
                        bow?.type?.rawName,
                ).joinToString(" "),
                textAlign = TextAlign.Center,
                style = CodexTypography.SMALL.asClickableStyle(),
                modifier = Modifier
                        .updateHelpDialogPosition(
                                helpState = HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_archer_round_stats__archer_info_title),
                                        helpBody = stringResource(R.string.help_archer_round_stats__archer_info_body),
                                ).asHelpState(helpListener),
                        )
                        .clickable { listener(StatsIntent.EditArcherInfoClicked) }
        )
        Text(
                text = classification?.shortStringId?.get()
                        ?: stringResource(R.string.archer_round_stats__archer_info_classification_v2_none),
                textAlign = TextAlign.Center,
                style = CodexTypography.NORMAL_PLUS,
                color = CodexTheme.colors.onAppBackground,
                modifier = Modifier
                        .updateHelpDialogPosition(
                                helpState = HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_archer_round_stats__archer_info_classification_title),
                                        helpBody = stringResource(R.string.help_archer_round_stats__archer_info_classification_body),
                                ).asHelpState(helpListener),
                        )
                        .padding(vertical = 5.dp)
        )
        Text(
                text = "Classification tables",
                textAlign = TextAlign.Center,
                style = CodexTypography.SMALL.asClickableStyle(),
                modifier = Modifier
                        .updateHelpDialogPosition(
                                helpState = HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_archer_round_stats__expand_classification_title),
                                        helpBody = stringResource(R.string.help_archer_round_stats__expand_classification_body),
                                ).asHelpState(helpListener),
                        )
                        .clickable { listener(StatsIntent.ExpandClassificationsClicked) }
        )
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        heightDp = 1300,
)
@Composable
fun HandicapAndClassificationSection_Preview() {
    CodexTheme {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            listOf(400, 426, 566, 717, 866, 999, 1110, 1197, 1266, 1320).forEach { finalScore ->
                HandicapAndClassificationSection(
                        state = StatsState(
                                main = ShootDetailsState(
                                        fullShootInfo = ShootPreviewHelperDsl.create {
                                            round = RoundPreviewHelper.wa1440RoundData
                                            completeRoundWithFinalScore(finalScore + 1)
                                        },
                                        archerInfo = DatabaseArcherPreviewHelper.default,
                                        bow = DatabaseBowPreviewHelper.default,
                                        wa1440FullRoundInfo = RoundPreviewHelper.wa1440RoundData,
                                ),
                                extras = StatsExtras(),
                                classificationTablesUseCase = ClassificationTablesPreviewHelper
                                        .get(LocalContext.current),
                        ),
                        helpListener = {},
                        listener = {},
                        modifier = Modifier.padding(10.dp)
                )
                StatsDivider()
            }
        }
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY
)
@Composable
fun PredictedUnofficial_HandicapAndClassificationSection_Preview() {
    CodexTheme {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            listOf(
                    ShootPreviewHelperDsl.create {
                        round = RoundPreviewHelper.wa1440RoundData
                        addIdenticalArrows(6, 9)
                    },
                    ShootPreviewHelperDsl.create {
                        round = RoundPreviewHelper.wa1440RoundData
                        roundSubTypeId = 4
                        completeRoundWithFinalScore(1400)
                    },
                    ShootPreviewHelperDsl.create {
                        round = RoundPreviewHelper.wa1440RoundData
                        roundSubTypeId = 4
                        addIdenticalArrows(6, 9)
                    },
            ).forEach { shootInfo ->
                HandicapAndClassificationSection(
                        state = StatsState(
                                main = ShootDetailsState(
                                        fullShootInfo = shootInfo,
                                        archerInfo = DatabaseArcherPreviewHelper.default
                                                .copy(isGent = false, age = ClassificationAge.OVER_50),
                                        bow = DatabaseBowPreviewHelper.default.copy(type = ClassificationBow.BAREBOW),
                                        wa1440FullRoundInfo = RoundPreviewHelper.wa1440RoundData,
                                ),
                                extras = StatsExtras(),
                                classificationTablesUseCase = ClassificationTablesPreviewHelper
                                        .get(LocalContext.current),
                        ),
                        helpListener = {},
                        listener = {},
                        modifier = Modifier.padding(10.dp)
                )
                StatsDivider()
            }
        }
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun NoArrows_HandicapAndClassificationSection_Preview() {
    CodexTheme {
        HandicapAndClassificationSection(
                state = StatsState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.wa1440RoundData
                                },
                                archerInfo = DatabaseArcherPreviewHelper.default,
                                bow = DatabaseBowPreviewHelper.default,
                                wa1440FullRoundInfo = RoundPreviewHelper.wa1440RoundData,
                        ),
                        extras = StatsExtras(),
                        classificationTablesUseCase = ClassificationTablesPreviewHelper
                                .get(LocalContext.current),
                ),
                helpListener = {},
                listener = {},
                modifier = Modifier.padding(10.dp)
        )
    }
}
