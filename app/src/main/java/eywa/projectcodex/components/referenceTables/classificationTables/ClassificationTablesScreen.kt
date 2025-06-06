package eywa.projectcodex.components.referenceTables.classificationTables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIf
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.grid.CodexGrid
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.RoundsUpdatingWrapper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundEnabledFilters
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundRows
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.classificationTables.ClassificationTableEntry
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsStatePreviewHelper
import eywa.projectcodex.components.referenceTables.classificationTables.ClassificationTablesIntent.*
import eywa.projectcodex.database.rounds.RoundRepo

@Composable
fun ClassificationTablesScreen(
        viewModel: ClassificationTablesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    ClassificationTablesScreen(state) { viewModel.handle(it) }
}

@Composable
fun ClassificationTablesScreen(
        state: ClassificationTablesState,
        listener: (ClassificationTablesIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        Column(
                verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .fillMaxSize()
                        .background(CodexTheme.colors.appBackground)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = CodexTheme.dimens.screenPadding)
                        .testTag(ClassificationTablesTestTag.SCREEN)
        ) {
            CategorySelectors(state, listener, Modifier.padding(bottom = 4.dp))

            Surface(
                    shape = RoundedCornerShape(
                            if (!state.updateDefaultRoundsState.hasTaskFinished) CodexTheme.dimens.cornerRounding
                            else if (state.selectRoundDialogState.selectedRound == null) CodexTheme.dimens.smallCornerRounding
                            else CodexTheme.dimens.cornerRounding,
                    ),
                    border = BorderStroke(1.dp, CodexTheme.colors.listItemOnAppBackground),
                    color = CodexTheme.colors.appBackground,
                    modifier = Modifier.padding(horizontal = CodexTheme.dimens.screenPadding, vertical = 8.dp)
            ) {
                RoundsUpdatingWrapper(
                        state = state.updateDefaultRoundsState,
                        warningModifier = Modifier.padding(10.dp),
                ) {
                    Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        SelectRoundRows(
                                state = state.selectRoundDialogState,
                                helpListener = helpListener,
                                listener = { listener(SelectRoundDialogAction(it)) },
                        )
                    }
                }
            }

            if (state.selectRoundDialogState.selectedRound?.round?.defaultRoundId == RoundRepo.VEGAS_DEFAULT_ROUND_ID) {
                Text(
                        text = stringResource(R.string.classification_tables__vegas_note),
                        style = CodexTypography.SMALL_PLUS,
                        color = CodexTheme.colors.onAppBackground,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier
                                .padding(horizontal = CodexTheme.dimens.screenPadding)
                                .padding(top = 10.dp)
                )
            }

            Table(state.scores, helpListener)
        }
    }
}

@Composable
private fun CategorySelectors(
        state: ClassificationTablesState,
        listener: (ClassificationTablesIntent) -> Unit,
        modifier: Modifier = Modifier,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.updateHelpDialogPosition(
                    HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_classification_tables__categories_title),
                            helpBody = stringResource(R.string.help_classification_tables__categories_body),
                    ).asHelpState(helpListener),
            )
    ) {
        DataRow(
                title = stringResource(R.string.classification_tables__gender_title),
                text = stringResource(
                        if (state.isGent) R.string.classification_tables__gender_male
                        else R.string.classification_tables__gender_female,
                ),
                helpState = null,
                onClick = { listener(ToggleIsGent) },
                accessibilityRole = Role.Switch,
                modifier = Modifier
                        .padding(vertical = 7.dp)
                        .testTag(ClassificationTablesTestTag.GENDER_SELECTOR)
        )
        CategoryInput(
                label = stringResource(R.string.classification_tables__age_title),
                currentValue = state.age.rawName,
                values = ClassificationAge.entries.map { it.rawName },
                testTag = ClassificationTablesTestTag.AGE_SELECTOR,
                onClick = { listener(AgeClicked) },
                onItemClick = { listener(AgeSelected(ClassificationAge.entries[it])) },
                onDismiss = { listener(CloseDropdown) },
                expanded = state.expanded == ClassificationTablesState.Dropdown.AGE,
        )
        CategoryInput(
                label = stringResource(R.string.classification_tables__bow_title),
                currentValue = state.bow.rawName,
                values = ClassificationBow.entries.map { it.rawName },
                testTag = ClassificationTablesTestTag.BOW_SELECTOR,
                onClick = { listener(BowClicked) },
                onItemClick = { listener(BowSelected(ClassificationBow.entries[it])) },
                onDismiss = { listener(CloseDropdown) },
                expanded = state.expanded == ClassificationTablesState.Dropdown.BOW,
                modifier = Modifier.padding(top = 5.dp)
        )
    }
}

