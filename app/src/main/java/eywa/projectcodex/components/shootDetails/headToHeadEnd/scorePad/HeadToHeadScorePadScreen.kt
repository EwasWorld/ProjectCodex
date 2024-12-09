package eywa.projectcodex.components.shootDetails.headToHeadEnd.scorePad

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.commonUi.HandleMainEffects
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsMainScreen
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGrid
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowDataPreviewHelper
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeat
import eywa.projectcodex.model.headToHead.FullHeadToHeadHeat
import eywa.projectcodex.model.headToHead.FullHeadToHeadSet

@Composable
fun HeadToHeadScorePadScreen(
        navController: NavController,
        viewModel: HeadToHeadScorePadViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: HeadToHeadScorePadIntent -> viewModel.handle(it) }

    ShootDetailsMainScreen(
            currentScreen = CodexNavRoute.HEAD_TO_HEAD_SCORE_PAD,
            state = state,
            listener = { listener(HeadToHeadScorePadIntent.ShootDetailsAction(it)) },
    ) { it, modifier -> HeadToHeadScorePadScreen(it, modifier, listener) }

    HandleMainEffects(
            navController = navController,
            state = state,
            listener = { listener(HeadToHeadScorePadIntent.ShootDetailsAction(it)) },
    )

    val data = state.getData()
    LaunchedEffect(
            data?.extras?.openAddHeat,
            data?.extras?.openSightersForHeat,
            data?.extras?.openEditHeatInfo,
    ) {
        if (data?.extras?.openAddHeat == true) {
            CodexNavRoute.HEAD_TO_HEAD_ADD_HEAT.navigate(
                    navController,
                    mapOf(NavArgument.SHOOT_ID to viewModel.shootId.toString()),
                    popCurrentRoute = true,
            )
            listener(HeadToHeadScorePadIntent.GoToAddEndHandled)
        }

        if (data?.extras?.openSightersForHeat != null) {
            CodexNavRoute.SHOOT_DETAILS_ADD_COUNT.navigate(
                    navController,
                    mapOf(
                            NavArgument.SHOOT_ID to viewModel.shootId.toString(),
                            NavArgument.MATCH_NUMBER to data.extras.openSightersForHeat.toString(),
                            NavArgument.IS_SIGHTERS to true.toString(),
                    ),
            )
            listener(HeadToHeadScorePadIntent.EditSightersHandled)
        }

        if (data?.extras?.openEditHeatInfo != null) {
            CodexNavRoute.HEAD_TO_HEAD_ADD_HEAT.navigate(
                    navController,
                    mapOf(
                            NavArgument.SHOOT_ID to viewModel.shootId.toString(),
                            NavArgument.MATCH_NUMBER to data.extras.openEditHeatInfo.toString(),
                    ),
                    popCurrentRoute = true,
            )
            listener(HeadToHeadScorePadIntent.EditHeatInfoHandled)
        }
    }
}

@Composable
fun HeadToHeadScorePadScreen(
        state: HeadToHeadScorePadState,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadScorePadIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HeadToHeadScorePadIntent.HelpShowcaseAction(it)) }

    ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        if (state.entries.isEmpty()) {
            Column(
                    verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = modifier
                            .background(CodexTheme.colors.appBackground)
                            .padding(CodexTheme.dimens.screenPadding)
            ) {
                Text(
                        text = stringResource(R.string.head_to_head_score_pad__no_heats),
                )
                CodexButton(
                        text = stringResource(R.string.head_to_head_score_pad__no_heats_button),
                        onClick = { listener(HeadToHeadScorePadIntent.GoToAddEnd) }
                )
            }
            return@ProvideTextStyle
        }

        Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                        .background(CodexTheme.colors.appBackground)
                        .padding(vertical = CodexTheme.dimens.screenPadding)
        ) {
            state.entries.forEach { entry ->
                Surface(
                        border = BorderStroke(1.dp, CodexTheme.colors.onAppBackground),
                        shape = RoundedCornerShape(CodexTheme.dimens.smallCornerRounding),
                        color = Color.Transparent,
                        modifier = Modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
                ) {
                    Box {
                        CodexIconButton(
                                icon = CodexIconInfo.VectorIcon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.head_to_head_score_pad__edit_heat),
                                ),
                                onClick = { listener(HeadToHeadScorePadIntent.EditHeatInfo(entry.heat.matchNumber)) },
                                modifier = Modifier.align(Alignment.BottomEnd)
                        )

                        Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier
                                        .padding(horizontal = 15.dp, vertical = 10.dp)
                                        .fillMaxWidth()
                        ) {
                            if (entry.heat.heat != null) {
                                DataRow(
                                        title = stringResource(
                                                R.string.head_to_head_add_heat__heat,
                                                entry.heat.matchNumber,
                                        ),
                                        text = HeadToHeadUseCase.roundName(entry.heat.heat).get(),
                                )
                            }
                            else {
                                Text(
                                        text = stringResource(
                                                R.string.head_to_head_add_heat__match_header,
                                                entry.heat.matchNumber,
                                        )
                                )
                            }

                            entry.heat.opponentString()?.get()?.let { opponent ->
                                if (!entry.heat.opponent.isNullOrBlank()) {
                                    Text(
                                            text = opponent,
                                    )
                                }
                            }
                            DataRow(
                                    title = stringResource(R.string.add_count__sighters),
                                    text = entry.heat.sightersCount.toString(),
                                    onClick = {
                                        listener(HeadToHeadScorePadIntent.EditSighters(entry.heat.matchNumber))
                                    },
                                    modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
                if (entry.sets.isEmpty()) {
                    Text(
                            text = stringResource(
                                    if (entry.heat.isBye) R.string.head_to_head_score_pad__is_bye
                                    else R.string.head_to_head_score_pad__no_sets
                            ),
                            style = CodexTypography.NORMAL_PLUS,
                            color = CodexTheme.colors.onAppBackground,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier
                                    .padding(horizontal = CodexTheme.dimens.screenPadding)
                                    .padding(top = 5.dp, bottom = 30.dp)
                    )
                }
                else {
                    check(!entry.heat.isBye) { "Cannot have entries and be a bye" }
                    HeadToHeadGrid(
                            state = entry.toGridState(),
                            errorOnIncompleteRows = false,
                            rowClicked = { _, _ -> },
                            onTextValueChanged = { _, _ -> },
                            editTypesClicked = {},
                            helpListener = helpListener,
                            modifier = Modifier.padding(top = 10.dp, bottom = 40.dp)
                    )
                }
            }
        }
    }
}

