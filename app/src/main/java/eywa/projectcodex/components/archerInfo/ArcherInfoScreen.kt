package eywa.projectcodex.components.archerInfo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.database.archer.getGenderString

@Composable
fun ArcherInfoScreen(
        navController: NavController,
        viewModel: ArcherInfoViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    ArcherInfoScreen(state) { viewModel.handle(it) }
}

@Composable
fun ArcherInfoScreen(
        state: ArcherInfoState,
        listener: (ArcherInfoIntent) -> Unit,
) {
    Column(
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                    .fillMaxSize()
                    .background(CodexTheme.colors.appBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 20.dp)
                    .testTag(ArcherInfoTestTag.SCREEN.getTestTag())
    ) {
        CategorySelectors(state, listener)
    }
}

@Composable
private fun CategorySelectors(
        state: ArcherInfoState,
        listener: (ArcherInfoIntent) -> Unit,
        modifier: Modifier = Modifier,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(ArcherInfoIntent.HelpShowcaseAction(it)) }

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
                    text = getGenderString(state.isGent).get(),
                    helpState = null,
                    onClick = { listener(ArcherInfoIntent.SetIsGent(!state.isGent)) },
                    accessibilityRole = Role.Switch,
                    modifier = Modifier
                            .padding(vertical = 7.dp)
                            .testTag(ArcherInfoTestTag.GENDER_SELECTOR.getTestTag())
            )
            Input(
                    label = stringResource(R.string.archer_info__age_dialog_title),
                    currentValue = state.age.rawName,
                    values = ClassificationAge.values().map { it.rawName },
                    testTag = ArcherInfoTestTag.AGE_SELECTOR,
                    onClick = { listener(ArcherInfoIntent.AgeClicked) },
                    onItemClick = { listener(ArcherInfoIntent.AgeSelected(ClassificationAge.values()[it])) },
                    onDismiss = { listener(ArcherInfoIntent.CloseDropdown) },
                    expanded = state.expanded == ArcherInfoState.Dropdown.AGE,
            )
            Input(
                    label = stringResource(R.string.archer_info__bow_dialog_title),
                    currentValue = state.bow.rawName,
                    values = ClassificationBow.values().map { it.rawName },
                    testTag = ArcherInfoTestTag.BOW_SELECTOR,
                    onClick = { listener(ArcherInfoIntent.BowClicked) },
                    onItemClick = { listener(ArcherInfoIntent.BowSelected(ClassificationBow.values()[it])) },
                    onDismiss = { listener(ArcherInfoIntent.CloseDropdown) },
                    expanded = state.expanded == ArcherInfoState.Dropdown.BOW,
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
        testTag: ArcherInfoTestTag,
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
            modifier = modifier.testTag(testTag.getTestTag())
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
                                    .testTag(ArcherInfoTestTag.SELECTOR_DIALOG_ITEM.getTestTag())
                    )
                }
            }
        }
    }
}

enum class ArcherInfoTestTag : CodexTestTag {
    SCREEN,

    GENDER_SELECTOR,
    AGE_SELECTOR,
    BOW_SELECTOR,
    SELECTOR_DIALOG_ITEM,
    ;

    override val screenName: String
        get() = "ARCHER_INFO"

    override fun getElement(): String = name
}

@Preview
@Composable
fun ArcherInfoScreen_Preview() {
    ArcherInfoScreen(
            ArcherInfoState()
    ) {}
}
