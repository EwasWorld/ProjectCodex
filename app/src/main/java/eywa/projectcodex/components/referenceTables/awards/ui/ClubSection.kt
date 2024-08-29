package eywa.projectcodex.components.referenceTables.awards.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.grid.CodexGridColumnMetadata
import eywa.projectcodex.common.sharedUi.grid.CodexGridRowMetadata
import eywa.projectcodex.common.sharedUi.grid.CodexGridWithHeaders
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.referenceTables.awards.AwardColor
import eywa.projectcodex.components.referenceTables.awards.AwardsState

private val club252AwardColours = listOf(
        AwardColor.ORANGE to 10,
        AwardColor.WHITE to 20,
        AwardColor.BLACK to 30,
        AwardColor.BLUE to 40,
        AwardColor.RED to 50,
        AwardColor.GOLD to 60,
        AwardColor.GREEN to 80,
        AwardColor.PURPLE to 100,
)

private val frostBiteAwardScores = listOf(200, 225, 250, 275, 300, 315, 330, 340, 350, 355)

@Composable
internal fun ClubSection(
        state: AwardsState,
        helpListener: (HelpShowcaseIntent) -> Unit,
        modifier: Modifier = Modifier,
) {
    Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
    ) {
        Text(
                text = stringResource(R.string.awards__club_section_title),
                style = CodexTypography.LARGE,
                color = CodexTheme.colors.onAppBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                        .align(Alignment.Start)
                        .padding(horizontal = CodexTheme.dimens.screenPadding)
        )
        Text(
                text = stringResource(R.string.awards__club_section_subtitle),
                style = CodexTypography.SMALL,
                color = CodexTheme.colors.onAppBackground,
                modifier = Modifier
                        .padding(horizontal = CodexTheme.dimens.screenPadding)
                        .align(Alignment.Start)
        )

        // Club 252
        Text(
                text = stringResource(R.string.awards__club_round_title, state.club252Row?.roundName ?: "Club 252"),
                style = CodexTypography.NORMAL_PLUS,
                color = CodexTheme.colors.onAppBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                        .align(Alignment.Start)
                        .padding(horizontal = CodexTheme.dimens.screenPadding)
                        .padding(top = 10.dp)
        )
        Club252Table(entry = state.club252Row, helpListener = helpListener)
        Text(
                text = stringResource(R.string.awards__club_252_disclaimer),
                style = CodexTypography.SMALL,
                color = CodexTheme.colors.onAppBackground,
                fontStyle = FontStyle.Italic,
                modifier = Modifier
                        .padding(horizontal = CodexTheme.dimens.screenPadding)
                        .align(Alignment.Start)
        )

        // Frostbite
        Text(
                text = stringResource(R.string.awards__club_round_title, state.frostbiteRow?.roundName ?: "Frostbite"),
                style = CodexTypography.NORMAL_PLUS,
                color = CodexTheme.colors.onAppBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                        .align(Alignment.Start)
                        .padding(horizontal = CodexTheme.dimens.screenPadding)
                        .padding(top = 10.dp)
        )
        FrostbiteTable(entry = state.frostbiteRow, helpListener = helpListener)
    }
}

@Composable
private fun Club252Table(
        entry: ClubAwardsRow?,
        helpListener: (HelpShowcaseIntent) -> Unit,
        modifier: Modifier = Modifier,
) {
    ProvideTextStyle(value = CodexTypography.NORMAL.copy(CodexTheme.colors.onListItemAppOnBackground)) {
        if (entry == null) {
            Surface(
                    shape = RoundedCornerShape(CodexTheme.dimens.smallCornerRounding),
                    color = CodexTheme.colors.listItemOnAppBackground,
                    modifier = modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
            ) {
                Text(
                        text = stringResource(R.string.classification_tables__no_tables),
                        modifier = Modifier
                                .testTag(AwardsTestTag.CLUB_TABLE_NO_DATA)
                                .padding(10.dp)
                )
            }
        }
        else {
            val scoreColumns = club252AwardColours
                    .mapIndexed { index, (_, distance) -> Club252AwardsColumn.Score(index, distance) }
            CodexGridWithHeaders(
                    data = listOf(entry),
                    columnMetadata = listOf(Club252AwardsColumn.Header).plus(scoreColumns),
                    extraData = Unit,
                    helpListener = helpListener,
                    modifier = modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = CodexTheme.dimens.screenPadding)
            )
        }
    }
}

