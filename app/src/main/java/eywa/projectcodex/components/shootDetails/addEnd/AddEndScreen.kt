package eywa.projectcodex.components.shootDetails.addEnd

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
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
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.addEnd.AddEndIntent.*
import eywa.projectcodex.components.shootDetails.commonUi.HandleMainEffects
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsMainScreen
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsStatePreviewHelper
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsScaffold
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.sightMarks.SightMarksPreviewHelper
import eywa.projectcodex.database.rounds.getDistanceUnitRes
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.model.SightMark
import eywa.projectcodex.model.user.CodexUser

@Composable
fun AddEndScreen(
        navController: NavController,
        viewModel: AddEndViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: AddEndIntent -> viewModel.handle(it) }

    ShootDetailsMainScreen(
            currentScreen = CodexNavRoute.SHOOT_DETAILS_ADD_END,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    ) { it, modifier -> AddEndScreen(it, modifier, listener) }

    HandleMainEffects(
            navController = navController,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    )
    HandleEffects(navController, state, listener)
}

@Composable
fun HandleEffects(
        navController: NavController,
        state: ShootDetailsResponse<AddEndState>,
        listener: (AddEndIntent) -> Unit,
) {
    val loadedState = state.getData() ?: return
    val context = LocalContext.current

    LaunchedEffect(loadedState) {
        loadedState.errors.forEach {
            ToastSpamPrevention.displayToast(context, context.resources.getString(it.messageId))
            listener(ErrorHandled(it))
        }

        if (loadedState.openFullSightMarks) {
            CodexNavRoute.SIGHT_MARKS.navigate(navController)
            listener(FullSightMarksHandled)
        }

        if (loadedState.openEditSightMark) {
            val args = if (loadedState.sightMark != null) {
                mapOf(NavArgument.SIGHT_MARK_ID to loadedState.sightMark.id.toString())
            }
            else {
                val distance = loadedState.fullShootInfo.remainingArrowsAtDistances?.firstOrNull()?.second
                        ?: DEFAULT_INT_NAV_ARG
                val isMetric = loadedState.fullShootInfo.round?.isMetric ?: true
                mapOf(NavArgument.DISTANCE to distance.toString(), NavArgument.IS_METRIC to isMetric.toString())
            }
            CodexNavRoute.SIGHT_MARK_DETAIL.navigate(navController, args)
            listener(EditSightMarkHandled)
        }

        if (loadedState.openSighters) {
            CodexNavRoute.SHOOT_DETAILS_ADD_COUNT.navigate(
                    navController,
                    mapOf(
                            NavArgument.SHOOT_ID to loadedState.fullShootInfo.id.toString(),
                            NavArgument.IS_SIGHTERS to true.toString(),
                    ),
            )
            listener(SightersHandled)
        }
    }
}

@Composable
private fun AddEndScreen(
        state: AddEndState,
        modifier: Modifier = Modifier,
        listener: (AddEndIntent) -> Unit,
) {
    if (state.isRoundFull) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = modifier
                        .padding(vertical = CodexTheme.dimens.screenPadding)
                        .testTag(AddEndTestTag.SCREEN)
        ) {
            AddEndContent(
                    state = state,
                    listener = listener,
                    modifier = Modifier.padding(vertical = 10.dp)
            )

            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(
                            vertical = 10.dp,
                            horizontal = CodexTheme.dimens.screenPadding,
                    )
            ) {
                Text(
                        text = stringResource(R.string.input_end__cannot_open_input_end_title),
                        style = CodexTypography.LARGE,
                        color = CodexTheme.colors.onAppBackground,
                        textAlign = TextAlign.Center,
                )
                Text(
                        text = stringResource(R.string.input_end__cannot_open_input_end_body),
                        style = CodexTypography.NORMAL,
                        color = CodexTheme.colors.onAppBackground,
                        textAlign = TextAlign.Center,
                )
                CodexButton(
                        text = stringResource(R.string.input_end__go_to_summary),
                        onClick = { listener(RoundFullDialogOkClicked) },
                        modifier = Modifier
                                .padding(top = 10.dp)
                                .testTag(AddEndTestTag.ROUND_COMPLETE_BUTTON)
                )
            }
        }
    }
    else {
        ArrowInputsScaffold(
                state = state,
                showCancelButton = false,
                showResetButton = false,
                submitButtonText = stringResource(R.string.input_end__next_end),
                helpListener = { listener(HelpShowcaseAction(it)) },
                submitHelpInfo = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_input_end__next_end_title),
                        helpBody = stringResource(R.string.help_input_end__next_end_body),
                ),
                testTag = AddEndTestTag.SCREEN,
                listener = { listener(ArrowInputsAction(it)) },
                modifier = modifier
        ) {
            AddEndContent(state, Modifier, listener)
        }
    }
}

