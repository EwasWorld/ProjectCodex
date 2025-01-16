package eywa.projectcodex.components.shootDetails.headToHeadEnd.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.logging.debugLog
import eywa.projectcodex.common.sharedUi.CodexTextField
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIf
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIfNotNull
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.grid.CodexGrid
import eywa.projectcodex.common.sharedUi.numberField.CodexNumberField
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridColumnTestTag.*
import eywa.projectcodex.model.Either
import eywa.projectcodex.model.headToHead.FullHeadToHeadSet

fun List<FullHeadToHeadSet>.anyRow(predicate: (HeadToHeadGridRowData) -> Boolean) =
        any { set -> set.data.any { predicate(it) } }

@Composable
fun HeadToHeadGrid(
        state: HeadToHeadGridState,
        errorOnIncompleteRows: Boolean,
        modifier: Modifier = Modifier,
        rowClicked: (setNumber: Int, type: HeadToHeadArcherType) -> Unit,
        editTypesClicked: () -> Unit,
        onTextValueChanged: (type: HeadToHeadArcherType, text: String?) -> Unit,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    debugLog("Grid")
    val resources = LocalContext.current.resources

    val focusRequesters = remember(
            state is HeadToHeadGridState.SingleEditable,
            state.enteredArrows.firstOrNull()?.data?.map { it.type },
    ) {
        if (state !is HeadToHeadGridState.SingleEditable) null
        else state.enteredArrows.firstOrNull()?.data
                ?.filter { it.isTotalRow && it.type != HeadToHeadArcherType.RESULT }
                ?.associate { it.type to FocusRequester() }
    }

    val columnMetadata = listOfNotNull(
            HeadToHeadGridColumn.SET_NUMBER.takeIf { state !is HeadToHeadGridState.SingleEditable },
            HeadToHeadGridColumn.TYPE,
            HeadToHeadGridColumn.ARROWS.takeIf {
                state.enteredArrows.anyRow { it is HeadToHeadGridRowData.Arrows }
            },
            HeadToHeadGridColumn.END_TOTAL,
            HeadToHeadGridColumn.TEAM_TOTAL.takeIf { state.showExtraTotalColumn },
            HeadToHeadGridColumn.POINTS.takeIf { state !is HeadToHeadGridState.SingleEditable },
    )

    val style =
            if (state is HeadToHeadGridState.SingleEditable) CodexTypography.NORMAL_PLUS else CodexTypography.NORMAL
    ProvideTextStyle(style.copy(color = CodexTheme.colors.onListItemAppOnBackground)) {
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
                        val clickable = it == HeadToHeadGridColumn.TYPE && state is HeadToHeadGridState.SingleEditable
                        Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                        .background(CodexTheme.colors.listAccentRowItemOnAppBackground)
                                        .padding(vertical = 5.dp, horizontal = 10.dp)
                                        .wrapContentHeight(Alignment.CenterVertically)
                                        .modifierIf(
                                                clickable,
                                                Modifier
                                                        .clickable { editTypesClicked() }
                                                        .testTag(HeadToHeadGridTestTag.EDIT_ROWS_BUTTON),
                                        )
                        ) {
                            Text(
                                    text = it.primaryTitle!!.get(),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                            )
                            if (clickable) {
                                Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = CodexTheme.colors.onListItemAppOnBackground,
                                )
                            }
                        }
                    }
                }
            }

            val selectedShape = RoundedCornerShape(10.dp)
            state.enteredArrows.forEachIndexed { setIndex, set ->
                fun HeadToHeadGridColumnTestTag.get() =
                        if (state is HeadToHeadGridState.SingleEditable) get(1, 1)
                        else get(state.matchNumber, set.setNumber)

                val extraData = HeadToHeadSetData(
                        isShootOff = set.isShootOff,
                        teamEndTotal = set.teamEndScore ?: 0,
                        opponentEndTotal = set.opponentEndScore ?: 0,
                        hasSelfAndTeamRows = set.showExtraColumnTotal(),
                        teamTotalColumnSpan = setOf(
                                HeadToHeadArcherType.SELF,
                                HeadToHeadArcherType.TEAM_MATE,
                                HeadToHeadArcherType.TEAM
                        ).intersect(set.data.map { it.type }.toSet()).size,
                        resultColumnSpan = setOf(
                                HeadToHeadGridColumn.ARROWS,
                                HeadToHeadGridColumn.END_TOTAL,
                                HeadToHeadGridColumn.TEAM_TOTAL,
                        ).intersect(columnMetadata.toSet()).size,
                        result = set.result,
                )

                if (state is HeadToHeadGridState.NonEditable) {
                    item(
                            fillBox = true,
                            // + 1 for total row
                            verticalSpan = set.data.size + 1,
                    ) {
                        Text(
                                text = (setIndex + 1).toString(),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                        .background(CodexTheme.colors.listAccentRowItemOnAppBackground)
                                        .padding(vertical = 5.dp, horizontal = 10.dp)
                                        .wrapContentHeight(Alignment.CenterVertically)
                        )
                    }
                }

                val incompleteErrorText =
                        if (errorOnIncompleteRows) ResOrActual.StringResource(R.string.err__required_field) else null
                set.data.sortedBy { it.type.ordinal }.forEach { row ->
                    val onClick = {
                        rowClicked(setIndex + 1, row.type)
                        focusRequesters?.get(row.type)?.requestFocus()
                        Unit
                    }

                    columnMetadata.forEach { column ->
                        val value = column.mapping(row, extraData)?.get(resources)
                        val isSelectable = state is HeadToHeadGridState.SingleEditable && (
                                (row.isTotalRow && column == HeadToHeadGridColumn.END_TOTAL)
                                        || (!row.isTotalRow && column == HeadToHeadGridColumn.ARROWS)
                                )
                        val isSelected = isSelectable
                                && row.type == (state as HeadToHeadGridState.SingleEditable).selected
                        val showIncompleteError = isSelectable && incompleteErrorText != null && !row.isComplete

                        if (value == null) {
                            // No cell
                        }
                        else if (
                            column == HeadToHeadGridColumn.END_TOTAL
                            && row is HeadToHeadGridRowData.EditableTotal
                            && row.type != HeadToHeadArcherType.RESULT
                        ) {
                            item(
                                    fillBox = true,
                                    verticalSpan = column.cellVerticalSpan(row, extraData),
                                    horizontalSpan = column.cellHorizontalSpan(row, extraData),
                            ) {
                                CodexNumberField(
                                        contentDescription = row.type.text.get(),
                                        currentValue = row.text.text,
                                        testTag = column.testTag.get(),
                                        placeholder = stringResource(R.string.head_to_head_add_end__total_text_placeholder),
                                        errorMessage = row.text.error
                                                ?: incompleteErrorText?.takeIf { !row.isComplete },
                                        onValueChanged = { onTextValueChanged(row.type, it) },
                                        colors = CodexTextField.transparentOutlinedTextFieldColors(
                                                focussedColor = CodexColors.COLOR_PRIMARY_DARK,
                                                unfocussedColor = CodexColors.COLOR_ON_PRIMARY_LIGHT,
                                                backgroundColor = (
                                                        if (isSelected) Color.White
                                                        else CodexTheme.colors.listAccentRowItemOnAppBackground
                                                        ),
                                        ),
                                        modifier = Modifier
                                                .modifierIfNotNull(focusRequesters?.get(row.type)) {
                                                    Modifier.focusRequester(it)
                                                }
                                                .onFocusChanged {
                                                    if (it.hasFocus) rowClicked(set.setNumber, row.type)
                                                }
                                )
                            }
                        }
                        else {
                            item(
                                    fillBox = true,
                                    verticalSpan = column.cellVerticalSpan(row, extraData),
                                    horizontalSpan = column.cellHorizontalSpan(row, extraData),
                            ) {
                                val background =
                                        if (isSelected) Color.White
                                        else if (isSelectable) CodexTheme.colors.listAccentRowItemOnAppBackground
                                        else CodexTheme.colors.listItemOnAppBackground
                                val backgroundShape = if (isSelectable) selectedShape else RectangleShape
                                val borderColor =
                                        if (isSelected) CodexColors.COLOR_PRIMARY_DARK
                                        else if (showIncompleteError) CodexTheme.colors.errorOnAppBackground
                                        else if (isSelectable) CodexColors.COLOR_ON_PRIMARY_LIGHT
                                        else CodexTheme.colors.listItemOnAppBackground

                                val horizontalPadding = if (column == HeadToHeadGridColumn.TYPE) 8.dp else 15.dp

                                debugLog("testTag: ${column.testTag.get().getElement()}")
                                Text(
                                        text = value,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                                .testTag(column.testTag.get())
                                                .modifierIf(
                                                        isSelectable,
                                                        Modifier.border(2.dp, borderColor, selectedShape),
                                                )
                                                .background(background, backgroundShape)
                                                .padding(horizontal = horizontalPadding, vertical = 3.dp)
                                                .wrapContentHeight(Alignment.CenterVertically)
                                                .clickable(onClick = onClick)
                                                .semantics {
                                                    column
                                                            .cellContentDescription(row, extraData)
                                                            ?.get(resources)
                                                            ?.let {
                                                                contentDescription = it
                                                            }
                                                    if (showIncompleteError) {
                                                        error(incompleteErrorText!!.get(resources))
                                                    }
                                                }
                                )
                            }
                        }
                    }
                }

                if (state is HeadToHeadGridState.NonEditable) {
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
                            DataRow(
                                    title = stringResource(
                                            R.string.head_to_head_add_end__set_result,
                                            extraData.result.title.get(),
                                    ),
                                    text = extraData.result.title.get(),
                                    modifier = Modifier
                                            .padding(vertical = 5.dp, horizontal = 10.dp)
                                            .testTag(SET_RESULT.get())
                            )
                            val runningTotals = state.runningTotals?.getOrNull(setIndex)?.left?.let {
                                stringResource(R.string.head_to_head_add_end__score_text, it.first, it.second)
                            }
                            DataRow(
                                    title = stringResource(R.string.head_to_head_add_end__running_total),
                                    text = runningTotals
                                            ?: stringResource(R.string.score_pad__running_total_placeholder),
                                    modifier = Modifier
                                            .padding(vertical = 5.dp, horizontal = 10.dp)
                                            .testTag(SET_RUNNING_TOTAL.get())
                            )
                        }
                    }
                }
            }

            if (state is HeadToHeadGridState.NonEditable && state.finalResult != null) {
                item(
                        fillBox = true,
                        horizontalSpan = columnMetadata.size,
                ) {
                    DataRow(
                            title = stringResource(R.string.head_to_head_add_end__final_result),
                            text = state.finalResult.title.get(),
                            textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                    .background(CodexTheme.colors.listAccentRowItemOnAppBackground)
                                    .padding(vertical = 5.dp, horizontal = 10.dp)
                                    .wrapContentHeight(Alignment.CenterVertically)
                                    .testTag(MATCH_RESULT.get(state.matchNumber, 1))
                    )
                }
            }
        }
    }

    val focusManager = LocalFocusManager.current
    val selectedRow = (state as? HeadToHeadGridState.SingleEditable)?.selected
    LaunchedEffect(selectedRow) {
        if (selectedRow != null) {
            val requester = focusRequesters?.get(selectedRow)

            if (requester != null) requester.requestFocus()
            else focusManager.clearFocus()
        }
    }
}

