package eywa.projectcodex.components.referenceTables.awards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.LoadingScreen
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsStatePreviewHelper
import eywa.projectcodex.components.referenceTables.awards.AwardsIntent
import eywa.projectcodex.components.referenceTables.awards.AwardsState
import eywa.projectcodex.components.referenceTables.awards.AwardsViewModel
import eywa.projectcodex.components.referenceTables.classificationTables.CategoryInput

@Composable
fun AwardsScreen(
        viewModel: AwardsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    if (state.hasLoaded) AwardsScreen(state) { viewModel.handleEvent(it) }
    else LoadingScreen()
}

@Composable
fun AwardsScreen(
        state: AwardsState,
        listener: (AwardsIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(AwardsIntent.HelpShowcaseAction(it)) }

    ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .fillMaxSize()
                        .background(CodexTheme.colors.appBackground)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = CodexTheme.dimens.screenPadding + 10.dp)
                        .testTag(AwardsTestTag.SCREEN)
        ) {
            CategoryInput(
                    label = stringResource(R.string.classification_tables__bow_title),
                    currentValue = state.bow.rawName,
                    values = ClassificationBow.entries.map { it.rawName },
                    testTag = AwardsTestTag.BOW_SELECTOR,
                    onClick = { listener(AwardsIntent.BowClicked) },
                    onItemClick = { listener(AwardsIntent.BowSelected(ClassificationBow.entries[it])) },
                    onDismiss = { listener(AwardsIntent.CloseDropdown) },
                    expanded = state.bowDropdownExpanded,
                    modifier = Modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
            )

            Divider(
                    color = CodexTheme.colors.onAppBackground,
                    modifier = Modifier.padding(horizontal = CodexTheme.dimens.screenPadding.times(3))
            )

            ClubSection(
                    state = state,
                    helpListener = helpListener,
                    modifier = Modifier.padding(bottom = 30.dp)
            )

            Divider(
                    color = CodexTheme.colors.onAppBackground,
                    modifier = Modifier.padding(horizontal = CodexTheme.dimens.screenPadding.times(3))
            )

            AgbSection(
                    state = state,
                    helpListener = helpListener,
            )
        }
    }
}

enum class AwardsTestTag : CodexTestTag {
    SCREEN,

    BOW_SELECTOR,

    AGB_TABLE_NO_DATA,
    CLUB_TABLE_NO_DATA,
    AGB_TABLE_AWARD_NAME,
    AGB_TABLE_ROUND,
    AGB_TABLE_SCORE,
    CLUB_252_TABLE_AWARD_NAME,
    CLUB_252_TABLE_SCORE,
    CLUB_FROSTBITE_TABLE_AWARD_NAME,
    CLUB_FROSTBITE_TABLE_SCORE,
    ;

    override val screenName: String
        get() = "CLASSIFICATION_TABLES"

    override fun getElement(): String = name
}

@Preview(
        heightDp = 1200,
)
@Composable
fun AwardsScreen_Preview() {
    CodexTheme {
        AwardsScreen(
                AwardsState(
                        bow = ClassificationBow.RECURVE,
                        allRounds = RoundPreviewHelper.allRounds.map { it.round },
                        updateDefaultRoundsState = UpdateDefaultRoundsStatePreviewHelper.complete,
                ),
        ) {}
    }
}

@Preview
@Composable
fun NoData_AwardsScreen_Preview() {
    CodexTheme {
        AwardsScreen(
                AwardsState(
                        bow = ClassificationBow.RECURVE,
                        updateDefaultRoundsState = UpdateDefaultRoundsStatePreviewHelper.complete,
                ),
        ) {}
    }
}
