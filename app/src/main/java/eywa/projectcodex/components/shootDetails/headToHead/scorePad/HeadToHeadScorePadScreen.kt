package eywa.projectcodex.components.shootDetails.headToHead.scorePad

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
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
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.commonUi.HandleMainEffects
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsMainScreen
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGrid
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGridRowDataPreviewHelper
import eywa.projectcodex.components.shootDetails.headToHead.grid.SetDropdownMenuItem
import eywa.projectcodex.components.shootDetails.headToHead.scorePad.HeadToHeadScorePadIntent.*
import eywa.projectcodex.components.shootDetails.headToHead.scorePad.HeadToHeadScorePadMatchTestTag.*
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadMatch
import eywa.projectcodex.model.headToHead.FullHeadToHeadMatch
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
            listener = { listener(ShootDetailsAction(it)) },
    ) { it, modifier -> HeadToHeadScorePadScreen(it, modifier, listener) }

    HandleMainEffects(
            navController = navController,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    )

    val data = state.getData()
    LaunchedEffect(data?.extras) {
        if (data?.extras?.openAddMatch == true) {
            CodexNavRoute.HEAD_TO_HEAD_ADD_MATCH.navigate(
                    navController,
                    mapOf(NavArgument.SHOOT_ID to viewModel.shootId.toString()),
                    popCurrentRoute = true,
            )
            listener(GoToAddEndHandled)
        }

        if (data?.extras?.openEditSightersForMatch != null) {
            CodexNavRoute.SHOOT_DETAILS_ADD_COUNT.navigate(
                    navController,
                    mapOf(
                            NavArgument.SHOOT_ID to viewModel.shootId.toString(),
                            NavArgument.MATCH_NUMBER to data.extras.openEditSightersForMatch.toString(),
                            NavArgument.IS_SIGHTERS to true.toString(),
                    ),
            )
            listener(EditSightersHandled)
        }

        data?.extras?.menuOpenForMatchNumber?.let { match ->
            when (data.extras.matchMenuActionClicked) {
                MenuAction.EDIT -> {
                    CodexNavRoute.HEAD_TO_HEAD_ADD_MATCH.navigate(
                            navController,
                            mapOf(
                                    NavArgument.SHOOT_ID to viewModel.shootId.toString(),
                                    NavArgument.MATCH_NUMBER to match.toString(),
                            ),
                    )
                    listener(MatchOptionsMenuActionHandled)
                }

                MenuAction.INSERT -> {
                    CodexNavRoute.HEAD_TO_HEAD_ADD_MATCH.navigate(
                            navController,
                            mapOf(
                                    NavArgument.SHOOT_ID to viewModel.shootId.toString(),
                                    NavArgument.MATCH_NUMBER to match.toString(),
                                    NavArgument.IS_INSERT to true.toString(),
                            ),
                    )
                    listener(MatchOptionsMenuActionHandled)
                }

                MenuAction.NEW_SET -> {
                    CodexNavRoute.HEAD_TO_HEAD_ADD_END.navigate(
                            navController,
                            mapOf(
                                    NavArgument.SHOOT_ID to viewModel.shootId.toString(),
                                    NavArgument.MATCH_NUMBER to match.toString(),
                            ),
                    )
                    listener(SetOptionsMenuActionHandled)
                }

                MenuAction.DELETE -> Unit
                null -> Unit
            }
        }

        data?.extras?.menuOpenForSet?.let { (match, set) ->
            when (data.extras.setMenuActionClicked) {
                MenuAction.EDIT -> {
                    check(set > 0) { "Invalid set number" }
                    CodexNavRoute.HEAD_TO_HEAD_ADD_END.navigate(
                            navController,
                            mapOf(
                                    NavArgument.SHOOT_ID to viewModel.shootId.toString(),
                                    NavArgument.MATCH_NUMBER to match.toString(),
                                    NavArgument.SET_NUMBER to set.toString(),
                            ),
                    )
                    listener(SetOptionsMenuActionHandled)
                }

                MenuAction.INSERT -> {
                    check(set > 0) { "Invalid set number" }
                    CodexNavRoute.HEAD_TO_HEAD_ADD_END.navigate(
                            navController,
                            mapOf(
                                    NavArgument.SHOOT_ID to viewModel.shootId.toString(),
                                    NavArgument.MATCH_NUMBER to match.toString(),
                                    NavArgument.SET_NUMBER to set.toString(),
                                    NavArgument.IS_INSERT to true.toString(),
                            ),
                    )
                    listener(SetOptionsMenuActionHandled)
                }


                MenuAction.NEW_SET -> throw IllegalStateException()
                MenuAction.DELETE -> Unit
                null -> Unit
            }
        }
    }
}

