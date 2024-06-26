package eywa.projectcodex.components.shootDetails.addArrowCount

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.DEFAULT_INT_NAV_ARG
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.numberField.CodexNumberField
import eywa.projectcodex.common.sharedUi.numberField.CodexNumberFieldErrorText
import eywa.projectcodex.common.sharedUi.numberField.PartialNumberFieldState
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.addArrowCount.AddArrowCountIntent.*
import eywa.projectcodex.components.shootDetails.addEnd.RemainingArrowsIndicator
import eywa.projectcodex.components.shootDetails.addEnd.SightMark
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.stats.ui.DateAndRoundSection

@Composable
fun AddArrowCountScreen(
        navController: NavController,
        viewModel: AddArrowCountViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val data = state.getData()
    val listener = { it: AddArrowCountIntent -> viewModel.handle(it) }

    if (data == null) {
        Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator(color = CodexTheme.colors.onAppBackground)
        }
    }
    else {
        AddArrowCountScreen(data, listener)
    }

    // Has a round, no arrow counts, and is not sighters
    val isScoring =
            data?.fullShootInfo != null
                    && data.fullShootInfo.arrowCounter == null
                    && !data.isEditingSighters
    LaunchedEffect(isScoring) {
        if (isScoring) {
            CodexNavRoute.SHOOT_DETAILS_ADD_END.navigate(
                    navController,
                    mapOf(NavArgument.SHOOT_ID to data!!.fullShootInfo.id.toString()),
                    popCurrentRoute = true,
            )
        }
    }

    LaunchedEffect(data?.editShootInfoClicked) {
        if (data?.editShootInfoClicked == true) {
            CodexNavRoute.NEW_SCORE.navigate(
                    navController,
                    mapOf(NavArgument.SHOOT_ID to data.fullShootInfo.id.toString()),
            )
            listener(EditShootInfoHandled)
        }
    }

    LaunchedEffect(data?.openEditSightMark) {
        if (data?.openEditSightMark == true) {
            val args = if (data.sightMark != null) {
                mapOf(NavArgument.SIGHT_MARK_ID to data.sightMark.id.toString())
            }
            else {
                val distance = data.fullShootInfo.remainingArrowsAtDistances?.firstOrNull()?.second
                        ?: DEFAULT_INT_NAV_ARG
                val isMetric = data.fullShootInfo.round?.isMetric ?: true
                mapOf(NavArgument.DISTANCE to distance.toString(), NavArgument.IS_METRIC to isMetric.toString())
            }
            CodexNavRoute.SIGHT_MARK_DETAIL.navigate(navController, args)
            listener(EditSightMarkHandled)
        }
    }

    LaunchedEffect(data?.openFullSightMarks) {
        if (data?.openFullSightMarks == true) {
            CodexNavRoute.SIGHT_MARKS.navigate(navController)
            listener(FullSightMarksHandled)
        }
    }

    LaunchedEffect(data?.openEditSighters) {
        if (data?.openEditSighters == true) {
            CodexNavRoute.SHOOT_DETAILS_ADD_COUNT.navigate(
                    navController,
                    mapOf(
                            NavArgument.SHOOT_ID to data.fullShootInfo.id.toString(),
                            NavArgument.IS_SIGHTERS to true.toString(),
                    ),
            )
            listener(EditSightersHandled)
        }
    }
}

@Composable
fun AddArrowCountScreen(
        state: AddArrowCountState,
        listener: (AddArrowCountIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterVertically),
            modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(CodexTheme.dimens.screenPadding)
                    .testTag(AddArrowCountTestTag.SCREEN)
    ) {
        ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
            DateAndRoundSection(
                    fullShootInfo = state.fullShootInfo,
                    editClickedListener = { listener(ClickEditShootInfo) },
                    helpListener = helpListener,
            )

            SightMark(
                    fullShootInfo = state.fullShootInfo,
                    sightMark = state.sightMark,
                    helpListener = helpListener,
                    onExpandClicked = { listener(FullSightMarksClicked) },
                    onEditClicked = { listener(EditSightMarkClicked) },
                    modifier = Modifier
            )
            RemainingArrowsIndicator(state.fullShootInfo, helpListener)
            ShotCount(
                    state = state,
                    helpListener = helpListener,
                    onSightersClicked = { listener(EditSightersClicked) },
                    modifier = Modifier.padding(vertical = 15.dp)
            )
            IncreaseCountInputs(state, listener)
        }
    }
}

