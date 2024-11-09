package eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.DEFAULT_INT_NAV_ARG
import eywa.projectcodex.common.navigation.NavArgument
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
import eywa.projectcodex.components.shootDetails.addEnd.SightMark
import eywa.projectcodex.components.shootDetails.commonUi.HandleMainEffects
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsMainScreen
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat.HeadToHeadAddHeatIntent.*

@Composable
fun HeadToHeadAddHeatScreen(
        navController: NavController,
        viewModel: HeadToHeadAddHeatViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: HeadToHeadAddHeatIntent -> viewModel.handle(it) }

    ShootDetailsMainScreen(
            currentScreen = CodexNavRoute.HEAD_TO_HEAD_ADD_HEAT,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    ) { it, modifier -> HeadToHeadAddHeatScreen(it, modifier, listener) }

    HandleMainEffects(
            navController = navController,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    )

    val data = state.getData()
    LaunchedEffect(
            data?.extras?.openAllSightMarks,
            data?.extras?.openEditSightMark,
            data?.extras?.openAddEndScreen,
    ) {
        if (data != null) {
            if (data.extras.openAllSightMarks) {
                CodexNavRoute.SIGHT_MARKS.navigate(navController)
                listener(ExpandSightMarkHandled)
            }

            if (data.extras.openEditSightMark) {
                val args = if (data.headToHeadRoundInfo?.sightMark != null) {
                    mapOf(NavArgument.SIGHT_MARK_ID to data.headToHeadRoundInfo.sightMark.id.toString())
                }
                else {
                    val distance = data.headToHeadRoundInfo?.distance ?: DEFAULT_INT_NAV_ARG
                    val isMetric = data.headToHeadRoundInfo?.isMetric ?: true
                    mapOf(NavArgument.DISTANCE to distance.toString(), NavArgument.IS_METRIC to isMetric.toString())
                }
                CodexNavRoute.SIGHT_MARK_DETAIL.navigate(navController, args)
                listener(EditSightMarkHandled)
            }

            if (data.extras.openAddEndScreen) {
                CodexNavRoute.HEAD_TO_HEAD_ADD_END.navigate(
                        navController,
                        mapOf(NavArgument.SHOOT_ID to viewModel.shootId.toString()),
                        popCurrentRoute = true,
                )
                listener(OpenAddEndScreenHandled)
            }
        }
    }
}

@Composable
fun HeadToHeadAddHeatScreen(
        state: HeadToHeadAddHeatState,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadAddHeatIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    Column(
            verticalArrangement = Arrangement.spacedBy(40.dp, alignment = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                    .background(CodexTheme.colors.appBackground)
                    .padding(vertical = CodexTheme.dimens.screenPadding)
    ) {
        if (state.headToHeadRoundInfo != null) {
            SightMark(
                    distance = state.headToHeadRoundInfo.distance,
                    isMetric = state.headToHeadRoundInfo.isMetric,
                    sightMark = state.headToHeadRoundInfo.sightMark,
                    helpListener = helpListener,
                    onExpandClicked = { listener(ExpandSightMarkClicked) },
                    onEditClicked = { listener(EditSightMarkClicked) },
            )
        }

        if (state.previousHeat != null) {
            Surface(
                    border = BorderStroke(1.dp, CodexTheme.colors.listItemOnAppBackground),
                    color = CodexTheme.colors.appBackground,
                    modifier = Modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
            ) {
                ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
                    Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp, alignment = Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 20.dp, horizontal = 25.dp)
                    ) {
                        DataRow(
                                title = stringResource(R.string.head_to_head_add_heat__heat),
                                text = HeadToHeadUseCase.shortRoundName(state.previousHeat.heat).get(),
                        )
                        DataRow(
                                title = stringResource(
                                        R.string.head_to_head_add_end__score_text,
                                        state.previousHeat.teamRunningTotal,
                                        state.previousHeat.opponentRunningTotal,
                                ),
                                text = state.previousHeat.result.title.get(),
                        )
                    }
                }
            }
        }

        Surface(
                shape = RoundedCornerShape(CodexTheme.dimens.cornerRounding),
                border = BorderStroke(1.dp, CodexTheme.colors.listItemOnAppBackground),
                color = CodexTheme.colors.appBackground,
                modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = CodexTheme.dimens.screenPadding)
        ) {
            HeadToHeadAddHeatContent(
                    state = state,
                    listener = { listener(it) },
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 25.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeadToHeadAddHeatContent(
        state: HeadToHeadAddHeatState,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadAddHeatIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    SimpleDialog(
            isShown = state.extras.showSelectHeatDialog,
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
                            text = state.extras.heat?.let { HeadToHeadUseCase.shortRoundName(it).get() }
                                    ?: stringResource(R.string.head_to_head_add_heat__heat_null),
                            style = LocalTextStyle.current
                                    .asClickableStyle()
                                    .copy(
                                            color = if (state.extras.showHeatRequiredError) CodexTheme.colors.errorOnAppBackground
                                            else CodexTheme.colors.linkText,
                                    ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                    .clickable { listener(HeatClicked) }
                                    .align(Alignment.CenterVertically)
                                    .semantics {
                                        if (state.extras.showHeatRequiredError) {
                                            error(errorString)
                                        }
                                    }
                    )
                    if (state.extras.showHeatRequiredError) {
                        Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = CodexTheme.colors.errorOnAppBackground,
                        )
                    }
                }
                CodexChip(
                        text = stringResource(R.string.head_to_head_ref__bye),
                        selected = state.extras.isBye,
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
                        text = state.extras.opponent,
                        enabled = !state.extras.isBye,
                        onValueChange = { listener(OpponentUpdated(it)) },
                        placeholderText = stringResource(R.string.head_to_head_add_heat__opponent_placeholder),
                )
            }
            CodexLabelledNumberFieldWithErrorMessage(
                    title = stringResource(R.string.head_to_head_add_heat__opponent_quali_rank),
                    enabled = !state.extras.isBye,
                    currentValue = state.extras.opponentQualiRank.text,
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

@Preview
@Composable
fun Heat_HeadToHeadAddScreen_Preview() {
    CodexTheme {
        HeadToHeadAddHeatScreen(
                state = HeadToHeadAddHeatState(
                        previousHeat = HeadToHeadAddHeatState.PreviousHeat(
                                heat = 0,
                                result = HeadToHeadResult.WIN,
                                teamRunningTotal = 6,
                                opponentRunningTotal = 0,
                        ),
                ),
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun HeadToHeadAddHeatScreen_Preview() {
    CodexTheme {
        HeadToHeadAddHeatScreen(
                state = HeadToHeadAddHeatState(),
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
        HeadToHeadAddHeatScreen(
                state = HeadToHeadAddHeatState(
                        extras = HeadToHeadAddHeatExtras(isBye = true, showHeatRequiredError = true),
                ),
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        widthDp = 350,
)
@Composable
fun Short_HeadToHeadAddHeatScreen_Preview() {
    CodexTheme {
        HeadToHeadAddHeatScreen(
                state = HeadToHeadAddHeatState(
                        extras = HeadToHeadAddHeatExtras(isBye = true, showHeatRequiredError = true),
                ),
        ) {}
    }
}
