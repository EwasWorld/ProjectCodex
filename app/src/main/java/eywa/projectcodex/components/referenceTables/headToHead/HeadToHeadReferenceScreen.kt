package eywa.projectcodex.components.referenceTables.headToHead

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.grid.CodexGridColumnMetadata
import eywa.projectcodex.common.sharedUi.grid.CodexGridRowMetadata
import eywa.projectcodex.common.sharedUi.grid.CodexGridWithHeaders
import eywa.projectcodex.common.sharedUi.numberField.CodexLabelledNumberFieldWithErrorMessage
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual

@Composable
fun HeadToHeadReferenceScreen(
        viewModel: HeadToHeadReferenceViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    HeadToHeadReferenceScreen(state) { viewModel.handleEvent(it) }
}

@Composable
fun HeadToHeadReferenceScreen(
        state: HeadToHeadReferenceState,
        listener: (HeadToHeadReferenceIntent) -> Unit,
) {
    val helpListener: (HelpShowcaseIntent) -> Unit = { listener(HeadToHeadReferenceIntent.HelpShowcaseAction(it)) }

    ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        Column(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .fillMaxSize()
                        .background(CodexTheme.colors.appBackground)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = CodexTheme.dimens.screenPadding)
                        .testTag(HeadToHeadReferenceTestTag.SCREEN)
        ) {
            CodexLabelledNumberFieldWithErrorMessage(
                    title = stringResource(R.string.head_to_head_ref__archer_a),
                    currentValue = state.archerRank.text,
                    fieldTestTag = HeadToHeadReferenceTestTag.ARCHER_RANK,
                    errorMessageTestTag = HeadToHeadReferenceTestTag.ARCHER_RANK_ERROR,
                    placeholder = stringResource(R.string.head_to_head_ref__archer_a_placeholder),
                    errorMessage = state.archerRank.error,
                    helpState = HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_head_to_head_ref__archer_a_title),
                            helpBody = stringResource(R.string.help_head_to_head_ref__archer_a_body),
                    ).asHelpState(helpListener),
                    onValueChanged = { listener(HeadToHeadReferenceIntent.ArcherRankChanged(it)) },
            )
            CodexLabelledNumberFieldWithErrorMessage(
                    title = stringResource(R.string.head_to_head_ref__archer_b),
                    currentValue = state.opponentRank.text,
                    fieldTestTag = HeadToHeadReferenceTestTag.OPPONENT_RANK,
                    errorMessageTestTag = HeadToHeadReferenceTestTag.OPPONENT_RANK_ERROR,
                    placeholder = stringResource(R.string.head_to_head_ref__archer_b_placeholder),
                    errorMessage = state.opponentRank.error,
                    helpState = HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_head_to_head_ref__archer_b_title),
                            helpBody = stringResource(R.string.help_head_to_head_ref__archer_b_body),
                    ).asHelpState(helpListener),
                    onValueChanged = { listener(HeadToHeadReferenceIntent.OpponentRankChanged(it)) },
            )
            CodexLabelledNumberFieldWithErrorMessage(
                    title = stringResource(R.string.head_to_head_ref__total_archers),
                    currentValue = state.totalArchersFull.text,
                    fieldTestTag = HeadToHeadReferenceTestTag.TOTAL_ARCHERS,
                    errorMessageTestTag = HeadToHeadReferenceTestTag.TOTAL_ARCHERS_ERROR,
                    placeholder = stringResource(R.string.head_to_head_ref__total_archers_placeholder),
                    errorMessage = state.totalArchersFull.error,
                    helpState = HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_head_to_head_ref__total_archers_title),
                            helpBody = stringResource(R.string.help_head_to_head_ref__total_archers_body),
                    ).asHelpState(helpListener),
                    onValueChanged = { listener(HeadToHeadReferenceIntent.TotalArchersChanged(it)) },
            )

            Divider(
                    color = CodexTheme.colors.onAppBackground,
                    modifier = Modifier.padding(CodexTheme.dimens.screenPadding)
            )

            state.meetIn?.let {
                val roundName = it.get()
                val string = buildAnnotatedString {
                    val style = SpanStyle(
                            fontSize = CodexTypography.NORMAL_PLUS.fontSize,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                    )


                    append(
                            stringResource(
                                    R.string.head_to_head_ref__meet_in_start,
                                    state.archerRank.parsed!!,
                                    state.opponentRank.parsed!!,
                            ),
                    )
                    withStyle(style) { append(roundName) }
                    append(stringResource(R.string.head_to_head_ref__meet_in_end))
                }
                Text(
                        text = string,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                                .padding(horizontal = CodexTheme.dimens.screenPadding)
                                .padding(bottom = CodexTheme.dimens.screenPadding)
                                .updateHelpDialogPosition(
                                        HelpShowcaseItem(
                                                helpTitle = stringResource(R.string.help_head_to_head_ref__meet_in_title),
                                                helpBody = stringResource(R.string.help_head_to_head_ref__meet_in_body),
                                        ).asHelpState(helpListener),
                                )
                                .testTag(HeadToHeadReferenceTestTag.MEET_IN)
                )
            }

            state.tableData?.let { data ->
                Text(
                        text = stringResource(R.string.help_head_to_head_ref__opponents_table_heading),
                        style = CodexTypography.NORMAL_PLUS.copy(color = CodexTheme.colors.onAppBackground),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                                .padding(horizontal = CodexTheme.dimens.screenPadding)
                                .align(Alignment.Start)
                )

                val totalRounds = state.totalRounds ?: 3
                CodexGridWithHeaders(
                        data = data,
                        columnMetadata = listOf(HeadToHeadReferenceColumn.Rank)
                                .plus(
                                        List(totalRounds) {
                                            HeadToHeadReferenceColumn.Round(totalRounds - it - 1, totalRounds)
                                        },
                                ),
                        extraData = Unit,
                        helpListener = helpListener,
                        modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = CodexTheme.dimens.screenPadding)
                                .updateHelpDialogPosition(
                                        HelpShowcaseItem(
                                                helpTitle = stringResource(R.string.help_head_to_head_ref__opponents_table_title),
                                                helpBody = stringResource(R.string.help_head_to_head_ref__opponents_table_body),
                                        ).asHelpState(helpListener),
                                )
                )
            }
        }
    }
}