@Composable
private fun IncreaseCountInputs(
        state: AddArrowCountState,
        listener: (AddArrowCountIntent) -> Unit,
) {
    val resources = LocalContext.current.resources
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    if (state.fullShootInfo.isRoundComplete) {
        Text(
                text = stringResource(R.string.add_count__round_complete),
                modifier = Modifier
                        .testTag(AddArrowCountTestTag.ROUND_COMPLETE)
                        .updateHelpDialogPosition(
                                helpState = HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_archer_round_stats__round_complete_title),
                                        helpBody = stringResource(R.string.help_archer_round_stats__round_complete_body),
                                ).asHelpState(helpListener),
                        )
        )
        return
    }

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.updateHelpDialogPosition(
                        helpState = HelpShowcaseItem(
                                helpTitle = stringResource(R.string.help_add_count__end_size_title),
                                helpBody = stringResource(R.string.help_add_count__end_size_body),
                        ).asHelpState(helpListener),
                )
        ) {
            CodexIconButton(
                    icon = CodexIconInfo.VectorIcon(imageVector = Icons.Default.Remove),
                    onClick = { listener(ClickDecrease) },
                    modifier = Modifier
                            .testTag(AddArrowCountTestTag.INPUT_MINUS_BUTTON)
                            .clearAndSetSemantics { }
            )
            CodexNumberField(
                    contentDescription = stringResource(R.string.add_count__add_count_input_desc),
                    currentValue = state.endSize.text,
                    errorMessage = state.endSize.error,
                    testTag = AddArrowCountTestTag.ADD_COUNT_INPUT,
                    placeholder = "6",
                    onValueChanged = { listener(OnValueChanged(it)) },
                    modifier = Modifier.semantics {
                        customActions = listOf(
                                CustomAccessibilityAction(
                                        label = resources.getString(R.string.add_count__set_add_count_to_desc, 3),
                                        action = { listener(OnValueChanged("3")); true },
                                ),
                                CustomAccessibilityAction(
                                        label = resources.getString(R.string.add_count__set_add_count_to_desc, 6),
                                        action = { listener(OnValueChanged("6")); true },
                                ),
                                CustomAccessibilityAction(
                                        label = resources.getString(R.string.add_count__increase_add_count_desc),
                                        action = { listener(ClickIncrease); true },
                                ),
                                CustomAccessibilityAction(
                                        label = resources.getString(R.string.add_count__decrease_add_count_desc),
                                        action = { listener(ClickDecrease); true },
                                ),
                        )
                    }
            )
            CodexIconButton(
                    icon = CodexIconInfo.VectorIcon(imageVector = Icons.Default.Add),
                    onClick = { listener(ClickIncrease) },
                    modifier = Modifier
                            .testTag(AddArrowCountTestTag.INPUT_PLUS_BUTTON)
                            .clearAndSetSemantics { }
            )
        }
        CodexNumberFieldErrorText(
                errorText = state.endSize.error,
                testTag = AddArrowCountTestTag.ADD_COUNT_INPUT_ERROR,
        )

        CodexButton(
                text = stringResource(R.string.add_count__submit),
                onClick = { listener(ClickSubmit) },
                enabled = state.endSize.error == null,
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_add_count__submit_title),
                        helpBody = stringResource(R.string.help_add_count__submit_body),
                ).asHelpState(helpListener),
                modifier = Modifier
                        .padding(top = 5.dp)
                        .testTag(AddArrowCountTestTag.SUBMIT)
        )
    }
}

enum class AddArrowCountTestTag : CodexTestTag {
    SCREEN,
    ADD_COUNT_INPUT,
    ADD_COUNT_INPUT_ERROR,
    SIGHTERS_COUNT,
    SHOT_COUNT,
    TOTAL_COUNT,
    INPUT_PLUS_BUTTON,
    INPUT_MINUS_BUTTON,
    SUBMIT,
    ROUND_COMPLETE,
    ;

    override val screenName: String
        get() = "ADD_ARROW_COUNT"

    override fun getElement(): String = name
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun AddArrowCountScreen_Preview() {
    CodexTheme {
        AddArrowCountScreen(
                AddArrowCountState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    addRound(RoundPreviewHelper.yorkRoundData, 12)
                                    addArrowCounter(30)
                                },
                        ),
                        extras = AddArrowCountExtras(PartialNumberFieldState("6")),
                        isEditingSighters = false,
                )
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun NoRound_AddArrowCountScreen_Preview() {
    CodexTheme {
        AddArrowCountScreen(
                AddArrowCountState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    addArrowCounter(24)
                                },
                        ),
                        extras = AddArrowCountExtras(PartialNumberFieldState("6")),
                        isEditingSighters = false,
                )
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun RoundComplete_AddArrowCountScreen_Preview() {
    CodexTheme {
        AddArrowCountScreen(
                AddArrowCountState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.yorkRoundData
                                    completeRoundWithCounter()
                                },
                        ),
                        extras = AddArrowCountExtras(PartialNumberFieldState("0")),
                        isEditingSighters = false,
                )
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Error_AddArrowCountScreen_Preview() {
    CodexTheme {
        AddArrowCountScreen(
                AddArrowCountState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.yorkRoundData
                                    addArrowCounter(24)
                                },
                        ),
                        extras = AddArrowCountExtras(PartialNumberFieldState("hi")),
                        isEditingSighters = false,
                )
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Sighters_AddArrowCountScreen_Preview() {
    CodexTheme {
        AddArrowCountScreen(
                AddArrowCountState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    addRound(RoundPreviewHelper.yorkRoundData, 12)
                                    addArrowCounter(30)
                                },
                        ),
                        extras = AddArrowCountExtras(PartialNumberFieldState("6")),
                        isEditingSighters = true,
                )
        ) {}
    }
}