@Composable
fun CategoryInput(
        label: String,
        currentValue: String,
        expanded: Boolean,
        values: List<String>,
        testTag: CodexTestTag,
        onClick: () -> Unit,
        onItemClick: (Int) -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier,
) {
    DataRow(
            title = label,
            text = currentValue,
            helpState = null,
            onClick = onClick,
            modifier = modifier
                    .padding(vertical = 7.dp)
                    .testTag(testTag)
    )
    SimpleDialog(
            isShown = expanded,
            onDismissListener = onDismiss,
    ) {
        SimpleDialogContent(
                title = label,
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = onDismiss,
                ),
        ) {
            Column {
                values.forEachIndexed { index, value ->
                    Text(
                            text = value,
                            style = CodexTypography.NORMAL,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                    .clickable { onItemClick(index) }
                                    .fillMaxWidth()
                                    .padding(10.dp)
                                    .testTag(ClassificationTablesTestTag.SELECTOR_DIALOG_ITEM)
                    )
                }
            }
        }
    }
}

@Composable
private fun Table(
        entries: List<Pair<ClassificationTableEntry, Boolean>>,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    ProvideTextStyle(value = CodexTypography.NORMAL.copy(CodexTheme.colors.onListItemAppOnBackground)) {
        Surface(
                shape = RoundedCornerShape(
                        if (entries.isEmpty()) CodexTheme.dimens.smallCornerRounding
                        else CodexTheme.dimens.cornerRounding,
                ),
                color = CodexTheme.colors.listItemOnAppBackground,
                modifier = Modifier
                        .updateHelpDialogPosition(
                                HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_classification_tables__table_title),
                                        helpBody = stringResource(R.string.help_classification_tables__table_body),
                                ).asHelpState(helpListener),
                        )
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = CodexTheme.dimens.screenPadding)
                        .padding(top = 20.dp)
        ) {
            if (entries.isEmpty()) {
                Text(
                        text = stringResource(R.string.classification_tables__no_tables),
                        modifier = Modifier
                                .testTag(ClassificationTablesTestTag.TABLE_NO_DATA)
                                .padding(10.dp)
                )
            }
            else {
                val resources = LocalContext.current.resources

                CodexGrid(
                        columns = 3,
                        alignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = CodexTheme.dimens.screenPadding, vertical = 10.dp)
                ) {

                    ClassificationTableColumn.entries.forEach {
                        item {
                            Text(
                                    text = it.header.get(),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                    entries.forEach { (entry, isActual) ->
                        ClassificationTableColumn.entries.forEach {
                            item {
                                Text(
                                        text = it.data(entry).get(),
                                        modifier = Modifier
                                                .padding(3.dp)
                                                .testTag(it.tableCellTestTag)
                                                .modifierIf(!isActual, Modifier.alpha(0.4f))
                                                .semantics {
                                                    contentDescription = it
                                                            .semanticData(entry, isActual)
                                                            .get(resources)
                                                }
                                )
                            }
                        }
                    }
                }
            }
        }
        Text(
                text = stringResource(R.string.classification_tables__disclaimer),
                style = CodexTypography.SMALL,
                color = CodexTheme.colors.onAppBackground,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
        )
    }
}

enum class ClassificationTableColumn(
        val header: ResOrActual<String>,
        val tableCellTestTag: ClassificationTablesTestTag,
        val data: (ClassificationTableEntry) -> ResOrActual<String>,
        val semanticData: (ClassificationTableEntry, isActual: Boolean) -> ResOrActual<String>,
) {
    CLASSIFICATION_NAME(
            header = ResOrActual.StringResource(R.string.classification_tables__classification_header),
            tableCellTestTag = ClassificationTablesTestTag.TABLE_CLASSIFICATION,
            data = { it.classification.shortStringId },
            semanticData = { it, _ -> it.classification.shortStringId },
    ),
    SCORE(
            header = ResOrActual.StringResource(R.string.classification_tables__score_field),
            tableCellTestTag = ClassificationTablesTestTag.TABLE_SCORE,
            data = { ResOrActual.Actual(it.score?.toString() ?: "-") },
            semanticData = { it, isActual ->
                if (it.score == null) {
                    ResOrActual.StringResource(R.string.classification_tables__score_field_empty_semantics)
                }
                else {
                    ResOrActual.StringResource(
                            R.string.classification_tables__score_field_semantics,
                            listOfNotNull(
                                    it.score,
                                    ResOrActual.StringResource(R.string.classification_tables__unofficial_semantics)
                                            .takeIf { !isActual } ?: "",
                            )
                    )
                }
            }
    ),
    HANDICAP(
            header = ResOrActual.StringResource(R.string.classification_tables__handicap_field),
            tableCellTestTag = ClassificationTablesTestTag.TABLE_HANDICAP,
            data = { ResOrActual.Actual(it.handicap?.toString() ?: "-") },
            semanticData = { it, isActual ->

                ResOrActual.StringResource(
                        R.string.classification_tables__handicap_field_semantics,
                        listOf(
                                it.handicap?.toString() ?: "-",
                                ResOrActual.StringResource(R.string.classification_tables__unofficial_semantics)
                                        .takeIf { !isActual } ?: "",
                        ),
                )
            }
    ),
}

enum class ClassificationTablesTestTag : CodexTestTag {
    SCREEN,

