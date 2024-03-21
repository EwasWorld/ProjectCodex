package eywa.projectcodex.components.archerHandicaps.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.navigation.BottomSheetNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexButtonDefaults
import eywa.projectcodex.common.sharedUi.CodexDateTimeSelectorRow
import eywa.projectcodex.common.sharedUi.CodexTextField
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.numberField.CodexLabelledNumberField
import eywa.projectcodex.common.sharedUi.numberField.CodexNumberFieldErrorText
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsTestTag
import eywa.projectcodex.components.archerHandicaps.add.ArcherHandicapsAddIntent.DateChanged
import eywa.projectcodex.components.archerHandicaps.add.ArcherHandicapsAddIntent.HandicapTextUpdated
import eywa.projectcodex.components.archerHandicaps.add.ArcherHandicapsAddIntent.SubmitPressed

object ArcherHandicapsBottomSheetAdd : BottomSheetNavRoute {
    override val sheetRouteBase = "archer_handicap_add"

    override val args: Map<NavArgument, Boolean> = emptyMap()

    @Composable override fun getMenuBarTitle(entry: NavBackStackEntry?): String =
            stringResource(R.string.archer_info__title)

    @Composable
    override fun ColumnScope.SheetContent(navController: NavController) {
        val viewModel: ArcherHandicapsAddViewModel = hiltViewModel()

        val state by viewModel.state.collectAsState()
        AddHandicapBottomSheetContent(
                state = state,
                listener = { viewModel.handle(it) },
        )

        LaunchedEffect(state.shouldCloseDialog) {
            if (state.shouldCloseDialog) {
                navController.popBackStack()
                viewModel.handle(ArcherHandicapsAddIntent.CloseHandled)
            }
        }
    }
}

@Composable
private fun AddHandicapBottomSheetContent(
        state: ArcherHandicapsAddState,
        listener: (ArcherHandicapsAddIntent) -> Unit,
) {
    ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onDialogBackground)) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = CodexTheme.dimens.cornerRounding)
        ) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                CodexDateTimeSelectorRow(
                        date = state.date,
                        updateDateListener = { listener(DateChanged(it)) },
                        helpState = null,
                        modifier = Modifier.padding(vertical = 3.dp)
                )
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CodexLabelledNumberField(
                            title = stringResource(R.string.archer_handicaps__handicap_header),
                            currentValue = state.handicap.text,
                            testTag = ArcherHandicapsTestTag.ADD_HANDICAP_VALUE,
                            placeholder = "75",
                            errorMessage = state.handicap.error,
                            onValueChanged = { listener(HandicapTextUpdated(it)) },
                            colors = CodexTextField.transparentOutlinedTextFieldColorsOnDialog(),
                            helpState = null,
                    )
                    CodexNumberFieldErrorText(
                            errorText = state.handicap.error,
                            testTag = ArcherHandicapsTestTag.ADD_HANDICAP_ERROR_TEXT,
                            modifier = Modifier.padding(top = 3.dp)
                    )
                }
                CodexButton(
                        text = stringResource(R.string.archer_handicaps__add_submit),
                        buttonStyle = CodexButtonDefaults.DefaultOutlinedButton,
                        onClick = { listener(SubmitPressed) },
                        helpState = null,
                        modifier = Modifier
                                .padding(top = 8.dp, bottom = 20.dp)
                                .testTag(ArcherHandicapsTestTag.ADD_HANDICAP_SUBMIT)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddHandicapBottomSheetContent_Preview(
) {
    CodexTheme {
        AddHandicapBottomSheetContent(
                ArcherHandicapsAddState()
        ) {}
    }
}
