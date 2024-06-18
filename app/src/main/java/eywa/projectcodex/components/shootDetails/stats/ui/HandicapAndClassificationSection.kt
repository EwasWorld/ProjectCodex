package eywa.projectcodex.components.shootDetails.stats.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
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
import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.stats.StatsExtras
import eywa.projectcodex.components.shootDetails.stats.StatsIntent
import eywa.projectcodex.components.shootDetails.stats.StatsState
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
                        archerInfo = state.archerInfo!!,
                        bow = state.bow!!,
                        classification = classification,
                        isOfficial = isOfficialClassification,
                        helpListener = helpListener,
                        listener = listener,
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
                text = stringResource(R.string.archer_round_stats__round_handicap_two_lines),
                style = CodexTypography.SMALL,
                color = CodexTheme.colors.onAppBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.clearAndSetSemantics { }
        )
        Text(
                text = handicap?.toString() ?: stringResource(R.string.archer_round_stats__round_handicap_placeholder),
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
                        .testTag(StatsTestTag.HANDICAP_TEXT)
                        .semanticsWithContext {
                            contentDescription =
                                    if (handicap == null) {
                                        it.getString(R.string.archer_round_stats__round_handicap_placeholder_cont_desc)
                                    }
                                    else {
                                        it.getString(
                                                R.string.archer_round_stats__simple_content_description,
                                                handicap,
                                                it.getString(R.string.archer_round_stats__round_handicap),
                                        )
                                    }
                            onClick(
                                    it.getString(R.string.archer_round_stats__handicap_tables_link_cont_desc)
                            ) { listener(StatsIntent.ExpandHandicapsClicked); true }
                        }
        )
        Text(
                text = stringResource(R.string.archer_round_stats__handicap_tables_link),
                textAlign = TextAlign.Center,
                style = CodexTypography.SMALL.asClickableStyle(),
                modifier = Modifier
                        .testTag(StatsTestTag.HANDICAP_TABLES)
                        .clearAndSetSemantics { }
                        .updateHelpDialogPosition(
                                helpState = HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_archer_round_stats__expand_handicaps_title),
                                        helpBody = stringResource(R.string.help_archer_round_stats__expand_handicaps_body),
                                ).asHelpState(helpListener),
                        )
                        .clickable { listener(StatsIntent.ExpandHandicapsClicked) }
        )
    }
}

@Composable
private fun ClassificationSection(
        archerInfo: DatabaseArcher,
        bow: DatabaseBow,
        classification: Classification?,
        isOfficial: Boolean,
        helpListener: (HelpShowcaseIntent) -> Unit,
        listener: (StatsIntent) -> Unit,
) {
    val title = stringResource(
            when {
                classification == null || isOfficial -> R.string.archer_round_stats__archer_info_classification_v2
                else -> R.string.archer_round_stats__archer_info_classification_v2_unofficial
            }
    )
    val category = stringResource(
            R.string.archer_round_stats__category,
            archerInfo.age.rawName,
            archerInfo.genderString.get(),
            bow.type.rawName,
    )

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
                text = title,
                textAlign = TextAlign.Center,
                style = CodexTypography.SMALL,
                color = CodexTheme.colors.onAppBackground,
                modifier = Modifier
                        .testTag(StatsTestTag.CLASSIFICATION_TITLE)
                        .clearAndSetSemantics { }
        )
        Text(
                text = category,
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
                        .testTag(StatsTestTag.CLASSIFICATION_CATEGORY)
                        .semanticsWithContext {
                            contentDescription = it.getString(
                                    R.string.archer_round_stats__archer_info_classification_category_cont_desc,
                                    category,
                            )
                        }
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
                        .testTag(StatsTestTag.CLASSIFICATION)
                        .semanticsWithContext {
                            contentDescription =
                                    it.getString(
                                            R.string.archer_round_stats__classification_content_description,
                                            classification?.shortStringId?.get(it.resources)
                                                    ?: it.getString(R.string.archer_round_stats__archer_info_classification_v2_none),
                                            title,
                                    )
                            onClick(
                                    it.getString(R.string.archer_round_stats__classification_tables_link_cont_desc)
                            ) { listener(StatsIntent.ExpandClassificationsClicked); true }
                        }
        )
        Text(
                text = stringResource(R.string.archer_round_stats__classification_tables_link),
                textAlign = TextAlign.Center,
                style = CodexTypography.SMALL.asClickableStyle(),
                modifier = Modifier
                        .testTag(StatsTestTag.CLASSIFICATION_TABLES)
                        .clearAndSetSemantics { }
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