data class HeadToHeadReferenceRow(val rank: Int, val opponents: List<Int?>) : CodexGridRowMetadata

sealed class HeadToHeadReferenceColumn : CodexGridColumnMetadata<HeadToHeadReferenceRow, Unit> {
    data object Rank : HeadToHeadReferenceColumn() {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.head_to_head_ref__rank)
        override val testTag: CodexTestTag
            get() = HeadToHeadReferenceTestTag.TABLE_RANK
        override val mapping: (HeadToHeadReferenceRow) -> ResOrActual<String>
            get() = { ResOrActual.Actual(it.rank.toString()) }
        override val cellContentDescription: (HeadToHeadReferenceRow, Unit) -> ResOrActual<String>?
            get() = { entry, _ ->
                ResOrActual.StringResource(R.string.head_to_head_ref__rank_content_desc, listOf(entry.rank))
            }

        override fun isTotal(): Boolean = true
    }

    data class Round(val round: Int, val totalRounds: Int) : HeadToHeadReferenceColumn() {
        override val primaryTitle: ResOrActual<String>
            get() = HeadToHeadUseCase.shortRoundName(round)
        override val testTag: CodexTestTag
            get() = HeadToHeadReferenceTestTag.TABLE_ROUND
        override val mapping: (HeadToHeadReferenceRow) -> ResOrActual<String>
            get() = { entry ->
                entry.opponents[round]?.let { ResOrActual.Actual(it.toString()) }
                        ?: ResOrActual.StringResource(R.string.head_to_head_ref__bye)
            }
        override val cellContentDescription: (HeadToHeadReferenceRow, Unit) -> ResOrActual<String>?
            get() = { entry, _ ->
                ResOrActual.StringResource(
                        R.string.head_to_head_ref__round_content_desc,
                        listOf(
                                mapping(entry),
                                HeadToHeadUseCase.roundName(round),
                                entry.rank.toString(),
                        ),
                )
            }
    }

    override val primaryTitleHorizontalSpan: Int
        get() = 1
    override val primaryTitleVerticalSpan: Int
        get() = 1
    override val secondaryTitle: ResOrActual<String>?
        get() = null
    override val helpTitle: ResOrActual<String>?
        get() = null
    override val helpBody: ResOrActual<String>?
        get() = null
}

enum class HeadToHeadReferenceTestTag : CodexTestTag {
    SCREEN,

    ARCHER_RANK,
    ARCHER_RANK_ERROR,
    OPPONENT_RANK,
    OPPONENT_RANK_ERROR,
    TOTAL_ARCHERS,
    TOTAL_ARCHERS_ERROR,
    MEET_IN,
    TABLE_RANK,
    TABLE_ROUND,
    ;

    override val screenName: String
        get() = "HEAD_TO_HEAD_REF"

    override fun getElement(): String = name
}

@Preview
@Composable
fun HeadToHeadReferenceScreen_Preview() {
    val state = HeadToHeadReferenceState()
    CodexTheme {
        HeadToHeadReferenceScreen(
                state.copy(
                        archerRank = state.archerRank.onTextChanged("5"),
                        opponentRank = state.opponentRank.onTextChanged("17"),
                        totalArchers = state.totalArchers.onTextChanged("20"),
                ),
        ) {}
    }
}

@Preview
@Composable
fun Error_HeadToHeadReferenceScreen_Preview() {
    val state = HeadToHeadReferenceState()
    CodexTheme {
        HeadToHeadReferenceScreen(
                state.copy(
                        archerRank = state.archerRank.onTextChanged("-5"),
                        totalArchers = state.totalArchers.onTextChanged("0"),
                ),
        ) {}
    }
}

@Preview
@Composable
fun Empty_HeadToHeadReferenceScreen_Preview() {
    val state = HeadToHeadReferenceState()
    CodexTheme {
        HeadToHeadReferenceScreen(
                state,
        ) {}
    }
}

@Preview
@Composable
fun NoTotal_HeadToHeadReferenceScreen_Preview() {
    val state = HeadToHeadReferenceState()
    CodexTheme {
        HeadToHeadReferenceScreen(
                state.copy(
                        archerRank = state.archerRank.onTextChanged("5"),
                        opponentRank = state.opponentRank.onTextChanged("17"),
                ),
        ) {}
    }
}

@Preview
@Composable
fun NoOpponent_HeadToHeadReferenceScreen_Preview() {
    val state = HeadToHeadReferenceState()
    CodexTheme {
        HeadToHeadReferenceScreen(
                state.copy(
                        archerRank = state.archerRank.onTextChanged("5"),
                ),
        ) {}
    }
}

@Preview
@Composable
fun NoArcher_HeadToHeadReferenceScreen_Preview() {
    val state = HeadToHeadReferenceState()
    CodexTheme {
        HeadToHeadReferenceScreen(
                state.copy(
                        opponentRank = state.opponentRank.onTextChanged("5"),
                ),
        ) {}
    }
}
