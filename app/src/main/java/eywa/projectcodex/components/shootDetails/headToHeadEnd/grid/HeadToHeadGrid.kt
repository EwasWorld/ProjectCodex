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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.CodexTextField
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIf
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIfNotNull
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
    val resources = LocalContext.current.resources

    val focusRequesters = remember(
            state.isSingleEditableSet,
            state.enteredArrows.firstOrNull()?.data?.map { it.type },
    ) {
        if (!state.isSingleEditableSet) null
        else state.enteredArrows.firstOrNull()?.data
                ?.filter { it.isTotalRow }
                ?.associate { it.type to FocusRequester() }
    }

    val columnMetadata = listOfNotNull(
            HeadToHeadGridColumn.SET_NUMBER.takeIf { !state.isSingleEditableSet },
            HeadToHeadGridColumn.TYPE,
            HeadToHeadGridColumn.ARROWS.takeIf {
                state.enteredArrows.anyRow { it is HeadToHeadGridRowData.Arrows }
            },
            HeadToHeadGridColumn.END_TOTAL,
            HeadToHeadGridColumn.TEAM_TOTAL.takeIf { state.showExtraTotalColumn },
            HeadToHeadGridColumn.POINTS.takeIf {
                !state.isSingleEditableSet
                        || state.enteredArrows.anyRow { it.type == HeadToHeadArcherType.RESULT }
            },
    )

    val style = if (state.isSingleEditableSet) CodexTypography.NORMAL_PLUS else CodexTypography.NORMAL
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
                        val clickable = it == HeadToHeadGridColumn.TYPE && state.isSingleEditableSet
                        Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                        .background(CodexTheme.colors.listAccentRowItemOnAppBackground)
                                        .padding(vertical = 5.dp, horizontal = 10.dp)
                                        .wrapContentHeight(Alignment.CenterVertically)
                                        .modifierIf(clickable, Modifier.clickable { editTypesClicked() })
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
                val extraData = HeadToHeadSetData(
                        isShootOff = set.isShootOff,
                        teamEndTotal = set.teamEndScore ?: 0,
                        opponentEndTotal = set.opponentEndScore ?: 0,
                        hasSelfAndTeamRows = set.showExtraColumnTotal(),
                        result = set.result,
                )

                if (!state.isSingleEditableSet) {
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

                val incompleteError =
                        if (errorOnIncompleteRows) ResOrActual.StringResource(R.string.err__required_field) else null
                set.data.sortedBy { it.type.ordinal }.forEach { row ->
                    val onClick = {
                        rowClicked(setIndex + 1, row.type)
                        focusRequesters?.get(row.type)?.requestFocus()
                        Unit
                    }

                    columnMetadata.forEach { column ->
                        val cellModifier = column.testTag?.let { Modifier.testTag(it) } ?: Modifier
                        val value = column.mapping(row, extraData)?.get(resources)
                        val isSelectable = state.isSingleEditableSet && (
                                (row.isTotalRow && column == HeadToHeadGridColumn.END_TOTAL)
                                        || (!row.isTotalRow && column == HeadToHeadGridColumn.ARROWS)
                                )
                        val isSelected = isSelectable && row.type == state.selected

                        if (value == null) {
                            // No cell
                        }
                        else if (column == HeadToHeadGridColumn.END_TOTAL && row is HeadToHeadGridRowData.EditableTotal) {
                            item(fillBox = true) {
                                CodexNumberField(
                                        contentDescription = row.type.text.get(),
                                        currentValue = row.text.text,
                                        testTag = HeadToHeadGridTestTag.END_TOTAL_INPUT,
                                        placeholder = stringResource(R.string.head_to_head_add_end__total_text_placeholder),
                                        errorMessage = row.text.error ?: incompleteError?.takeIf { !row.isComplete },
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
                            ) {
                                val background =
                                        if (isSelected) Color.White
                                        else if (isSelectable) CodexTheme.colors.listAccentRowItemOnAppBackground
                                        else CodexTheme.colors.listItemOnAppBackground
                                val backgroundShape = if (isSelectable) selectedShape else RectangleShape
                                val borderColor =
                                        if (isSelected) CodexColors.COLOR_PRIMARY_DARK
                                        else if (isSelectable && incompleteError != null) CodexTheme.colors.errorOnAppBackground
                                        else if (isSelectable) CodexColors.COLOR_ON_PRIMARY_LIGHT
                                        else CodexTheme.colors.listItemOnAppBackground

                                val horizontalPadding = if (column == HeadToHeadGridColumn.TYPE) 8.dp else 15.dp

                                Text(
                                        text = value,
                                        textAlign = TextAlign.Center,
                                        modifier = cellModifier
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
                                    text = stringResource(
                                            R.string.head_to_head_add_end__set_result,
                                            extraData.result.title.get(),
                                    ),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp)
                            )
                            val runningTotals = state.runningTotals?.getOrNull(setIndex)?.left?.let {
                                stringResource(R.string.head_to_head_add_end__score_text, it.first, it.second)
                            }
                            Text(
                                    text = stringResource(
                                            R.string.head_to_head_add_end__running_total,
                                            runningTotals
                                                    ?: stringResource(R.string.score_pad__running_total_placeholder),
                                    ),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp)
                            )
                        }
                    }
                }
            }

            if (!state.isSingleEditableSet && state.finalResult != null) {
                item(
                        fillBox = true,
                        horizontalSpan = columnMetadata.size,
                ) {
                    Text(
                            text = stringResource(
                                    R.string.head_to_head_add_end__final_result,
                                    state.finalResult.title.get(),
                            ),
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
    }

    val focusManager = LocalFocusManager.current
    LaunchedEffect(state.selected) {
        if (state.selected != null) {
            val requester = focusRequesters?.get(state.selected)

            if (requester != null) requester.requestFocus()
            else focusManager.clearFocus()
        }
    }
}

enum class HeadToHeadGridTestTag : CodexTestTag {
    SCREEN,
    END_TOTAL_INPUT,
    ;

    override val screenName: String
        get() = "HEAD_TO_HEAD_GRID"

    override fun getElement(): String = name
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Input_HeadToHeadGrid_Preview() {
    CodexTheme {
        HeadToHeadGrid(
                state = HeadToHeadGridState(
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
                        isSingleEditableSet = true,
                        runningTotals = null,
                        finalResult = null,
                ),
                errorOnIncompleteRows = false,
                rowClicked = { _, _ -> },
                onTextValueChanged = { _, _ -> },
                editTypesClicked = {},
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
        HeadToHeadGrid(
                state = HeadToHeadGridState(
                        enteredArrows = listOf(
                                FullHeadToHeadSet(
                                        data = HeadToHeadGridRowDataPreviewHelper.create(
                                                teamSize = 2,
                                                typesToIsTotal = mapOf(
                                                        HeadToHeadArcherType.SELF to false,
                                                        HeadToHeadArcherType.TEAM_MATE to false,
                                                        HeadToHeadArcherType.OPPONENT to true,
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
                        isSingleEditableSet = true,
                        runningTotals = null,
                        finalResult = null,
                ),
                errorOnIncompleteRows = false,
                rowClicked = { _, _ -> },
                onTextValueChanged = { _, _ -> },
                editTypesClicked = {},
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
        HeadToHeadGrid(
                state = HeadToHeadGridState(
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
                        selected = null,
                        isSingleEditableSet = false,
                        runningTotals = listOf(2 to 0, 4 to 0, 5 to 0).map { Either.Left(it) },
                        finalResult = HeadToHeadResult.WIN,
                ),
                errorOnIncompleteRows = false,
                rowClicked = { _, _ -> },
                onTextValueChanged = { _, _ -> },
                editTypesClicked = {},
                helpListener = {},
                modifier = Modifier.padding(vertical = 20.dp)
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
        HeadToHeadGrid(
                state = HeadToHeadGridState(
                        enteredArrows = listOf(
                                FullHeadToHeadSet(
                                        data = HeadToHeadGridRowDataPreviewHelper.createEmptyRows(isEditable = true),
                                        teamSize = 1,
                                        isShootOffWin = false,
                                        setNumber = 1,
                                        isRecurveStyle = true,
                                )
                        ),
                        selected = null,
                        isSingleEditableSet = true,
                        runningTotals = null,
                        finalResult = null,
                ),
                errorOnIncompleteRows = true,
                rowClicked = { _, _ -> },
                onTextValueChanged = { _, _ -> },
                editTypesClicked = {},
                helpListener = {},
                modifier = Modifier.padding(vertical = 20.dp)
        )
    }
}
