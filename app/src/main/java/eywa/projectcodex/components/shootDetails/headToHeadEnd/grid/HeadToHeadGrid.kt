package eywa.projectcodex.components.shootDetails.headToHeadEnd.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIf
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.grid.CodexGrid
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult

private fun List<HeadToHeadGridRowData>.opponentEndTotal() =
        filter { it.type == HeadToHeadArcherType.OPPONENT_ARROW }.sumOf { it.totalScore() }

private fun List<HeadToHeadGridRowData>.teamEndTotal() =
        filter { it.type.isTeam }.sumOf { it.totalScore() }

private fun List<HeadToHeadGridRowData>.result(extraData: HeadToHeadSetData): HeadToHeadResult {
    val isComplete = all { it.isComplete(teamSize = extraData.teamSize, endSize = extraData.endSize) }
    if (!isComplete) return HeadToHeadResult.INCOMPLETE
    return when {
        extraData.teamEndTotal == extraData.opponentEndTotal -> HeadToHeadResult.TIE
        extraData.teamEndTotal > extraData.opponentEndTotal -> HeadToHeadResult.WIN
        else -> HeadToHeadResult.LOSS
    }
}

internal fun List<HeadToHeadGridRowData>.showExtraColumnTotal() =
        map { it.type }.let {
            it.contains(HeadToHeadArcherType.SELF_ARROW) && it.contains(HeadToHeadArcherType.TEAM_MATE_ARROW)
        }