@Composable
private fun AddEndContent(
        state: AddEndState,
        modifier: Modifier = Modifier,
        listener: (AddEndIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    Column(
            verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.padding(bottom = 10.dp)
    ) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                        .padding(bottom = 10.dp)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = CodexTheme.dimens.screenPadding)
        ) {
            SightMark(
                    fullShootInfo = state.fullShootInfo,
                    sightMark = state.sightMark,
                    helpListener = helpListener,
                    onExpandClicked = { listener(FullSightMarksClicked) },
                    onEditClicked = { listener(EditSightMarkClicked) },
            )
            state.fullShootInfo.remainingArrowsAtDistances?.let {
                Text(
                        text = stringResource(R.string.input_end__section_delimiter),
                        style = CodexTypography.NORMAL,
                        color = CodexTheme.colors.onAppBackground,
                )
            }
            RemainingArrowsIndicator(
                    state.fullShootInfo,
                    helpListener,
            )
        }
        ArrowsShot(
                sighters = state.fullShootInfo.shootRound?.sightersCount ?: 0,
                arrowsShot = state.fullShootInfo.arrowsShot,
                onClickSighters = { listener(SightersClicked) },
                helpListener = helpListener,
        )
        ScoreIndicator(
                totalScore = state.fullShootInfo.score,
                helpListener = helpListener,
        )
    }
}

@Composable
fun ArrowsShot(
        arrowsShot: Int,
        sighters: Int,
        onClickSighters: () -> Unit,
        modifier: Modifier = Modifier,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
    ) {
        DataRow(
                title = stringResource(R.string.input_end__sighters_header),
                text = sighters.toString(),
                titleStyle = CodexTypography.SMALL_PLUS.copy(color = CodexTheme.colors.onAppBackground),
                textStyle = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)
                        .asClickableStyle(),
                textModifier = Modifier.testTag(AddEndTestTag.SIGHTERS),
                modifier = Modifier
                        .updateHelpDialogPosition(
                                HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_input_end__sighters_title),
                                        helpBody = stringResource(R.string.help_input_end__sighters_body),
                                ).asHelpState(helpListener),
                        )
                        .clickable { onClickSighters() }
        )
        DataRow(
                title = stringResource(R.string.input_end__archer_arrows_count_header),
                text = arrowsShot.toString(),
                titleStyle = CodexTypography.SMALL_PLUS.copy(color = CodexTheme.colors.onAppBackground),
                textStyle = CodexTypography.NORMAL_PLUS.copy(color = CodexTheme.colors.onAppBackground),
                textModifier = Modifier.testTag(AddEndTestTag.ROUND_ARROWS),
                modifier = Modifier
                        .updateHelpDialogPosition(
                                HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_input_end__arrows_shot_title),
                                        helpBody = stringResource(R.string.help_input_end__arrows_shot_body),
                                ).asHelpState(helpListener),
                        )
        )
    }
}

