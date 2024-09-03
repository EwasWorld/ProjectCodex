package eywa.projectcodex.components.referenceTables.handicapTables.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.numberField.CodexNumberField
import eywa.projectcodex.common.sharedUi.numberField.CodexNumberFieldErrorText
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.components.referenceTables.handicapTables.HandicapTablesIntent
import eywa.projectcodex.components.referenceTables.handicapTables.HandicapTablesState

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Selections(
        state: HandicapTablesState,
        listener: (HandicapTablesIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HandicapTablesIntent.HelpShowcaseAction(it)) }

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
            modifier = Modifier
                    .padding(vertical = 2.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = CodexTheme.dimens.screenPadding)
    ) {
        DataRow(
                title = stringResource(R.string.handicap_tables__handicap_system_title),
                text = stringResource(
                        if (state.use2023System) R.string.handicap_tables__handicap_system_agb_2023
                        else R.string.handicap_tables__handicap_system_david_lane,
                ),
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_handicap_tables__2023_system_title),
                        helpBody = stringResource(R.string.help_handicap_tables__2023_system_body),
                ).asHelpState(helpListener),
                onClick = { listener(HandicapTablesIntent.ToggleHandicapSystem) },
                accessibilityRole = Role.Switch,
                modifier = Modifier
                        .testTag(HandicapTablesTestTag.SYSTEM_SELECTOR)
                        .padding(bottom = 10.dp)
        )

        FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            Text(
                    text = stringResource(state.inputType.labelId),
                    style = LocalTextStyle.current.asClickableStyle(),
                    modifier = Modifier
                            .testTag(HandicapTablesTestTag.INPUT_SELECTOR)
                            .clickable(
                                    onClickLabel = stringResource(R.string.handicap_tables__input_selector_click_label),
                                    onClick = { listener(HandicapTablesIntent.ToggleInput) },
                                    role = Role.Switch,
                            )
                            .updateHelpDialogPosition(
                                    HelpShowcaseItem(
                                            helpTitle = stringResource(R.string.help_handicap_tables__input_type_title),
                                            helpBody = stringResource(state.inputType.typeHelpId),
                                    ).asHelpState(helpListener),
                            )
                            .align(Alignment.CenterVertically)
            )
            CodexNumberField(
                    currentValue = state.inputFull.text,
                    placeholder = "50",
                    contentDescription = stringResource(state.inputType.labelId),
                    errorMessage = state.inputFull.error,
                    testTag = HandicapTablesTestTag.INPUT_TEXT,
                    onValueChanged = { listener(HandicapTablesIntent.InputChanged(it)) },
                    modifier = Modifier
                            .updateHelpDialogPosition(
                                    HelpShowcaseItem(
                                            helpTitle = stringResource(R.string.help_handicap_tables__input_title),
                                            helpBody = stringResource(state.inputType.inputHelpId),
                                    ).asHelpState(helpListener),
                            )
                            .align(Alignment.CenterVertically)
            )
        }
        CodexNumberFieldErrorText(
                errorText = state.inputFull.error,
                testTag = HandicapTablesTestTag.INPUT_ERROR,
        )
        DataRow(
                title = stringResource(R.string.handicap_tables__is_compound_title),
                text = stringResource(
                        if (state.isCompound) R.string.general_on
                        else R.string.general_off,
                ),
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_handicap_tables__is_compound_title),
                        helpBody = stringResource(R.string.help_handicap_tables__is_compound_body),
                ).asHelpState(helpListener),
                onClick = { listener(HandicapTablesIntent.ToggleIsCompound) },
                accessibilityRole = Role.Switch,
                modifier = Modifier
                        .testTag(HandicapTablesTestTag.COMPOUND_SELECTOR)
                        .padding(vertical = 10.dp)
        )
    }
}