@Composable
private fun FrostbiteTable(
        entry: ClubAwardsRow?,
        helpListener: (HelpShowcaseIntent) -> Unit,
        modifier: Modifier = Modifier,
) {
    ProvideTextStyle(value = CodexTypography.NORMAL.copy(CodexTheme.colors.onListItemAppOnBackground)) {
        if (entry == null) {
            Surface(
                    shape = RoundedCornerShape(CodexTheme.dimens.smallCornerRounding),
                    color = CodexTheme.colors.listItemOnAppBackground,
                    modifier = modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
            ) {
                Text(
                        text = stringResource(R.string.classification_tables__no_tables),
                        modifier = Modifier
                                .testTag(AwardsTestTag.CLUB_TABLE_NO_DATA)
                                .padding(10.dp)
                )
            }
        }
        else {
            CodexGridWithHeaders(
                    data = listOf(entry),
                    columnMetadata = listOf(FrostbiteAwardsColumn.Header)
                            .plus(List(frostBiteAwardScores.size) { FrostbiteAwardsColumn.Score(it) }),
                    extraData = Unit,
                    helpListener = helpListener,
                    modifier = modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = CodexTheme.dimens.screenPadding)
            )
        }
    }
}

data class ClubAwardsRow(
        val roundName: String,
        val scores: List<Int>
) : CodexGridRowMetadata {
    override val isTotalRow: Boolean
        get() = false
}

private sealed class Club252AwardsColumn : CodexGridColumnMetadata<ClubAwardsRow, Unit> {
    data object Header : Club252AwardsColumn() {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.awards__name_title)
        override val primaryTitleHorizontalSpan: Int
            get() = 1
        override val primaryTitleVerticalSpan: Int
            get() = 2
        override val secondaryTitle: ResOrActual<String>?
            get() = null
        override val helpTitle: ResOrActual<String>?
            get() = null
        override val helpBody: ResOrActual<String>?
            get() = null
        override val testTag: CodexTestTag
            get() = AwardsTestTag.CLUB_252_TABLE_AWARD_NAME
        override val mapping: (ClubAwardsRow) -> ResOrActual<String>
            get() = { ResOrActual.StringResource(R.string.awards__score_title) }
        override val cellContentDescription: (ClubAwardsRow, Unit) -> ResOrActual<String>?
            get() = { _, _ -> null }
    }

    data class Score(
            val typeIndex: Int,
            val distance: Int,
    ) : Club252AwardsColumn() {
        override val primaryTitle: ResOrActual<String>
            get() = club252AwardColours[typeIndex].first.title
        override val primaryTitleHorizontalSpan: Int
            get() = 1
        override val primaryTitleVerticalSpan: Int
            get() = 1
        override val secondaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.awards__club_252_distance_title, listOf(distance))
        override val helpTitle: ResOrActual<String>?
            get() = ResOrActual.StringResource(R.string.help_awards__score_title).takeIf { typeIndex == 0 }
        override val helpBody: ResOrActual<String>?
            get() = ResOrActual.StringResource(R.string.help_awards__score_body).takeIf { typeIndex == 0 }
        override val testTag: CodexTestTag
            get() = AwardsTestTag.CLUB_252_TABLE_SCORE
        override val mapping: (ClubAwardsRow) -> ResOrActual<String>
            get() = { ResOrActual.Actual(it.scores[typeIndex].toString()) }
        override val cellContentDescription: (ClubAwardsRow, Unit) -> ResOrActual<String>?
            get() = { entry, _ ->
                ResOrActual.StringResource(
                        R.string.awards__score_content_description,
                        listOf(mapping(entry), secondaryTitle),
                )
            }
    }
}

private sealed class FrostbiteAwardsColumn : CodexGridColumnMetadata<ClubAwardsRow, Unit> {
    data object Header : FrostbiteAwardsColumn() {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.awards__name_title)
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
        override val testTag: CodexTestTag
            get() = AwardsTestTag.CLUB_FROSTBITE_TABLE_AWARD_NAME
        override val mapping: (ClubAwardsRow) -> ResOrActual<String>
            get() = { ResOrActual.StringResource(R.string.awards__score_title) }
        override val cellContentDescription: (ClubAwardsRow, Unit) -> ResOrActual<String>?
            get() = { _, _ -> null }
    }

    data class Score(val levelIndex: Int) : FrostbiteAwardsColumn() {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.Actual(frostBiteAwardScores[levelIndex].toString())
        override val primaryTitleHorizontalSpan: Int
            get() = 1
        override val primaryTitleVerticalSpan: Int
            get() = 1
        override val secondaryTitle: ResOrActual<String>?
            get() = null
        override val helpTitle: ResOrActual<String>?
            get() = ResOrActual.StringResource(R.string.help_awards__score_title).takeIf { levelIndex == 0 }
        override val helpBody: ResOrActual<String>?
            get() = ResOrActual.StringResource(R.string.help_awards__score_body).takeIf { levelIndex == 0 }
        override val testTag: CodexTestTag
            get() = AwardsTestTag.CLUB_FROSTBITE_TABLE_SCORE
        override val mapping: (ClubAwardsRow) -> ResOrActual<String>
            get() = { ResOrActual.Actual(it.scores[levelIndex].toString()) }
        override val cellContentDescription: (ClubAwardsRow, Unit) -> ResOrActual<String>?
            get() = { entry, _ ->
                ResOrActual.StringResource(
                        R.string.awards__score_content_description,
                        listOf(mapping(entry), frostBiteAwardScores[levelIndex].toString()),
                )
            }
    }
}