    GENDER_SELECTOR,
    AGE_SELECTOR,
    BOW_SELECTOR,
    SELECTOR_DIALOG_ITEM,

    TABLE_NO_DATA,
    TABLE_CLASSIFICATION,
    TABLE_SCORE,
    TABLE_HANDICAP,
    ;

    override val screenName: String
        get() = "CLASSIFICATION_TABLES"

    override fun getElement(): String = name
}

@Preview
@Composable
fun ClassificationTablesScreen_Preview() {
    ClassificationTablesScreen(
            ClassificationTablesState(
                    selectRoundDialogState = SelectRoundDialogState(
                            selectedRoundId = RoundPreviewHelper.outdoorImperialRoundData.round.roundId,
                            filters = SelectRoundEnabledFilters(),
                            allRounds = listOf(RoundPreviewHelper.outdoorImperialRoundData),
                    ),
                    officialClassifications = listOf(
                            "1,Women,Recurve,Senior,York,211",
                            "2,Women,Recurve,Senior,York,309",
                            "3,Women,Recurve,Senior,York,432",
                            "4,Women,Recurve,Senior,York,576",
                            "5,Women,Recurve,Senior,York,726",
                            "6,Women,Recurve,Senior,York,868",
                            "7,Women,Recurve,Senior,York,989",
                    ).mapNotNull { ClassificationTableEntry.fromString(it) },
                    roughHandicaps = listOf(
                            "1,Women,Recurve,Senior,WA 1440 (90m),337",
                            "2,Women,Recurve,Senior,WA 1440 (90m),464",
                            "3,Women,Recurve,Senior,WA 1440 (90m),609",
                            "4,Women,Recurve,Senior,WA 1440 (90m),761",
                            "5,Women,Recurve,Senior,WA 1440 (90m),907",
                            "6,Women,Recurve,Senior,WA 1440 (90m),1033",
                            "7,Women,Recurve,Senior,WA 1440 (90m),1137",
                            "8,Women,Recurve,Senior,WA 1440 (90m),1086",
                            "9,Women,Recurve,Senior,WA 1440 (90m),1162",
                    ).mapNotNull { ClassificationTableEntry.fromString(it) },
                    updateDefaultRoundsState = UpdateDefaultRoundsStatePreviewHelper.complete,
            ),
    ) {}
}

@Preview
@Composable
fun Empty_ClassificationTablesScreen_Preview() {
    ClassificationTablesScreen(
            ClassificationTablesState(
                    selectRoundDialogState = SelectRoundDialogState(
                            filters = SelectRoundEnabledFilters(),
                            allRounds = listOf(RoundPreviewHelper.outdoorImperialRoundData),
                    ),
                    officialClassifications = emptyList(),
                    roughHandicaps = listOf(
                            "1,Women,Recurve,Senior,WA 1440 (90m),337",
                            "2,Women,Recurve,Senior,WA 1440 (90m),464",
                            "3,Women,Recurve,Senior,WA 1440 (90m),609",
                            "4,Women,Recurve,Senior,WA 1440 (90m),761",
                            "5,Women,Recurve,Senior,WA 1440 (90m),907",
                            "6,Women,Recurve,Senior,WA 1440 (90m),1033",
                            "7,Women,Recurve,Senior,WA 1440 (90m),1137",
                            "8,Women,Recurve,Senior,WA 1440 (90m),1219",
                            "9,Women,Recurve,Senior,WA 1440 (90m),1283",
                    ).mapNotNull { ClassificationTableEntry.fromString(it)?.copy(score = null, handicap = 55) },
                    updateDefaultRoundsState = UpdateDefaultRoundsStatePreviewHelper.complete,
            ),
    ) {}
}

@Preview
@Composable
fun Vegas_ClassificationTablesScreen_Preview() {
    ClassificationTablesScreen(
            ClassificationTablesState(
                    selectRoundDialogState = SelectRoundDialogState(
                            selectedRoundId = RoundPreviewHelper.vegasRoundData.round.roundId,
                            filters = SelectRoundEnabledFilters(),
                            allRounds = listOf(RoundPreviewHelper.vegasRoundData),
                    ),
                    officialClassifications = listOf(
                            "1,Women,Recurve,Senior,Vegas (Triple Face),137",
                            "2,Women,Recurve,Senior,Vegas (Triple Face),206",
                            "3,Women,Recurve,Senior,Vegas (Triple Face),294",
                            "4,Women,Recurve,Senior,Vegas (Triple Face),387",
                            "5,Women,Recurve,Senior,Vegas (Triple Face),465",
                            "6,Women,Recurve,Senior,Vegas (Triple Face),515",
                            "7,Women,Recurve,Senior,Vegas (Triple Face),545",
                            "8,Women,Recurve,Senior,Vegas (Triple Face),567",
                    ).mapNotNull { ClassificationTableEntry.fromString(it) },
                    roughHandicaps = listOf(),
                    updateDefaultRoundsState = UpdateDefaultRoundsStatePreviewHelper.complete,
            ),
    ) {}
}
