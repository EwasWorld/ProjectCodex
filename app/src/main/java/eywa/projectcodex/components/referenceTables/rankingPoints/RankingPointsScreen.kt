package eywa.projectcodex.components.referenceTables.rankingPoints

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.grid.CodexGridColumnMetadata
import eywa.projectcodex.common.sharedUi.grid.CodexGridWithHeaders
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.asDecimalFormat

data class RankingPointsState(
        val useCase: RankingPointsUseCase,
)

sealed class RankingPointsIntent {
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : RankingPointsIntent()
}

@Composable
fun RankingPointsScreen(
        viewModel: RankingPointsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    RankingPointsScreen(state) { viewModel.handleEvent(it) }
}

@Composable
fun RankingPointsScreen(
        state: RankingPointsState,
        listener: (RankingPointsIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(RankingPointsIntent.HelpShowcaseAction(it)) }

    ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        Column(
                verticalArrangement = Arrangement.spacedBy(25.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .fillMaxSize()
                        .background(CodexTheme.colors.appBackground)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = CodexTheme.dimens.screenPadding)
                        .testTag(RankingPointsTestTag.SCREEN)
        ) {
            Text(
                    text = stringResource(R.string.ranking_points__subtitle),
                    style = CodexTypography.SMALL,
                    color = CodexTheme.colors.onAppBackground,
                    modifier = Modifier
                            .padding(horizontal = CodexTheme.dimens.screenPadding)
                            .align(Alignment.Start)
            )
            CodexGridWithHeaders(
                    data = state.useCase.data,
                    columnMetadata = RankingPointsColumn.entries,
                    extraData = Unit,
                    helpListener = helpListener,
                    modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = CodexTheme.dimens.screenPadding)
            )
            Text(
                    text = stringResource(R.string.ranking_points__tiers),
                    style = CodexTypography.SMALL,
                    color = CodexTheme.colors.onAppBackground,
                    modifier = Modifier
                            .padding(horizontal = CodexTheme.dimens.screenPadding)
                            .align(Alignment.Start)
            )
        }
    }
}

enum class RankingPointsColumn : CodexGridColumnMetadata<RankingPointsEntry, Unit> {
    RANK {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.ranking_points__rank_title)
        override val secondaryTitle: ResOrActual<String>?
            get() = null
        override val mapping: (RankingPointsEntry) -> ResOrActual<String>
            get() = { ResOrActual.Actual(it.rank.toString()) }

        override fun isTotal(): Boolean = true
    },
    TIER_ONE_RANKING_ROUND {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.ranking_points__tier_1_title)
        override val primaryTitleHorizontalSpan: Int
            get() = 2
        override val secondaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.ranking_points__ranking_round_title)
        override val mapping: (RankingPointsEntry) -> ResOrActual<String>
            get() = { it.tierOneRankingRound.asDecimalFormat(2) }
    },
    TIER_ONE_H2H {
        override val primaryTitle: ResOrActual<String>?
            get() = null
        override val secondaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.ranking_points__h2h_title)
        override val mapping: (RankingPointsEntry) -> ResOrActual<String>
            get() = { it.tierOneH2h.asDecimalFormat(2) }
    },
    TIER_TWO_PLUS {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.ranking_points__tier_2_plus_title)
        override val secondaryTitle: ResOrActual<String>?
            get() = null
        override val mapping: (RankingPointsEntry) -> ResOrActual<String>
            get() = { it.tierTwoPlus.asDecimalFormat(2) }

        override fun isAccentColor(): Boolean = true
    },
    TIER_TWO_RANKING_ROUND {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.ranking_points__tier_2_title)
        override val primaryTitleHorizontalSpan: Int
            get() = 2
        override val secondaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.ranking_points__ranking_round_title)
        override val mapping: (RankingPointsEntry) -> ResOrActual<String>
            get() = { it.tierTwoRankingRound.asDecimalFormat(2) }
    },
    TIER_TWO_H2H {
        override val primaryTitle: ResOrActual<String>?
            get() = null
        override val secondaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.ranking_points__h2h_title)
        override val mapping: (RankingPointsEntry) -> ResOrActual<String>
            get() = { it.tierTwoH2h.asDecimalFormat(2) }
    },
    TIER_THREE {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.ranking_points__tier_3_title)
        override val secondaryTitle: ResOrActual<String>?
            get() = null
        override val mapping: (RankingPointsEntry) -> ResOrActual<String>
            get() = { it.tierThree.asDecimalFormat(2) }

        override fun isAccentColor(): Boolean = true
    },
    ;

    override val primaryTitleHorizontalSpan: Int
        get() = 1
    override val primaryTitleVerticalSpan: Int
        get() = if (secondaryTitle == null) 2 else 1
    override val helpTitle: ResOrActual<String>?
        get() = null
    override val helpBody: ResOrActual<String>?
        get() = null
    override val testTag: CodexTestTag?
        get() = null
    override val cellContentDescription: (RankingPointsEntry, Unit) -> ResOrActual<String>?
        get() = { entry, _ ->
            ResOrActual.StringResource(
                    R.string.ranking_points__content_description,
                    listOf(mapping(entry)!!, getPrimaryTitleNotNull(), entry.rank),
            )
        }

    private fun getPrimaryTitleNotNull() =
            primaryTitle ?: RankingPointsColumn.entries[ordinal - 1].primaryTitle!!
}

enum class RankingPointsTestTag : CodexTestTag {
    SCREEN,
    ;

    override val screenName: String
        get() = "RANKING_POINTS"

    override fun getElement(): String = name
}