enum class HeadToHeadScorePadTestTag : CodexTestTag {
    SCREEN,
    ;

    override val screenName: String
        get() = "HEAD_TO_HEAD_SCORE_PAD"

    override fun getElement(): String = name
}

@Preview(
        heightDp = 1200,
)
@Composable
fun HeadToHeadScorePadScreen_Preview() {
    CodexTheme {
        HeadToHeadScorePadScreen(
                HeadToHeadScorePadState(
                        entries = listOf(
                                FullHeadToHeadHeat(
                                        heat = DatabaseHeadToHeadHeat(
                                                matchNumber = 1,
                                                heat = 1,
                                                opponent = "Jessica Summers",
                                                opponentQualificationRank = 1,
                                                shootId = 1,
                                                sightersCount = 0,
                                                isBye = false,
                                                isShootOffWin = false,
                                        ),
                                        isRecurveStyle = true,
                                        isStandardFormat = true,
                                        teamSize = 1,
                                        sets = listOf(),
                                ),
                                FullHeadToHeadHeat(
                                        heat = DatabaseHeadToHeadHeat(
                                                matchNumber = 1,
                                                heat = 1,
                                                opponent = "Jessica Summ",
                                                opponentQualificationRank = 1,
                                                shootId = 1,
                                                sightersCount = 0,
                                                isBye = false,
                                                isShootOffWin = false,
                                        ),
                                        isRecurveStyle = true,
                                        isStandardFormat = true,
                                        teamSize = 1,
                                        sets = listOf(
                                                FullHeadToHeadSet(
                                                        data = HeadToHeadGridRowDataPreviewHelper.create(),
                                                        teamSize = 1,
                                                        isShootOffWin = false,
                                                        setNumber = 1,
                                                        isRecurveStyle = true,
                                                ),
                                                FullHeadToHeadSet(
                                                        data = HeadToHeadGridRowDataPreviewHelper.create(),
                                                        teamSize = 1,
                                                        isShootOffWin = false,
                                                        setNumber = 2,
                                                        isRecurveStyle = true,
                                                ),
                                        ),
                                ),
                                FullHeadToHeadHeat(
                                        heat = DatabaseHeadToHeadHeat(
                                                matchNumber = 1,
                                                heat = 1,
                                                opponent = null,
                                                opponentQualificationRank = null,
                                                shootId = 1,
                                                sightersCount = 0,
                                                isBye = false,
                                                isShootOffWin = false,
                                        ),
                                        isRecurveStyle = true,
                                        isStandardFormat = true,
                                        teamSize = 1,
                                        sets = listOf(
                                                FullHeadToHeadSet(
                                                        data = HeadToHeadGridRowDataPreviewHelper.create(),
                                                        teamSize = 1,
                                                        isShootOffWin = false,
                                                        setNumber = 1,
                                                        isRecurveStyle = true,
                                                ),
                                                FullHeadToHeadSet(
                                                        data = HeadToHeadGridRowDataPreviewHelper.create(),
                                                        teamSize = 1,
                                                        isShootOffWin = false,
                                                        setNumber = 2,
                                                        isRecurveStyle = true,
                                                ),
                                        ),
                                ),
                        ),
                )
        ) {}
    }
}

@Preview
@Composable
fun Empty_HeadToHeadScorePadScreen_Preview() {
    CodexTheme {
        HeadToHeadScorePadScreen(HeadToHeadScorePadState(entries = listOf())) {}
    }
}
