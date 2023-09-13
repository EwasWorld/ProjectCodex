package eywa.projectcodex.components.classificationTables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.CodexGrid
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundEnabledFilters
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundRows
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.classificationTables.ClassificationTableEntry
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.AgeClicked
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.AgeSelected
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.BowClicked
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.BowSelected
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.CloseDropdown
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.HelpShowcaseAction
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.SelectRoundDialogAction
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.ToggleIsGent

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

    Column(
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                    .background(CodexTheme.colors.appBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 20.dp)
                    .testTag(ClassificationTablesTestTag.SCREEN.getTestTag())
    ) {
        Text(
                text = "Beta Feature:",
                fontWeight = FontWeight.Bold,
                style = CodexTypography.LARGE,
                color = CodexTheme.colors.onAppBackground,
        )

        CategorySelectors(state, listener, Modifier.padding(bottom = 20.dp))

        Surface(
                shape = RoundedCornerShape(20),
                border = BorderStroke(1.dp, CodexTheme.colors.listItemOnAppBackground),
                color = CodexTheme.colors.appBackground,
                modifier = Modifier.padding(horizontal = 20.dp)
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

        Table(state.scores, helpListener)
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.updateHelpDialogPosition(
                    HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_classification_tables__categories_title),
                            helpBody = stringResource(R.string.help_classification_tables__categories_body),
                    ),
            )
    ) {
        ProvideTextStyle(value = CodexTypography.NORMAL.copy(CodexTheme.colors.onAppBackground)) {
            DataRow(
                    title = stringResource(R.string.classification_tables__gender_title),
                    text = stringResource(
                            if (state.isGent) R.string.classification_tables__gender_male
                            else R.string.classification_tables__gender_female
                    ),
                    helpState = null,
                    onClick = { listener(ToggleIsGent) },
                    accessibilityRole = Role.Switch,
                    modifier = Modifier
                            .padding(vertical = 7.dp)
                            .testTag(ClassificationTablesTestTag.GENDER_SELECTOR.getTestTag())
            )
            Input(
                    label = stringResource(R.string.classification_tables__age_title),
                    currentValue = state.age.rawName,
                    values = ClassificationAge.values().map { it.rawName },
                    testTag = ClassificationTablesTestTag.AGE_SELECTOR,
                    onClick = { listener(AgeClicked) },
                    onItemClick = { listener(AgeSelected(ClassificationAge.values()[it])) },
                    onDismiss = { listener(CloseDropdown) },
                    expanded = state.expanded == ClassificationTablesState.Dropdown.AGE,
            )
            Input(
                    label = stringResource(R.string.classification_tables__bow_title),
                    currentValue = state.bow.rawName,
                    values = ClassificationBow.values().map { it.rawName },
                    testTag = ClassificationTablesTestTag.BOW_SELECTOR,
                    onClick = { listener(BowClicked) },
                    onItemClick = { listener(BowSelected(ClassificationBow.values()[it])) },
                    onDismiss = { listener(CloseDropdown) },
                    expanded = state.expanded == ClassificationTablesState.Dropdown.BOW,
                    modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun Input(
        label: String,
        currentValue: String,
        expanded: Boolean,
        values: List<String>,
        testTag: ClassificationTablesTestTag,
        onClick: () -> Unit,
        onItemClick: (Int) -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier,
) {
    DataRow(
            title = label,
            helpState = null,
            modifier = modifier.testTag(testTag.getTestTag())
    ) {
        Text(
                text = currentValue,
                color = CodexTheme.colors.linkText,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onClick() }
        )
    }
    SimpleDialog(
            isShown = expanded,
            onDismissListener = onDismiss,
    ) {
        SimpleDialogContent(
                title = label,
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = onDismiss,
                )
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
                                    .testTag(ClassificationTablesTestTag.SELECTOR_DIALOG_ITEM.getTestTag())
                    )
                }
            }
        }
    }
}

@Composable
private fun Table(
        entries: List<ClassificationTableEntry>,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    ProvideTextStyle(value = CodexTypography.NORMAL.copy(CodexTheme.colors.onListItemAppOnBackground)) {
        Surface(
                shape = RoundedCornerShape(10),
                color = CodexTheme.colors.listItemOnAppBackground,
                modifier = Modifier
                        .updateHelpDialogPosition(
                                HelpState(
                                        helpListener = helpListener,
                                        helpTitle = stringResource(R.string.help_classification_tables__table_title),
                                        helpBody = stringResource(R.string.help_classification_tables__table_body),
                                )
                        )
                        .horizontalScroll(rememberScrollState())
                        .padding(20.dp)
        ) {
            if (entries.isEmpty()) {
                Text(
                        text = stringResource(R.string.classification_tables__no_tables),
                        modifier = Modifier.testTag(ClassificationTablesTestTag.TABLE_NO_DATA.getTestTag())
                )
            }
            else {
                val resources = LocalContext.current.resources

                CodexGrid(
                        columns = 3,
                        alignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                ) {

                    listOf(
                            R.string.classification_tables__classification_header,
                            R.string.classification_tables__score_field,
                            R.string.classification_tables__handicap_field,
                    ).forEach {
                        item {
                            Text(
                                    text = stringResource(it),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                    entries.forEach { entry ->
                        listOf(
                                resources.getString(entry.classification.shortStringId) to ClassificationTablesTestTag.TABLE_CLASSIFICATION,
                                entry.score.toString() to ClassificationTablesTestTag.TABLE_SCORE,
                                (entry.handicap?.toString() ?: "-") to ClassificationTablesTestTag.TABLE_HANDICAP,
                        ).forEach { (text, testTag) ->
                            item {
                                Text(
                                        text = text,
                                        modifier = Modifier
                                                .padding(3.dp)
                                                .testTag(testTag.getTestTag())
                                )
                            }
                        }
                    }
                }
            }
        }
    }
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
fun PreviewClassificationTablesScreen() {
    ClassificationTablesScreen(
            ClassificationTablesState(
                    selectRoundDialogState = SelectRoundDialogState(
                            selectedRoundId = RoundPreviewHelper.outdoorImperialRoundData.round.roundId,
                            filters = SelectRoundEnabledFilters(),
                            allRounds = listOf(RoundPreviewHelper.outdoorImperialRoundData),
                            isRoundDialogOpen = false,
                            isSubtypeDialogOpen = false,
                    ),
                    scores = listOf(
                            "1,Women,Recurve,Senior,York,211",
                            "2,Women,Recurve,Senior,York,309",
                            "3,Women,Recurve,Senior,York,432",
                            "4,Women,Recurve,Senior,York,576",
                            "5,Women,Recurve,Senior,York,726",
                            "6,Women,Recurve,Senior,York,868",
                            "7,Women,Recurve,Senior,York,989",
                            "8,Women,Recurve,Senior,York,1086",
                            "9,Women,Recurve,Senior,York,1162",
                    ).mapNotNull { ClassificationTableEntry.fromString(it) },
            )
    ) {}
}
