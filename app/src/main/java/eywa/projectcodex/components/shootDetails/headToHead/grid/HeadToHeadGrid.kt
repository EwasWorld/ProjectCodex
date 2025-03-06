package eywa.projectcodex.components.shootDetails.headToHead.grid

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
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
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGridColumnTestTag.*
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
        itemClickedListener: (setNumber: Int, type: SetDropdownMenuItem) -> Unit,
        closeDropdownMenuListener: (setNumber: Int) -> Unit,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    val resources = LocalContext.current.resources
    val colors = CodexTheme.colors

    val focusRequesters = remember(
            state is HeadToHeadGridState.SingleEditable,
            state.enteredArrows.firstOrNull()?.data?.map { it.type },
    ) {
        if (state !is HeadToHeadGridState.SingleEditable) {
            null
        }
        else {
            state.enteredArrows.firstOrNull()?.data
                    ?.filter {
                        it.isTotalRow
                                // TODO Why not these two?
                                && it.type != HeadToHeadArcherType.RESULT
                                && it.type != HeadToHeadArcherType.SHOOT_OFF
                    }
                    ?.associate { it.type to FocusRequester() }
        }
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
                if (it.primaryTitle == null) {
                    item {
                        Box {}
                    }
                }
                else {
                    item(
                            backgroundColor = { CodexTheme.colors.listAccentRowItemOnAppBackground },
                    ) {
                        val clickable = it == HeadToHeadGridColumn.TYPE && state is HeadToHeadGridState.SingleEditable
                        Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
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
                fun HeadToHeadGridColumnTestTag.get(type: HeadToHeadArcherType? = null): CodexTestTag {
                    val rowTitle = type?.text?.get(resources)?.replace(Regex("[^a-zA-Z0-9]"), "")
                    return if (state is HeadToHeadGridState.SingleEditable) get(1, 1, rowTitle)
                    else get(state.matchNumber, set.setNumber, rowTitle)
                }

                val extraData = HeadToHeadSetData(
                        isShootOff = set.isShootOff,
                        teamEndTotal = set.teamEndScore ?: 0,
                        opponentEndTotal = set.opponentEndScore ?: 0,
                        hasSelfAndTeamRows = set.showExtraColumnTotal(),
                        teamTotalColumnSpan = setOf(
                                HeadToHeadArcherType.SELF,
                                HeadToHeadArcherType.TEAM_MATE,
                                HeadToHeadArcherType.TEAM,
                        ).intersect(set.data.map { it.type }.toSet()).size,
                        resultColumnSpan = setOf(
                                HeadToHeadGridColumn.ARROWS,
                                HeadToHeadGridColumn.END_TOTAL,
                                HeadToHeadGridColumn.TEAM_TOTAL,
                                HeadToHeadGridColumn.POINTS,
                        ).intersect(columnMetadata.toSet()).size,
                        result = set.result,
                )

                if (state is HeadToHeadGridState.NonEditable) {
                    item(
                            backgroundColor = { CodexTheme.colors.listAccentRowItemOnAppBackground },
                            // + 1 for total row
                            verticalSpan = set.data.size + 1,
                    ) {
                        Text(
                                text = (setIndex + 1).toString(),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
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
                        val isSelectable = state is HeadToHeadGridState.SingleEditable
                                && (
                                // Total or arrow column
                                (row.isTotalRow && column == HeadToHeadGridColumn.END_TOTAL)
                                        || (!row.isTotalRow && column == HeadToHeadGridColumn.ARROWS)
                                )
                                && (
                                // Don't allow SHOOT_OFF_IS_CLOSEST to be selectable if score isn't tied
                                row.type != HeadToHeadArcherType.SHOOT_OFF
                                        || extraData.teamEndTotal == extraData.opponentEndTotal
                                )
                        val isSelected = isSelectable
                                && row.type == (state as HeadToHeadGridState.SingleEditable).selected
                        val showIncompleteError = isSelectable && incompleteErrorText != null && !row.isComplete

                        val backgroundColor =
                                if (isSelected) Color.White
                                else if (isSelectable) colors.listAccentRowItemOnAppBackground
                                else colors.listItemOnAppBackground

                        val borderColor =
                                if (isSelected) CodexColors.COLOR_PRIMARY_DARK
                                else if (showIncompleteError) colors.errorOnAppBackground
                                else if (isSelectable) CodexColors.COLOR_ON_PRIMARY_LIGHT
                                else colors.listItemOnAppBackground

                        if (
                            column == HeadToHeadGridColumn.END_TOTAL
                            && row is HeadToHeadGridRowData.ShootOff
                        ) {
                            item(
                                    verticalSpan = column.cellVerticalSpan(row, extraData),
                                    horizontalSpan = column.cellHorizontalSpan(row, extraData),
                                    backgroundColor = { backgroundColor },
                                    backgroundShape = selectedShape,
                                    modifier = Modifier
                                            .border(2.dp, borderColor, selectedShape)
                                            .clickable(onClick = onClick)
                            ) {
                                val icon = when (row.result) {
                                    HeadToHeadResult.WIN -> Icons.Default.Check
                                    HeadToHeadResult.LOSS -> Icons.Default.Close
                                    else -> Icons.Default.Remove
                                }
                                val contentDescription = when (row.result) {
                                    HeadToHeadResult.WIN -> "True"
                                    HeadToHeadResult.LOSS -> "False"
                                    else -> "Not applicable"
                                }
                                Icon(
                                        imageVector = icon,
                                        contentDescription = contentDescription,
                                        tint = CodexTheme.colors.onListItemAppOnBackground,
                                        modifier = Modifier.testTag(column.testTag.get(row.type))
                                )
                            }
                        }
                        else if (value == null) {
                            // No cell
                        }
                        else if (
                            column == HeadToHeadGridColumn.END_TOTAL
                            && row is HeadToHeadGridRowData.EditableTotal
                        ) {
                            val finalBorderColor =
                                    if (row.text.error == null) borderColor
                                    else colors.errorOnAppBackground

                            item(
                                    verticalSpan = column.cellVerticalSpan(row, extraData),
                                    horizontalSpan = column.cellHorizontalSpan(row, extraData),
                                    backgroundColor = { backgroundColor },
                                    backgroundShape = selectedShape,
                                    modifier = Modifier
                                            .modifierIf(
                                                    isSelectable,
                                                    Modifier.border(2.dp, finalBorderColor, selectedShape),
                                            )
                            ) {
                                CodexNumberField(
                                        contentDescription = row.type.text.get(),
                                        currentValue = row.text.text,
                                        testTag = column.testTag.get(row.type),
                                        placeholder = stringResource(R.string.head_to_head_add_end__total_text_placeholder),
                                        errorMessage = row.text.error
                                                ?: incompleteErrorText?.takeIf { !row.isComplete },
                                        onValueChanged = { onTextValueChanged(row.type, it) },
                                        colors = CodexTextField.transparentOutlinedTextFieldColors(
                                                focussedColor = Color.Transparent,
                                                unfocussedColor = Color.Transparent,
                                                backgroundColor = Color.Transparent,
                                                errorColor = Color.Transparent,
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
                                    backgroundColor = { backgroundColor },
                                    backgroundShape = if (isSelectable) selectedShape else RectangleShape,
                                    verticalSpan = column.cellVerticalSpan(row, extraData),
                                    horizontalSpan = column.cellHorizontalSpan(row, extraData),
                                    modifier = Modifier
                                            .modifierIf(
                                                    isSelectable,
                                                    Modifier.border(2.dp, borderColor, selectedShape),
                                            )
                                            .clickable(onClick = onClick)
                            ) {
                                val horizontalPadding = if (column == HeadToHeadGridColumn.TYPE) 8.dp else 15.dp

                                Text(
                                        text = value,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                                .testTag(column.testTag.get(row.type))
                                                .padding(horizontal = horizontalPadding, vertical = 3.dp)
                                                .wrapContentHeight(Alignment.CenterVertically)
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
                            itemCount = 2,
                            backgroundColor = { CodexTheme.colors.listAccentRowItemOnAppBackground },
                            // Ignore set number column
                            horizontalSpan = columnMetadata.size - 1,
                    ) {
                        Box {
                            if (state.showSetResult) {
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
                            }
                            else {
                                Spacer(modifier = Modifier.height(1.dp))
                            }
                            DropdownMenu(
                                    menuItems = state.dropdownMenuExpandedFor?.third.orEmpty(),
                                    expanded = state.dropdownMenuExpandedFor?.first == state.matchNumber
                                            && state.dropdownMenuExpandedFor.second == set.setNumber,
                                    itemClickedListener = { itemClickedListener(set.setNumber, it) },
                                    dismissListener = { closeDropdownMenuListener(set.setNumber) },
                                    testTag = SET_DROPDOWN_MENU_ITEM.get(),
                            )
                        }
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

            if (state is HeadToHeadGridState.NonEditable && state.finalResult != null) {
                item(
                        backgroundColor = { CodexTheme.colors.listAccentRowItemOnAppBackground },
                        horizontalSpan = columnMetadata.size,
                ) {
                    DataRow(
                            title = stringResource(R.string.head_to_head_add_end__final_result),
                            text = state.finalResult.title.get(),
                            textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
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

@Composable
private fun DropdownMenu(
        menuItems: List<SetDropdownMenuItem> = emptyList(),
        expanded: Boolean,
        testTag: CodexTestTag,
        itemClickedListener: (SetDropdownMenuItem) -> Unit,
        dismissListener: () -> Unit,
) {
    DropdownMenu(
            expanded = expanded,
            onDismissRequest = dismissListener,
    ) {
        menuItems.forEach { item ->
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
    SET_DROPDOWN_MENU_ITEM,
    ;

    fun get(matchNumber: Int, setNumber: Int, type: String? = null): CodexTestTag = object : CodexTestTag {
        override val screenName: String
            get() = HeadToHeadGridTestTag.SCREEN_NAME

        override fun getElement(): String = "${name}_${matchNumber}_$setNumber" + (type?.let { "_$it" } ?: "")
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Editable_HeadToHeadGrid_Preview() {
    CodexTheme {
        HeadToHeadGridPreviewHelper(
                state = HeadToHeadGridState.SingleEditable(
                        enteredArrows = listOf(
                                FullHeadToHeadSet(
                                        data = HeadToHeadGridRowDataPreviewHelper.create(isEditable = true),
                                        teamSize = 1,
                                        setNumber = 1,
                                        isSetPointsFormat = true,
                                        endSize = 3,
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
fun EditableTeam_HeadToHeadGrid_Preview() {
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
                                        setNumber = 1,
                                        isSetPointsFormat = true,
                                        endSize = 3,
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
fun EditablePartialTeam_HeadToHeadGrid_Preview() {
    CodexTheme {
        HeadToHeadGridPreviewHelper(
                state = HeadToHeadGridState.SingleEditable(
                        enteredArrows = listOf(
                                FullHeadToHeadSet(
                                        data = HeadToHeadGridRowDataPreviewHelper.create(
                                                teamSize = 2,
                                                typesToIsTotal = mapOf(
                                                        HeadToHeadArcherType.SELF to false,
                                                        HeadToHeadArcherType.OPPONENT to true,
                                                        HeadToHeadArcherType.RESULT to true,
                                                ),
                                                isEditable = true,
                                        ).plus(
                                                HeadToHeadGridRowData.EditableTotal(
                                                        type = HeadToHeadArcherType.TEAM_MATE,
                                                        expectedArrowCount = 3,
                                                ),
                                        ),
                                        teamSize = 2,
                                        setNumber = 1,
                                        isSetPointsFormat = true,
                                        endSize = 3,
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
                                        setNumber = 1,
                                        isSetPointsFormat = true,
                                        endSize = 3,
                                ),
                                FullHeadToHeadSet(
                                        data = HeadToHeadGridRowDataPreviewHelper.create(),
                                        teamSize = 1,
                                        setNumber = 2,
                                        isSetPointsFormat = true,
                                        endSize = 3,
                                ),
                                FullHeadToHeadSet(
                                        data = HeadToHeadGridRowDataPreviewHelper.create(isShootOff = true),
                                        teamSize = 1,
                                        setNumber = 3,
                                        isSetPointsFormat = true,
                                        endSize = 3,
                                ),
                        ),
                        runningTotals = listOf(2 to 0, 4 to 0, 5 to 0).map { Either.Left(it) },
                        finalResult = HeadToHeadResult.WIN,
                        showSetResult = true,
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
fun AllTypesTotals_ScorePad_HeadToHeadGrid_Preview() {
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
                                        setNumber = 1,
                                        isSetPointsFormat = true,
                                        endSize = 3,
                                ),
                        ),
                        runningTotals = null,
                        finalResult = null,
                        showSetResult = false,
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
fun AllTypesArrows_ScorePad_HeadToHeadGrid_Preview() {
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
                                        setNumber = 1,
                                        isSetPointsFormat = true,
                                        endSize = 3,
                                ),
                        ),
                        runningTotals = null,
                        finalResult = null,
                        showSetResult = true,
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
fun Editable_Error_HeadToHeadGrid_Preview() {
    CodexTheme {
        HeadToHeadGridPreviewHelper(
                state = HeadToHeadGridState.SingleEditable(
                        enteredArrows = listOf(
                                FullHeadToHeadSet(
                                        data = HeadToHeadGridRowDataPreviewHelper.createEmptyRows(isEditable = true),
                                        teamSize = 1,
                                        setNumber = 1,
                                        isSetPointsFormat = true,
                                        endSize = 3,
                                ),
                        ),
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
            itemClickedListener = { _: Int, _: SetDropdownMenuItem -> },
            closeDropdownMenuListener = {},
            modifier = Modifier.padding(vertical = 20.dp)
    )
}
