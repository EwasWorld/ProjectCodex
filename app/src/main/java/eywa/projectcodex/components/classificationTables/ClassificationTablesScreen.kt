package eywa.projectcodex.components.classificationTables

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundEnabledFilters
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundRows
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.classificationTables.ClassificationTableEntry
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.*

class ClassificationTablesScreen : ActionBarHelp {
    @Composable
    fun ComposeContent(
            state: ClassificationTablesState,
            listener: (ClassificationTablesIntent) -> Unit,
    ) {
        val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

        Column(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .fillMaxSize()
                        .background(CodexTheme.colors.appBackground)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 20.dp)
                        .testTag(TestTag.SCREEN.getTestTag())
        ) {
            Text(
                    text = "Beta Feature:",
                    fontWeight = FontWeight.Bold,
                    style = CodexTypography.LARGE,
                    color = CodexTheme.colors.onAppBackground,
            )

            ProvideTextStyle(value = CodexTypography.NORMAL.copy(CodexTheme.colors.onAppBackground)) {
                CodexCheckbox(text = "Gents:", checked = state.isGent, onToggle = { listener(ToggleIsGent) })
                Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Input(
                            label = "Age:",
                            currentValue = state.age.rawName,
                            values = ClassificationAge.values().map { it.rawName },
                            onClick = { listener(AgeClicked) },
                            onItemClick = { listener(AgeSelected(ClassificationAge.values()[it])) },
                            onDismiss = { listener(CloseDropdown) },
                            expanded = state.expanded == ClassificationTablesState.Dropdown.AGE,
                            helpState = null,
                    )
                    Input(
                            label = "Bow:",
                            currentValue = state.bow.rawName,
                            values = ClassificationBow.values().map { it.rawName },
                            onClick = { listener(BowClicked) },
                            onItemClick = { listener(BowSelected(ClassificationBow.values()[it])) },
                            onDismiss = { listener(CloseDropdown) },
                            expanded = state.expanded == ClassificationTablesState.Dropdown.BOW,
                            helpState = null,
                    )
                }
            }

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
                            isSelectRoundDialogOpen = state.isSelectRoundDialogOpen,
                            isSelectSubtypeDialogOpen = state.isSelectSubtypeDialogOpen,
                            selectedRound = state.round,
                            selectedSubtypeId = state.subType ?: 1,
                            rounds = state.allRounds,
                            filters = state.roundFilters,
                            helpListener = helpListener,
                            listener = { listener(SelectRoundDialogAction(it)) },
                    )
                }
            }

            Table(state.scores)
        }
    }

    @Composable
    private fun Input(
            label: String,
            currentValue: String,
            expanded: Boolean,
            values: List<String>,
            onClick: () -> Unit,
            onItemClick: (Int) -> Unit,
            onDismiss: () -> Unit,
            helpState: HelpState?,
    ) {
        DataRow(
                title = label,
                helpState = helpState,
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
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun Table(
            entries: List<ClassificationTableEntry>,
    ) {
        ProvideTextStyle(value = CodexTypography.NORMAL) {
            Surface(
                    shape = RoundedCornerShape(10),
                    color = CodexTheme.colors.listItemOnAppBackground,
                    modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(20.dp)
            ) {
                Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    if (entries.isNotEmpty()) {
                        Row(
                                horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterHorizontally),
                        ) {
                            TableColumn(
                                    heading = "Classification",
                                    rowCount = entries.size,
                                    content = { stringResource(entries[it].classification.shortStringId) }
                            )
                            TableColumn(
                                    heading = stringResource(R.string.classification_tables__score_field),
                                    rowCount = entries.size,
                                    content = { entries[it].score.toString() }
                            )
                            TableColumn(
                                    heading = stringResource(R.string.classification_tables__handicap_field),
                                    rowCount = entries.size,
                                    content = { entries[it].handicap?.toString() ?: "-" }
                            )
                        }
                    }
                    else {
                        Text(
                                text = stringResource(R.string.classification_tables__no_tables),
                                modifier = Modifier
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun TableColumn(
            heading: String,
            rowCount: Int,
            content: @Composable (index: Int) -> String,
    ) {
        Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                    text = heading,
                    fontWeight = FontWeight.Bold,
            )
            repeat(rowCount) {
                Text(text = content(it))
            }
        }
    }

    enum class TestTag : CodexTestTag {
        SCREEN,
        ;

        override val screenName: String
            get() = "CLASSIFICATION_TABLES"

        override fun getElement(): String = name
    }

    @Preview
    @Composable
    fun PreviewClassificationTablesScreen() {
        ComposeContent(
                ClassificationTablesState(
                        round = RoundPreviewHelper.outdoorImperialRoundData,
                        roundFilters = SelectRoundEnabledFilters(),
                        subType = 1,
                        allRounds = listOf(RoundPreviewHelper.outdoorImperialRoundData),
                        isSelectRoundDialogOpen = false,
                        isSelectSubtypeDialogOpen = false,
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
}