@Composable
fun SightMark(
        fullShootInfo: FullShootInfo,
        sightMark: SightMark?,
        modifier: Modifier = Modifier,
        helpListener: (HelpShowcaseIntent) -> Unit,
        onEditClicked: () -> Unit,
        onExpandClicked: () -> Unit,
) = SightMark(
        distance = fullShootInfo.remainingArrowsAtDistances?.firstOrNull()?.second
                ?: fullShootInfo.roundDistances?.minOfOrNull { it.distance },
        isMetric = fullShootInfo.round?.isMetric,
        sightMark = sightMark,
        helpListener = helpListener,
        onEditClicked = onEditClicked,
        onExpandClicked = onExpandClicked,
        modifier = modifier
)

@Composable
fun SightMark(
        distance: Int?,
        isMetric: Boolean?,
        sightMark: SightMark?,
        modifier: Modifier = Modifier,
        helpListener: (HelpShowcaseIntent) -> Unit,
        onEditClicked: () -> Unit,
        onExpandClicked: () -> Unit,
) {
    val distanceUnit = isMetric?.let { stringResource(getDistanceUnitRes(it)!!) }

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = modifier
    ) {
        if (distance != null && distanceUnit != null) {
            Text(
                    text = stringResource(R.string.input_end__sight_mark_title),
                    style = CodexTypography.SMALL_PLUS,
                    color = CodexTheme.colors.onAppBackground,
            )
            DataRow(
                    title = stringResource(
                            R.string.input_end__sight_mark,
                            distance,
                            distanceUnit,
                    ),
                    text = sightMark?.sightMark?.toString()
                            ?: stringResource(R.string.input_end__sight_mark_none_placeholder),
                    titleStyle = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
                    textStyle = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
                    onClick = { onEditClicked() },
                    onClickLabel = stringResource(
                            if (sightMark != null) R.string.input_end__sight_mark_edit
                            else R.string.input_end__sight_mark_edit_none,
                            distance,
                            distanceUnit,
                    ),
                    textModifier = Modifier,
                    modifier = Modifier
                            .testTag(AddEndTestTag.SIGHT_MARK)
                            .updateHelpDialogPosition(
                                    HelpShowcaseItem(
                                            helpTitle = stringResource(R.string.help_input_end__sight_mark_title),
                                            helpBody = stringResource(R.string.help_input_end__sight_mark_body),
                                    ).asHelpState(helpListener),
                            )
            )
        }
        Text(
                text = stringResource(
                        if (distance != null && distanceUnit != null) R.string.input_end__sight_mark_expand
                        else R.string.input_end__sight_mark_expand_no_distances,
                ),
                style = CodexTypography.SMALL.asClickableStyle(),
                modifier = Modifier
                        .testTag(AddEndTestTag.EXPAND_SIGHT_MARK)
                        .clickable { onExpandClicked() }
        )
    }
}

@Composable
private fun ScoreIndicator(
        totalScore: Int,
        modifier: Modifier = Modifier,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    val resources = LocalContext.current.resources

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                    .border(width = 1.dp, color = CodexTheme.colors.onAppBackground)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .updateHelpDialogPosition(
                            HelpShowcaseItem(
                                    helpTitle = stringResource(R.string.help_input_end__score_title),
                                    helpBody = stringResource(R.string.help_input_end__score_body),
                            ).asHelpState(helpListener),
                    )
    ) {
        Text(
                text = stringResource(R.string.input_end__archer_score_header),
                style = CodexTypography.SMALL_PLUS,
                color = CodexTheme.colors.onAppBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.clearAndSetSemantics { }
        )
        Text(
                text = totalScore.toString(),
                style = CodexTypography.LARGE,
                color = CodexTheme.colors.onAppBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                        .testTag(AddEndTestTag.ROUND_SCORE)
                        .semantics {
                            contentDescription = resources.getString(
                                    R.string.input_end__archer_score_accessibility_text,
                                    totalScore,
                            )
                        }
        )
    }
}

