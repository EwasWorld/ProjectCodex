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

private val agbAwardColours = listOf(
        AwardColor.WHITE, AwardColor.BLACK, AwardColor.BLUE, AwardColor.RED, AwardColor.GOLD, AwardColor.PURPLE,
)

@Composable
internal fun AgbSection(
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
                text = stringResource(R.string.awards__agb_section_title),
                style = CodexTypography.LARGE,
                color = CodexTheme.colors.onAppBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                        .align(Alignment.Start)
                        .padding(horizontal = CodexTheme.dimens.screenPadding)
        )
        Text(
                text = stringResource(R.string.awards__agb_section_subtitle),
                style = CodexTypography.SMALL,
                color = CodexTheme.colors.onAppBackground,
                modifier = Modifier
                        .padding(horizontal = CodexTheme.dimens.screenPadding)
                        .align(Alignment.Start)
        )

        Table(
                entries = state.agbRows,
                helpListener = helpListener,
                modifier = Modifier.padding(vertical = 10.dp)
        )
        Text(
                text = stringResource(R.string.awards__agb_disclaimer),
                style = CodexTypography.SMALL,
                color = CodexTheme.colors.onAppBackground,
                fontStyle = FontStyle.Italic,
                modifier = Modifier
                        .padding(horizontal = CodexTheme.dimens.screenPadding)
                        .align(Alignment.Start)
        )
    }
}

@Composable
private fun Table(
        entries: List<AgbAwardsRow>?,
        helpListener: (HelpShowcaseIntent) -> Unit,
        modifier: Modifier = Modifier,
) {
    ProvideTextStyle(value = CodexTypography.NORMAL.copy(CodexTheme.colors.onListItemAppOnBackground)) {
        if (entries.isNullOrEmpty()) {
            Surface(
                    shape = RoundedCornerShape(CodexTheme.dimens.smallCornerRounding),
                    color = CodexTheme.colors.listItemOnAppBackground,
                    modifier = modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
            ) {
                Text(
                        text = stringResource(R.string.classification_tables__no_tables),
                        modifier = Modifier
                                .testTag(AwardsTestTag.AGB_TABLE_NO_DATA)
                                .padding(10.dp)
                )
            }
        }
        else {
            CodexGridWithHeaders(
                    data = entries,
                    columnMetadata = listOf(AgbAwardsColumn.AwardName, AgbAwardsColumn.Round)
                            .plus(List(agbAwardColours.size) { AgbAwardsColumn.Score(it) }),
                    extraData = Unit,
                    helpListener = helpListener,
                    modifier = modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = CodexTheme.dimens.screenPadding)
            )
        }
    }
}

data class AgbAwardsRow(
        val awardName: String,
        val roundName: String,
        val scores: List<Int>,
) : CodexGridRowMetadata {
    override val isTotalRow: Boolean
        get() = false
}

private sealed class AgbAwardsColumn : CodexGridColumnMetadata<AgbAwardsRow, Unit> {
    data object AwardName : AgbAwardsColumn() {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.awards__name_title)
        override val primaryTitleHorizontalSpan: Int
            get() = 1
        override val primaryTitleVerticalSpan: Int
            get() = 2
        override val secondaryTitle: ResOrActual<String>?
            get() = null
        override val helpTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.help_awards__name_title)
        override val helpBody: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.help_awards__name_body)
        override val testTag: CodexTestTag
            get() = AwardsTestTag.AGB_TABLE_AWARD_NAME
        override val mapping: (AgbAwardsRow) -> ResOrActual<String>
            get() = { ResOrActual.Actual(it.awardName) }
        override val cellContentDescription: (AgbAwardsRow, Unit) -> ResOrActual<String>?
            get() = { entry, _ -> mapping(entry) }
    }

    data object Round : AgbAwardsColumn() {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.awards__round_title)
        override val primaryTitleHorizontalSpan: Int
            get() = 1
        override val primaryTitleVerticalSpan: Int
            get() = 2
        override val secondaryTitle: ResOrActual<String>?
            get() = null
        override val helpTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.help_awards__round_title)
        override val helpBody: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.help_awards__round_body)
        override val testTag: CodexTestTag
            get() = AwardsTestTag.AGB_TABLE_ROUND
        override val mapping: (AgbAwardsRow) -> ResOrActual<String>
            get() = { ResOrActual.Actual(it.roundName) }
        override val cellContentDescription: (AgbAwardsRow, Unit) -> ResOrActual<String>?
            get() = { entry, _ -> mapping(entry) }
    }

    data class Score(val typeIndex: Int) : AgbAwardsColumn() {
        override val primaryTitle: ResOrActual<String>?
            get() = ResOrActual.StringResource(R.string.awards__score_title).takeIf { typeIndex == 0 }
        override val primaryTitleHorizontalSpan: Int
            get() = agbAwardColours.size
        override val primaryTitleVerticalSpan: Int
            get() = 1
        override val secondaryTitle: ResOrActual<String>
            get() = agbAwardColours[typeIndex].title
        override val helpTitle: ResOrActual<String>?
            get() = ResOrActual.StringResource(R.string.help_awards__score_title).takeIf { typeIndex == 0 }
        override val helpBody: ResOrActual<String>?
            get() = ResOrActual.StringResource(R.string.help_awards__score_body).takeIf { typeIndex == 0 }
        override val testTag: CodexTestTag
            get() = AwardsTestTag.AGB_TABLE_SCORE
        override val mapping: (AgbAwardsRow) -> ResOrActual<String>
            get() = { ResOrActual.Actual(it.scores[typeIndex].toString()) }
        override val cellContentDescription: (AgbAwardsRow, Unit) -> ResOrActual<String>?
            get() = { entry, _ ->
                ResOrActual.StringResource(
                        R.string.awards__score_content_description,
                        listOf(mapping(entry), secondaryTitle),
                )
            }
    }
}