enum class HeadToHeadGridTestTag : CodexTestTag {
    SCREEN,
    EDIT_ROWS_BUTTON,
    ;

    override val screenName: String
        get() = SCREEN_NAME

    override fun getElement(): String = name

    companion object {
        const val SCREEN_NAME = "HEAD_TO_HEAD_GRID"
    }
}

enum class HeadToHeadGridColumnTestTag {
    MATCH_RESULT,
    SET_NUMBER_CELL,
    TYPE_CELL,
    ARROW_CELL,
    END_TOTAL_CELL,
    TEAM_TOTAL_CELL,
    POINTS_CELL,
    SET_RESULT,
    SET_RUNNING_TOTAL,
    ;

    fun get(matchNumber: Int, setNumber: Int): CodexTestTag = object : CodexTestTag {
        override val screenName: String
            get() = HeadToHeadGridTestTag.SCREEN_NAME

        override fun getElement(): String = "${name}_${matchNumber}_$setNumber"
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Input_HeadToHeadGrid_Preview() {
    CodexTheme {
        HeadToHeadGridPreviewHelper(
                state = HeadToHeadGridState.SingleEditable(
                        enteredArrows = listOf(
                                FullHeadToHeadSet(
                                        data = HeadToHeadGridRowDataPreviewHelper.create(isEditable = true),
                                        teamSize = 1,
                                        isShootOffWin = false,
                                        setNumber = 1,
                                        isRecurveStyle = true,
                                )
                        ),
                        selected = null,
                ),
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
        HeadToHeadGridPreviewHelper(
                state = HeadToHeadGridState.SingleEditable(
                        enteredArrows = listOf(
                                FullHeadToHeadSet(
                                        data = HeadToHeadGridRowDataPreviewHelper.create(
                                                teamSize = 2,
                                                typesToIsTotal = mapOf(
                                                        HeadToHeadArcherType.SELF to false,
                                                        HeadToHeadArcherType.TEAM_MATE to false,
                                                        HeadToHeadArcherType.OPPONENT to true,
                                                        HeadToHeadArcherType.RESULT to true,
                                                ),
                                                isEditable = true,
                                        ),
                                        teamSize = 2,
                                        isShootOffWin = false,
                                        setNumber = 1,
                                        isRecurveStyle = true,
                                ),
                        ),
                        selected = null,
                ),
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
        HeadToHeadGridPreviewHelper(
                state = HeadToHeadGridState.NonEditable(
                        matchNumber = 1,
                        enteredArrows = listOf(
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
                                        data = HeadToHeadGridRowDataPreviewHelper.create(isShootOff = true),
                                        teamSize = 1,
                                        isShootOffWin = true,
                                        setNumber = 3,
                                        isRecurveStyle = true,
                                ),
                        ),
                        runningTotals = listOf(2 to 0, 4 to 0, 5 to 0).map { Either.Left(it) },
                        finalResult = HeadToHeadResult.WIN,
                ),
        )
    }
}

@Preview(
        widthDp = 500,
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun AllTypesTotals_HeadToHeadGrid_Preview() {
    CodexTheme {
        HeadToHeadGridPreviewHelper(
                state = HeadToHeadGridState.NonEditable(
                        matchNumber = 1,
                        enteredArrows = listOf(
                                FullHeadToHeadSet(
                                        data = HeadToHeadGridRowDataPreviewHelper.create(
                                                typesToIsTotal = mapOf(
                                                        HeadToHeadArcherType.SELF to true,
                                                        HeadToHeadArcherType.TEAM_MATE to true,
                                                        HeadToHeadArcherType.TEAM to true,
                                                        HeadToHeadArcherType.OPPONENT to true,
                                                        HeadToHeadArcherType.RESULT to true,
                                                ),
                                                teamSize = 2,
                                        ),
                                        teamSize = 2,
                                        isShootOffWin = false,
                                        setNumber = 1,
                                        isRecurveStyle = true,
                                )
                        ),
                        runningTotals = null,
                        finalResult = null,
                ),
                errorOnIncompleteRows = true,
        )
    }
}

@Preview(
        widthDp = 500,
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun AllTypesArrows_HeadToHeadGrid_Preview() {
    CodexTheme {
        HeadToHeadGridPreviewHelper(
                state = HeadToHeadGridState.NonEditable(
                        matchNumber = 1,
                        enteredArrows = listOf(
                                FullHeadToHeadSet(
                                        data = HeadToHeadGridRowDataPreviewHelper.create(
                                                typesToIsTotal = mapOf(
                                                        HeadToHeadArcherType.SELF to false,
                                                        HeadToHeadArcherType.TEAM_MATE to false,
                                                        HeadToHeadArcherType.TEAM to false,
                                                        HeadToHeadArcherType.OPPONENT to false,
                                                ),
                                                teamSize = 2,
                                        ),
                                        teamSize = 2,
                                        isShootOffWin = false,
                                        setNumber = 1,
                                        isRecurveStyle = true,
                                )
                        ),
                        runningTotals = null,
                        finalResult = null,
                ),
                errorOnIncompleteRows = true,
        )
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Error_HeadToHeadGrid_Preview() {
    CodexTheme {
        HeadToHeadGridPreviewHelper(
                state = HeadToHeadGridState.NonEditable(
                        matchNumber = 1,
                        enteredArrows = listOf(
                                FullHeadToHeadSet(
                                        data = HeadToHeadGridRowDataPreviewHelper.createEmptyRows(isEditable = true),
                                        teamSize = 1,
                                        isShootOffWin = false,
                                        setNumber = 1,
                                        isRecurveStyle = true,
                                )
                        ),
                        runningTotals = null,
                        finalResult = null,
                ),
                errorOnIncompleteRows = true,
        )
    }
}

@Composable
fun HeadToHeadGridPreviewHelper(
        state: HeadToHeadGridState,
        errorOnIncompleteRows: Boolean = false,
) {
    HeadToHeadGrid(
            state = state,
            errorOnIncompleteRows = errorOnIncompleteRows,
            rowClicked = { _, _ -> },
            onTextValueChanged = { _, _ -> },
            editTypesClicked = {},
            helpListener = {},
            modifier = Modifier.padding(vertical = 20.dp)
    )
}
