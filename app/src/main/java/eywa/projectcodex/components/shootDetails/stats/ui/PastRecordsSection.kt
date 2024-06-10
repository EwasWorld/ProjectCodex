package eywa.projectcodex.components.shootDetails.stats.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.CodexTabSwitcher
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIf
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.shootDetails.stats.StatsIntent
import eywa.projectcodex.components.shootDetails.stats.StatsScreenPastRecordsTabs
import eywa.projectcodex.components.shootDetails.stats.StatsState
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag
import eywa.projectcodex.database.shootData.DatabaseShootShortRecord

@Composable
internal fun PastRecordsSection(
        state: StatsState,
        listener: (StatsIntent) -> Unit,
) {
    if (state.pastRoundScores.isNullOrEmpty()) return
    val helpListener = { it: HelpShowcaseIntent -> listener(StatsIntent.HelpShowcaseAction(it)) }

    Section {
        Text(
                text = stringResource(R.string.archer_round_stats__past_records),
                style = CodexTypography.SMALL_PLUS.asClickableStyle(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                        .clickable { listener(StatsIntent.PastRoundRecordsClicked) }
                        .testTag(StatsTestTag.PAST_RECORDS_LINK_TEXT.getTestTag())
                        .updateHelpDialogPosition(
                                helpState = HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_archer_round_stats__past_records_title),
                                        helpBody = stringResource(R.string.help_archer_round_stats__past_records_body),
                                ).asHelpState(helpListener),
                        )
        )
    }

    SimpleDialog(
            isShown = state.isPastRoundRecordsDialogOpen,
            onDismissListener = { listener(StatsIntent.PastRoundRecordsDismissed) },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.archer_round_stats__past_records),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_close),
                        onClick = { listener(StatsIntent.PastRoundRecordsDismissed) },
                ),
        ) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(25.dp),
                    modifier = Modifier.fillMaxWidth()
            ) {
                CodexTabSwitcher(
                        items = StatsScreenPastRecordsTabs.values().toList(),
                        selectedItem = state.pastRoundScoresTab,
                        itemClickedListener = { listener(StatsIntent.PastRecordsTabClicked(it)) },
                        itemColor = CodexTheme.colors.tabSwitcherOnDialogSelected,
                        dividerColor = CodexTheme.colors.tabSwitcherOnDialogDivider,
                        modifier = Modifier.testTag(StatsTestTag.PAST_RECORDS_DIALOG_TAB)
                )

                Crossfade(
                        targetState = state.pastRoundScores,
                        label = "recentBestRecordsTextFade"
                ) { records ->
                    Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        records.forEach { PastScore(state, it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PastScore(
        state: StatsState,
        shootRecord: DatabaseShootShortRecord,
) {
    val delim = stringResource(R.string.archer_round_stats__past_record_item_delim).let { " $it " }

    val isPb = shootRecord.score == state.pastRoundScoresPb
    val isCurrentShoot = shootRecord.shootId == state.fullShootInfo.id

    val text = listOfNotNull(
            DateTimeFormat.SHORT_DATE.format(shootRecord.dateShot),
            shootRecord.score.toString(),
    )

    val background = when {
        isPb -> CodexTheme.colors.personalBestTag
        isCurrentShoot -> CodexTheme.colors.dialogBackgroundAccent
        else -> null
    }

    val extraSemanticTextIds = mutableListOf<Int>()
    if (isPb) {
        extraSemanticTextIds.add(
                if (state.pastRoundScoresPbIsTied) R.string.archer_round_stats__is_tied_pb
                else R.string.archer_round_stats__is_pb
        )
    }
    if (isCurrentShoot) {
        extraSemanticTextIds.add(R.string.archer_round_stats__past_records_current)
    }
    if (!shootRecord.isComplete) {
        extraSemanticTextIds.add(R.string.archer_round_stats__past_records_incomplete)
    }
    val extraSemanticText = extraSemanticTextIds.map { stringResource(it) }

    Text(
            text = text.joinToString(delim),
            style = CodexTypography.SMALL_PLUS.asClickableStyle(),
            color = CodexTheme.colors.onDialogBackground,
            textAlign = TextAlign.Center,
            textDecoration = (
                    if (shootRecord.isComplete) TextDecoration.None else TextDecoration.LineThrough
                    ),
            modifier = Modifier
                    .testTag(StatsTestTag.PAST_RECORDS_DIALOG_ITEM.getTestTag())
                    .modifierIf(
                            predicate = background != null,
                            modifier = Modifier
                                    .background(
                                            color = background ?: Color.Transparent,
                                            shape = RoundedCornerShape(100),
                                    )
                                    .padding(horizontal = 10.dp)
                    )
                    .semantics {
                        contentDescription = text
                                .plus(extraSemanticText)
                                .joinToString(delim)
                    }
    )
}