@Composable
fun HeadToHeadAddEndGrid(
        state: HeadToHeadGridState,
        modifier: Modifier = Modifier,
        rowClicked: (setNumber: Int, type: HeadToHeadArcherType?) -> Unit,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    val resources = LocalContext.current.resources

    val data = state.enteredArrows
    val columnMetadata = listOfNotNull(
            HeadToHeadGridColumn.SET_NUMBER.takeIf { !state.isSingleEditableSet },
            HeadToHeadGridColumn.TYPE,
            HeadToHeadGridColumn.ARROWS.takeIf {
                state.enteredArrows.flatten().any { it is HeadToHeadGridRowData.Arrows }
            },
            HeadToHeadGridColumn.END_TOTAL,
            HeadToHeadGridColumn.TEAM_TOTAL.takeIf { state.showExtraTotalColumn },
            HeadToHeadGridColumn.POINTS.takeIf {
                !state.isSingleEditableSet
                        || state.enteredArrows.flatten().any { it.type == HeadToHeadArcherType.TEAM_POINTS }
            },
    )

    CodexGrid(
            columns = columnMetadata.size,
            alignment = Alignment.Center,
            verticalSpacing = 4.dp,
            horizontalSpacing = 4.dp,
            modifier = modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = CodexTheme.dimens.screenPadding)
    ) {
        columnMetadata.forEach {
            item(fillBox = true) {
                if (it.primaryTitle == null) {
                    Box {}
                }
                else {
                    Text(
                            text = it.primaryTitle!!.get(),
                            style = CodexTypography.NORMAL_PLUS,
                            color = CodexTheme.colors.onListItemAppOnBackground,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                    .background(CodexTheme.colors.listAccentRowItemOnAppBackground)
                                    .padding(vertical = 5.dp, horizontal = 10.dp)
                                    .wrapContentHeight(Alignment.CenterVertically)
                    )
                }
            }
        }

        val selectedShape = RoundedCornerShape(10.dp)
        data.forEachIndexed { index, set ->
            val isShootOff = index == data.lastIndex && state.hasShootOff
            val extraData = HeadToHeadSetData(
                    isShootOff = isShootOff,
                    endSize = if (isShootOff) 1 else state.endSize,
                    teamSize = state.teamSize,
                    teamEndTotal = set.teamEndTotal(),
                    opponentEndTotal = set.opponentEndTotal(),
                    hasSelfAndTeamRows = set.showExtraColumnTotal(),
                    result = HeadToHeadResult.INCOMPLETE,
            ).let { extra ->
                extra.copy(
                        result = set.result(extra).takeIf { !isShootOff || it != HeadToHeadResult.TIE }
                                ?: if (state.isShootOffWin) HeadToHeadResult.WIN else HeadToHeadResult.LOSS,
                )
            }

            if (!state.isSingleEditableSet) {
                item(
                        fillBox = true,
                        verticalSpan = set.size + 1,
                ) {
                    Text(
                            text = (index + 1).toString(),
                            style = CodexTypography.NORMAL_PLUS,
                            color = CodexTheme.colors.onListItemAppOnBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                    .background(CodexTheme.colors.listAccentRowItemOnAppBackground)
                                    .padding(vertical = 5.dp, horizontal = 10.dp)
                                    .wrapContentHeight(Alignment.CenterVertically)
                    )
                }
            }

            set.sortedBy { it.type.ordinal }.forEach { row ->
                columnMetadata.forEach { column ->
                    val cellModifier = column.testTag?.let { Modifier.testTag(it) } ?: Modifier
                    val value = column.mapping(row, extraData)?.get(resources)
                    val isSelectable = state.isSingleEditableSet && (
                            (row is HeadToHeadGridRowData.Total && column == HeadToHeadGridColumn.END_TOTAL)
                                    || (row !is HeadToHeadGridRowData.Total && column == HeadToHeadGridColumn.ARROWS)
                            )
                    val isSelected = isSelectable && row.type == state.selected

                    if (value != null) {
                        item(
                                fillBox = true,
                                verticalSpan = column.cellVerticalSpan(row, extraData),
                        ) {
                            val background =
                                    if (isSelected) Color.White
                                    else if (isSelectable) CodexTheme.colors.listAccentRowItemOnAppBackground
                                    else CodexTheme.colors.listItemOnAppBackground
                            val backgroundShape = if (isSelectable) selectedShape else RectangleShape
                            val borderColor =
                                    if (isSelected) CodexColors.COLOR_PRIMARY_DARK
                                    else if (isSelectable) CodexColors.COLOR_ON_PRIMARY_LIGHT
                                    else CodexTheme.colors.listItemOnAppBackground

                            val horizontalPadding = if (column == HeadToHeadGridColumn.TYPE) 8.dp else 15.dp

                            Text(
                                    text = value,
                                    style = CodexTypography.NORMAL_PLUS,
                                    color = CodexTheme.colors.onListItemAppOnBackground,
                                    textAlign = TextAlign.Center,
                                    modifier = cellModifier
                                            .modifierIf(
                                                    isSelectable,
                                                    Modifier.border(2.dp, borderColor, selectedShape),
                                            )
                                            .background(background, backgroundShape)
                                            .padding(horizontal = horizontalPadding, vertical = 3.dp)
                                            .wrapContentHeight(Alignment.CenterVertically)
                                            .semantics {
                                                column
                                                        .cellContentDescription(row, extraData)
                                                        ?.get(resources)
                                                        ?.let {
                                                            contentDescription = it
                                                        }
                                            }
                            )
                        }
                    }
                }
            }

            if (!state.isSingleEditableSet) {
                item(
                        fillBox = true,
                        // Ignore set number column
                        horizontalSpan = columnMetadata.size - 1,
                ) {
                    Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                    .background(CodexTheme.colors.listAccentRowItemOnAppBackground)
                                    .wrapContentHeight(Alignment.CenterVertically)
                    ) {
                        Text(
                                text = "Result: ${extraData.result.title.get()}",
                                style = CodexTypography.NORMAL_PLUS,
                                color = CodexTheme.colors.onListItemAppOnBackground,
                                textAlign = TextAlign.End,
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp)
                        )
                        Text(
                                text = "R/T: 0-2",
                                style = CodexTypography.NORMAL_PLUS,
                                color = CodexTheme.colors.onListItemAppOnBackground,
                                textAlign = TextAlign.End,
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp)
                        )
                    }
                }
            }
        }

        if (state.isSingleEditableSet) {
            item(
                    fillBox = true,
                    horizontalSpan = columnMetadata.size,
            ) {
                Text(
                        text = "+ Add",
                        style = CodexTypography.NORMAL_PLUS,
                        color = CodexTheme.colors.onListItemAppOnBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                                .background(CodexTheme.colors.listItemOnAppBackground)
                                .padding(vertical = 5.dp, horizontal = 10.dp)
                                .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Input_HeadToHeadGrid_Preview() {
    CodexTheme {
        HeadToHeadAddEndGrid(
                state = HeadToHeadGridState(
                        enteredArrows = listOf(HeadToHeadGridRowDataPreviewHelper.create()),
                        endSize = 3,
                        teamSize = 1,
                        selected = null,
                        isSingleEditableSet = true,
                        hasShootOff = false,
                        isShootOffWin = false,
                ),
                rowClicked = { _, _ -> },
                helpListener = {},
                modifier = Modifier.padding(vertical = 20.dp)
        )
    }
}

@Preview(
        widthDp = 500,
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun InputTeam_HeadToHeadGrid_Preview() {
    CodexTheme {
        HeadToHeadAddEndGrid(
                state = HeadToHeadGridState(
                        enteredArrows = listOf(
                                HeadToHeadGridRowDataPreviewHelper.create(
                                        teamSize = 2,
                                        typesToIsTotal = mapOf(
                                                HeadToHeadArcherType.SELF_ARROW to false,
                                                HeadToHeadArcherType.TEAM_MATE_ARROW to false,
                                                HeadToHeadArcherType.OPPONENT_ARROW to true,
                                        ),
                                ),
                        ),
                        endSize = 3,
                        teamSize = 2,
                        selected = null,
                        isSingleEditableSet = true,
                        hasShootOff = false,
                        isShootOffWin = false,
                ),
                rowClicked = { _, _ -> },
                helpListener = {},
                modifier = Modifier.padding(vertical = 20.dp)
        )
    }
}

@Preview(
        widthDp = 500,
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun ScorePad_HeadToHeadGrid_Preview() {
    CodexTheme {
        HeadToHeadAddEndGrid(
                state = HeadToHeadGridState(
                        enteredArrows = listOf(
                                HeadToHeadGridRowDataPreviewHelper.create(),
                                HeadToHeadGridRowDataPreviewHelper.create(),
                                HeadToHeadGridRowDataPreviewHelper.create(isShootOff = true),
                        ),
                        endSize = 3,
                        teamSize = 1,
                        selected = null,
                        isSingleEditableSet = false,
                        hasShootOff = true,
                        isShootOffWin = true,
                ),
                rowClicked = { _, _ -> },
                helpListener = {},
                modifier = Modifier.padding(vertical = 20.dp)
        )
    }
}
