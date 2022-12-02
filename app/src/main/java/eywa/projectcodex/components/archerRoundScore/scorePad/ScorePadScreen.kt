package eywa.projectcodex.components.archerRoundScore.scorePad

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.get
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent.ScorePadIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent.ScorePadIntent.*
import eywa.projectcodex.components.archerRoundScore.ArcherRoundsPreviewHelper
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadDataNew
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadDataNew.ColumnHeader
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadDataNew.ScorePadRow


private val COLUMN_HEADER_ORDER = listOf(
        ColumnHeader.ARROWS,
        ColumnHeader.HITS,
        ColumnHeader.SCORE,
        ColumnHeader.GOLDS,
        ColumnHeader.RUNNING_TOTAL,
)

@Composable
fun ScorePadScreen(
        isRoundFull: Boolean,
        displayDeleteEndConfirmationDialog: Boolean,
        dropdownMenuOpenForEndNumber: Int?,
        data: ScorePadDataNew,
        listener: (ArcherRoundIntent) -> Unit,
) {
    SimpleDialog(
            isShown = data.isNullOrEmpty(),
            onDismissListener = { listener(ArcherRoundIntent.NoArrowsDialogOkClicked) },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.archer_round_stats__no_arrows_dialog_title),
                positiveButton = ButtonState(
                        text = stringResource(R.string.archer_round_stats__no_arrows_dialog_button),
                        onClick = { listener(ArcherRoundIntent.NoArrowsDialogOkClicked) },
                ),
        )
    }
    SimpleDialog(
            isShown = displayDeleteEndConfirmationDialog,
            onDismissListener = { listener(ArcherRoundIntent.DeleteEndDialogCancelClicked) },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.score_pad_menu__delete_dialog_title),
                message = stringResource(
                        R.string.score_pad_menu__delete_dialog_body,
                        dropdownMenuOpenForEndNumber ?: -1
                ),
                positiveButton = ButtonState(
                        text = stringResource(R.string.general_delete),
                        onClick = { listener(ArcherRoundIntent.DeleteEndDialogOkClicked) },
                ),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener(ArcherRoundIntent.DeleteEndDialogCancelClicked) },
                ),
        )
    }

    if (data.isNullOrEmpty()) return

    // TODO Make the row and column headers stick
    Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState())
                    .verticalScroll(rememberScrollState())
                    .padding(5.dp)
    ) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            // Placeholder for the first row which is the column header for other columns
            Cell(
                    text = "",
                    listener = listener,
            )
            data.data.forEach { rowData ->
                Cell(
                        rowData = rowData,
                        listener = listener,
                )
            }
        }

        COLUMN_HEADER_ORDER.forEach { columnHeader ->
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(IntrinsicSize.Max)
            ) {
                Cell(
                        text = stringResource(columnHeader.getShortResourceId(data.goldsType)),
                        columnType = columnHeader,
                        listener = listener,
                )

                data.data.forEach { rowData ->
                    Box {
                        Cell(
                                rowData = rowData,
                                columnType = columnHeader,
                                listener = listener,
                        )
                        DropdownMenu(
                                isRoundFull = isRoundFull,
                                expanded = dropdownMenuOpenForEndNumber != null
                                        && columnHeader == ColumnHeader.ARROWS
                                        && (rowData as? ScorePadRow.End)?.endNumber == dropdownMenuOpenForEndNumber,
                                listener = listener,
                        )
                    }
                }
            }
        }
    }
}

/**
 * @param columnType null for the row headers column
 */
@Composable
private fun Cell(
        text: String? = null,
        rowData: ScorePadRow? = null,
        columnType: ColumnHeader? = null,
        listener: (ScorePadIntent) -> Unit,
) {
    val isTotalRow = rowData != null && rowData !is ScorePadRow.End
    val isHeaderOrTotal = isTotalRow || rowData == null || columnType == null

    val backgroundColour = when {
        isTotalRow -> CodexTheme.colors.listAccentRowItemOnAppBackground
        rowData == null && columnType == null -> null
        rowData != null && columnType != null -> CodexTheme.colors.listItemOnAppBackground
        else -> CodexTheme.colors.listAccentRowItemOnAppBackground
    }
    val backgroundModifier = backgroundColour?.let { Modifier.background(it) } ?: Modifier

    val clickModifier = if (rowData !is ScorePadRow.End) Modifier
    else Modifier.pointerInput(rowData) {
        detectTapGestures(
                onTap = { listener(RowClicked(rowData.endNumber)) },
                onLongPress = { listener(RowLongClicked(rowData.endNumber)) },
        )
    }

    Text(
            text = text
                    ?: columnType?.let { rowData!!.getContent(it, LocalContext.current.resources) }
                    ?: rowData!!.getRowHeader().get(),
            style = CodexTypography.NORMAL.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = if (isHeaderOrTotal) FontWeight.Bold else FontWeight.Normal,
            ),
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)
                    .then(backgroundModifier)
                    .padding(vertical = 5.dp, horizontal = 10.dp)
                    .then(clickModifier)
    )
}

@Composable
private fun DropdownMenu(
        isRoundFull: Boolean,
        expanded: Boolean,
        listener: (ScorePadIntent) -> Unit
) {
    DropdownMenu(
            expanded = expanded,
            onDismissRequest = { listener(CloseDropdownMenu) }
    ) {
        DropdownMenuItem.values().forEach { item ->
            if (isRoundFull && item == DropdownMenuItem.INSERT_END) return@forEach

            DropdownMenuItem(
                    onClick = { listener(item.action) },
            ) {
                Text(
                        text = stringResource(id = item.title),
                        style = CodexTypography.NORMAL
                )
            }
        }
    }
}

private enum class DropdownMenuItem(@StringRes val title: Int, val action: ScorePadIntent) {
    EDIT_END(R.string.score_pad_menu__edit, EditEndClicked),
    INSERT_END(R.string.score_pad_menu__insert, InsertEndClicked),
    DELETE_END(R.string.score_pad_menu__delete, DeleteEndClicked),
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun ScorePadScreen_Preview() {
    CodexTheme {
        ScorePadScreen(
                isRoundFull = false,
                displayDeleteEndConfirmationDialog = false,
                dropdownMenuOpenForEndNumber = null,
                data = ArcherRoundsPreviewHelper.SIMPLE.scorePadData,
        ) {}
    }
}