@Composable
fun RemainingArrowsIndicator(
        fullShootInfo: FullShootInfo,
        helpListener: (HelpShowcaseIntent) -> Unit,
        modifier: Modifier = Modifier,
) {
    fullShootInfo.remainingArrowsAtDistances?.let {
        val delimiter = stringResource(R.string.general_comma_new_line_separator)
        val remainingStrings = it.map { (count, distance) ->
            stringResource(
                    R.string.input_end__round_indicator_at,
                    count,
                    distance,
                    stringResource(fullShootInfo.distanceUnit!!),
            )
        }

        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                        .updateHelpDialogPosition(
                                HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_input_end__remaining_arrows_title),
                                        helpBody = stringResource(R.string.help_input_end__remaining_arrows_body),
                                ).asHelpState(helpListener),
                        )
        ) {
            Text(
                    text = stringResource(R.string.input_end__round_indicator_label),
                    style = CodexTypography.SMALL_PLUS,
                    color = CodexTheme.colors.onAppBackground,
            )
            Text(
                    text = remainingStrings[0] + if (it.size > 1) delimiter.trim() else "",
                    style = CodexTypography.NORMAL,
                    color = CodexTheme.colors.onAppBackground,
                    modifier = Modifier.testTag(AddEndTestTag.REMAINING_ARROWS_CURRENT)
            )
            if (it.size > 1) {
                Text(
                        text = remainingStrings.drop(1).joinToString(delimiter),
                        style = CodexTypography.SMALL,
                        color = CodexTheme.colors.onAppBackground,
                        modifier = Modifier.testTag(AddEndTestTag.REMAINING_ARROWS_LATER)
                )
            }
        }
    }
}

enum class AddEndTestTag : CodexTestTag {
    SCREEN,
    REMAINING_ARROWS_CURRENT,
    REMAINING_ARROWS_LATER,
    ROUND_SCORE,
    ROUND_ARROWS,
    SIGHT_MARK,
    EXPAND_SIGHT_MARK,
    ROUND_COMPLETE_BUTTON,
    SIGHTERS,
    ;

    override val screenName: String
        get() = "SHOOT_DETAILS_ADD_END"

    override fun getElement(): String = name
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        heightDp = 700,
)
@Composable
fun AddEndScreen_Preview() {
    CodexTheme {
        AddEndScreen(
                AddEndState(
                        main = ShootDetailsStatePreviewHelper.WITH_SHOT_ARROWS
                                .copy(sightMark = SightMark(SightMarksPreviewHelper.sightMarks[0])),
                        extras = AddEndExtras(),
                ),
                modifier = Modifier.fillMaxSize()
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun CompleteAddEndScreen_Preview() {
    CodexTheme {
        AddEndScreen(
                AddEndState(
                        main = ShootDetailsState(
                                shootId = 1,
                                user = CodexUser(),
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.yorkRoundData
                                    completeRoundWithFinalScore(1200)
                                },
                                sightMark = SightMark(SightMarksPreviewHelper.sightMarks[0]),
                        ),
                        extras = AddEndExtras(),
                ),
                modifier = Modifier.fillMaxSize()
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        widthDp = 330,
        heightDp = 600,
)
@Composable
fun Mini_AddEndScreen_Preview() {
    CodexTheme {
        AddEndScreen(
                AddEndState(
                        main = ShootDetailsStatePreviewHelper.WITH_SHOT_ARROWS
                                .copy(addEndArrows = List(6) { Arrow(10) }),
                        extras = AddEndExtras(),
                ),
                modifier = Modifier.fillMaxSize()
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun NoRound_AddEndScreen_Preview() {
    CodexTheme {
        AddEndScreen(
                AddEndState(
                        main = ShootDetailsState(
                                shootId = 1,
                                user = CodexUser(),
                                fullShootInfo = ShootPreviewHelperDsl.create { },
                        ).copy(addEndArrows = List(6) { Arrow(10) }),
                        extras = AddEndExtras(),
                ),
                modifier = Modifier.fillMaxSize()
        ) {}
    }
}
