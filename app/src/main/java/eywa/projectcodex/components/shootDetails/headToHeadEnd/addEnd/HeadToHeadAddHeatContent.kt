package eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.ChipColours
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexChip
import eywa.projectcodex.common.sharedUi.CodexTextField
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.numberField.CodexLabelledNumberFieldWithErrorMessage
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsTestTag
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd.HeadToHeadAddHeatIntent.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeadToHeadAddHeatContent(
        state: HeadToHeadAddState.AddHeat,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadAddHeatIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    SimpleDialog(
            isShown = state.showSelectHeatDialog,
            onDismissListener = { listener(CloseSelectHeatDialog) },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.head_to_head_add_heat__heat),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener(CloseSelectHeatDialog) },
                ),
        ) {
            Column {
                (0..HeadToHeadUseCase.MAX_HEAT).forEach { heat ->
                    Text(
                            text = HeadToHeadUseCase.roundName(heat).get(),
                            style = CodexTypography.NORMAL,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                    .clickable { listener(SelectHeatDialogItemClicked(heat)) }
                                    .fillMaxWidth()
                                    .padding(10.dp)
                                    .testTag(HeadToHeadAddHeatTestTag.SELECTOR_DIALOG_ITEM)
                    )
                }
            }
        }
    }

    Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
    ) {
        ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
            Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
            ) {
                DataRow(
                        title = stringResource(R.string.head_to_head_add_heat__heat),
                ) {
                    val errorString = stringResource(R.string.err__required_field)
                    Text(
                            text = state.heat?.let { HeadToHeadUseCase.shortRoundName(it).get() }
                                    ?: stringResource(R.string.head_to_head_add_heat__heat_null),
                            style = LocalTextStyle.current
                                    .asClickableStyle()
                                    .copy(
                                            color = if (state.showHeatRequiredError) CodexTheme.colors.warningOnAppBackground
                                            else CodexTheme.colors.linkText,
                                    ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                    .clickable { listener(HeatClicked) }
                                    .align(Alignment.CenterVertically)
                                    .semantics {
                                        if (state.showHeatRequiredError) {
                                            error(errorString)
                                        }
                                    }
                    )
                    if (state.showHeatRequiredError) {
                        Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = CodexTheme.colors.warningOnAppBackground,
                        )
                    }
                }
                CodexChip(
                        text = stringResource(R.string.head_to_head_ref__bye),
                        selected = state.isBye,
                        testTag = HeadToHeadAddHeatTestTag.IS_BYE_CHECKBOX,
                        onToggle = { listener(ToggleIsBye) },
                        colours = ChipColours.Defaults.onPrimary(),
                )
            }
            DataRow(
                    title = stringResource(R.string.head_to_head_add_heat__opponent),
                    modifier = Modifier.padding(vertical = 10.dp)
            ) {
                CodexTextField(
                        text = state.opponent,
                        enabled = !state.isBye,
                        onValueChange = { listener(OpponentUpdated(it)) },
                        placeholderText = stringResource(R.string.head_to_head_add_heat__opponent_placeholder),
                )
            }
            CodexLabelledNumberFieldWithErrorMessage(
                    title = stringResource(R.string.head_to_head_add_heat__opponent_quali_rank),
                    enabled = !state.isBye,
                    currentValue = state.opponentQualiRank.text,
                    fieldTestTag = HeadToHeadAddHeatTestTag.OPPONENT_QUALI_RANK_INPUT,
                    errorMessageTestTag = HeadToHeadAddHeatTestTag.OPPONENT_QUALI_RANK_ERROR,
                    placeholder = stringResource(R.string.head_to_head_add_heat__quali_rank_placeholder),
                    onValueChanged = { listener(OpponentQualiRankUpdated(it)) },
            )

            CodexButton(
                    text = stringResource(R.string.head_to_head_add_heat__submit),
                    onClick = { listener(SubmitClicked) },
                    helpState = null,
                    modifier = Modifier
                            .padding(top = 8.dp)
                            .testTag(ArcherHandicapsTestTag.ADD_HANDICAP_SUBMIT)
            )
        }
    }
}

enum class HeadToHeadAddHeatTestTag : CodexTestTag {
    SCREEN,
    OPPONENT_QUALI_RANK_INPUT,
    OPPONENT_QUALI_RANK_ERROR,
    IS_BYE_CHECKBOX,
    SELECTOR_DIALOG_ITEM,
    ;

    override val screenName: String
        get() = "HEAD_TO_HEAD_ADD_HEAT"

    override fun getElement(): String = name
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun HeadToHeadAddHeatScreen_Preview() {
    CodexTheme {
        HeadToHeadAddHeatContent(
                state = HeadToHeadAddState.AddHeat(),
                modifier = Modifier.padding(10.dp)
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Bye_HeadToHeadAddHeatScreen_Preview() {
    CodexTheme {
        HeadToHeadAddHeatContent(
                state = HeadToHeadAddState.AddHeat(isBye = true, showHeatRequiredError = true),
                modifier = Modifier.padding(10.dp)
        ) {}
    }
}