@Composable
fun HeadToHeadScorePadScreen(
        state: HeadToHeadScorePadState,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadScorePadIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    val deleteDialogTitle: String?
    val deleteDialogMessage: String?

    if (state.extras.menuOpenForSet != null && state.extras.setMenuActionClicked == MenuAction.DELETE) {
        val (match, set) = state.extras.menuOpenForSet
        deleteDialogTitle = stringResource(R.string.head_to_head_score_pad__delete_dialog_title)
        deleteDialogMessage = stringResource(R.string.head_to_head_score_pad__delete_dialog_body, match, set)
    }
    else if (state.extras.menuOpenForMatchNumber != null && state.extras.matchMenuActionClicked == MenuAction.DELETE) {
        deleteDialogTitle = stringResource(R.string.head_to_head_add_heat__delete_dialog_title)
        deleteDialogMessage =
                stringResource(R.string.head_to_head_add_heat__delete_dialog_body, state.extras.menuOpenForMatchNumber)
    }
    else {
        deleteDialogTitle = null
        deleteDialogMessage = null
    }

    SimpleDialog(
            isShown = deleteDialogTitle != null,
            onDismissListener = { listener(SetOptionsMenuActionHandled) },
    ) {
        SimpleDialogContent(
                title = deleteDialogTitle ?: "",
                message = deleteDialogMessage ?: "",
                positiveButton = ButtonState(
                        text = stringResource(R.string.general_delete),
                        onClick = { listener(DeleteConfirmationOkClicked) },
                ),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener(DeleteConfirmationCancelClicked) },
                ),
        )
    }

    ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        if (state.entries.isEmpty()) {
            Column(
                    verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = modifier
                            .background(CodexTheme.colors.appBackground)
                            .padding(CodexTheme.dimens.screenPadding)
                            .testTag(HeadToHeadScorePadTestTag.SCREEN)
            ) {
                Text(
                        text = stringResource(R.string.head_to_head_score_pad__no_heats),
                )
                CodexButton(
                        text = stringResource(R.string.head_to_head_score_pad__no_heats_button),
                        onClick = { listener(GoToAddEnd) },
                        modifier = Modifier.testTag(HeadToHeadScorePadTestTag.ADD_MATCH_BUTTON)
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
                        .testTag(HeadToHeadScorePadTestTag.SCREEN)
        ) {
            state.entries.forEach { entry ->

                fun Modifier.testTag(testTag: HeadToHeadScorePadMatchTestTag) =
                        testTag(testTag.getTestTag(entry.match.matchNumber))

                Surface(
                        border = BorderStroke(1.dp, CodexTheme.colors.onAppBackground),
                        shape = RoundedCornerShape(CodexTheme.dimens.smallCornerRounding),
                        color = Color.Transparent,
                        modifier = Modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
                ) {
                    Box {
                        Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                            CodexIconButton(
                                    icon = CodexIconInfo.VectorIcon(imageVector = Icons.Default.MoreHoriz),
                                    captionBelow = stringResource(R.string.head_to_head_score_pad__edit_match),
                                    onClick = { listener(OpenMatchOptionsClicked(entry.match.matchNumber)) },
                                    modifier = Modifier
                                            .testTag(EDIT_MATCH_INFO_BUTTON.getTestTag(entry.match.matchNumber))
                                            .border(
                                                    shape = RoundedCornerShape(30, 0),
                                                    width = 1.dp,
                                                    color = Color.White
                                            )
                                            .padding(end = 1.dp)
                            )
                            DropdownMenu(
                                    allowNewEndsToBeAdded = entry.allowNewEndsToBeAdded(),
                                    expanded = state.extras.menuOpenForMatchNumber == entry.match.matchNumber,
                                    testTag = MATCH_DROPDOWN_MENU_ITEM.getTestTag(entry.match.matchNumber),
                                    itemClickedListener = {
                                        listener(MatchOptionsMenuClicked(entry.match.matchNumber, it))
                                    },
                                    dismissListener = { listener(CloseMatchOptionsMenu(entry.match.matchNumber)) },
                            )
                        }

                        Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier
                                        .padding(horizontal = 15.dp, vertical = 15.dp)
                                        .fillMaxWidth()
                        ) {
                            if (entry.match.heat != null) {
                                DataRow(
                                        title = stringResource(
                                                R.string.head_to_head_add_heat__heat,
                                                entry.match.matchNumber,
                                        ),
                                        text = HeadToHeadUseCase.roundName(entry.match.heat).get(),
                                        modifier = Modifier.testTag(MATCH_TEXT)
                                )
                            }
                            else {
                                Text(
                                        text = stringResource(
                                                R.string.head_to_head_add_heat__match_header,
                                                entry.match.matchNumber,
                                        ),
                                        modifier = Modifier.testTag(MATCH_TEXT)
                                )
                            }

                            entry.match.opponentString()?.get()?.let { opponent ->
                                if (!entry.match.opponent.isNullOrBlank()) {
                                    Text(
                                            text = opponent,
                                            modifier = Modifier.testTag(OPPONENT_TEXT)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            entry.match.maxPossibleRank?.let {
                                check(it >= 1)
                                val maxRank = when (it) {
                                    1 -> return@let
                                    2 -> stringResource(R.string.head_to_head_add_heat__max_rank_silver)
                                    3 -> stringResource(R.string.head_to_head_add_heat__max_rank_bronze)
                                    else -> it.toString()
                                }
                                DataRow(
                                        title = stringResource(R.string.head_to_head_add_heat__max_rank),
                                        text = maxRank,
                                        modifier = Modifier.testTag(MAX_RANK)
                                )
                            }
                            DataRow(
                                    title = stringResource(R.string.add_count__sighters),
                                    text = entry.match.sightersCount.toString(),
                                    onClick = {
                                        listener(EditSighters(entry.match.matchNumber))
                                    },
                                    modifier = Modifier.testTag(SIGHTERS)
                            )
                        }
                    }
                }

                if (entry.sets.isEmpty()) {
                    Text(
                            text = stringResource(
                                    if (entry.match.isBye) R.string.head_to_head_score_pad__is_bye
                                    else R.string.head_to_head_score_pad__no_sets
                            ),
                            style = CodexTypography.NORMAL_PLUS,
                            color = CodexTheme.colors.onAppBackground,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier
                                    .padding(horizontal = CodexTheme.dimens.screenPadding)
                                    .padding(top = 10.dp)
                                    .testTag(NO_ENDS)
                    )
                }
                else {
                    check(!entry.match.isBye) { "Cannot have entries and be a bye" }
                    HeadToHeadGrid(
                            state = entry.toGridState(
                                    dropdownMenuExpandedFor = state.setDropdownMenuExpandedFor
                                            .takeIf { entry.match.matchNumber == state.setDropdownMenuExpandedFor?.first },
                            ),
                            errorOnIncompleteRows = false,
                            rowClicked = { setNumber, _ ->
                                listener(SetClicked(entry.match.matchNumber, setNumber))
                            },
                            onTextValueChanged = { _, _ -> },
                            editTypesClicked = {},
                            helpListener = helpListener,
                            modifier = Modifier
                                    .padding(top = 15.dp, bottom = 10.dp)
                                    .testTag(GRID),
                            itemClickedListener = { setNumber: Int, dropdownMenuItem: SetDropdownMenuItem ->
                                listener(SetOptionsMenuClicked(entry.match.matchNumber, setNumber, dropdownMenuItem))
                            },
                            closeDropdownMenuListener = {
                                listener(CloseSetOptionsMenu(entry.match.matchNumber, it))
                            }
                    )
                }

                Spacer(Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun DropdownMenu(
        allowNewEndsToBeAdded: Boolean,
        expanded: Boolean,
        testTag: CodexTestTag,
        itemClickedListener: (MatchDropdownMenuItem) -> Unit,
        dismissListener: () -> Unit,
) {
    DropdownMenu(
            expanded = expanded,
            onDismissRequest = dismissListener,
    ) {
        MatchDropdownMenuItem.entries.forEach { item ->
            if (!allowNewEndsToBeAdded && item == MatchDropdownMenuItem.CONTINUE) return@forEach

            DropdownMenuItem(
                    onClick = { itemClickedListener(item) },
                    modifier = Modifier.testTag(testTag)
            ) {
                Text(
                        text = item.title.get(),
                        style = CodexTypography.NORMAL,
                )
            }
        }
    }
}

enum class HeadToHeadScorePadTestTag : CodexTestTag {
    SCREEN,
    ADD_MATCH_BUTTON,
    ;

    override val screenName: String
        get() = "HEAD_TO_HEAD_SCORE_PAD"

    override fun getElement(): String = name
}

enum class HeadToHeadScorePadMatchTestTag {
    MATCH_TEXT,
    OPPONENT_TEXT,
    SIGHTERS,
    MAX_RANK,
    NO_ENDS,
    GRID,
    EDIT_MATCH_INFO_BUTTON,
    ADD_NEW_SET_BUTTON,
    MATCH_DROPDOWN_MENU_ITEM,
    ;

    fun getTestTag(matchNumber: Int) =
            object : CodexTestTag {
                override val screenName: String
                    get() = "HEAD_TO_HEAD_SCORE_PAD"

                override fun getElement(): String = "${name}_$matchNumber"
            }
}

@Preview(
        heightDp = 1500,
)
@Composable
fun HeadToHeadScorePadScreen_Preview() {
    CodexTheme {
        HeadToHeadScorePadScreen(
                HeadToHeadScorePadState(
                        entries = listOf(
                                FullHeadToHeadMatch(
                                        match = DatabaseHeadToHeadMatch(
                                                matchNumber = 1,
                                                heat = 1,
                                                opponent = "Jessica Summers",
                                                opponentQualificationRank = 1,
                                                shootId = 1,
                                                sightersCount = 0,
                                                isBye = false,
                                                isShootOffWin = false,
                                                maxPossibleRank = 3,
                                        ),
                                        isRecurveStyle = true,
                                        isStandardFormat = true,
                                        teamSize = 1,
                                        sets = listOf(),
                                ),
                                FullHeadToHeadMatch(
                                        match = DatabaseHeadToHeadMatch(
                                                matchNumber = 2,
                                                heat = 1,
                                                opponent = "Jessica Summ",
                                                opponentQualificationRank = 1,
                                                shootId = 1,
                                                sightersCount = 0,
                                                isBye = false,
                                                isShootOffWin = false,
                                                maxPossibleRank = null,
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
                                                FullHeadToHeadSet(
                                                        data = HeadToHeadGridRowDataPreviewHelper.create(),
                                                        teamSize = 1,
                                                        isShootOffWin = false,
                                                        setNumber = 3,
                                                        isRecurveStyle = true,
                                                ),
                                        ),
                                ),
                                FullHeadToHeadMatch(
                                        match = DatabaseHeadToHeadMatch(
                                                matchNumber = 3,
                                                heat = 1,
                                                opponent = null,
                                                opponentQualificationRank = null,
                                                shootId = 1,
                                                sightersCount = 0,
                                                isBye = false,
                                                isShootOffWin = false,
                                                maxPossibleRank = null,
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
