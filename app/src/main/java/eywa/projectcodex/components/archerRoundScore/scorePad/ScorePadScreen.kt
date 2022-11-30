package eywa.projectcodex.components.archerRoundScore.scorePad

import androidx.annotation.StringRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.get
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent.ScorePadIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent.ScorePadIntent.*
import eywa.projectcodex.components.archerRoundScore.ArcherRoundPreviewHelper
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadDataNew
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadDataNew.ColumnHeader
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadRow
import kotlin.reflect.KClass


private val COLUMN_HEADER_ORDER = listOf(
        ColumnHeader.CONTENT,
        ColumnHeader.HITS,
        ColumnHeader.SCORE,
        ColumnHeader.GOLDS,
        ColumnHeader.RUNNING_TOTAL,
)

@Composable
fun ScorePadScreen(
        dropdownMenuOpenForEndNumber: Int?,
        data: ScorePadDataNew,
        listener: (ScorePadIntent) -> Unit,
) {
    val resources = LocalContext.current.resources

    // TODO Make the row and column headers stick
    Row(
            modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .padding(5.dp)
    ) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            // Placeholder for the first row which is the column header for other columns
            Cell(text = "", rowType = null, columnType = null)
            data.data.forEach { rowData ->
                val modifier = if (rowData !is ScorePadRow.End) Modifier
                else Modifier.clickable { listener(RowClicked(rowData.endNumber)) }

                Cell(
                        text = rowData.getRowHeader().get(),
                        rowType = rowData::class,
                        columnType = null,
                        modifier = modifier,
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
                        rowType = null,
                        columnType = columnHeader,
                )

                data.data.forEach { rowData ->
                    val modifier = if (rowData !is ScorePadRow.End) Modifier
                    else Modifier.clickable { listener(RowClicked(rowData.endNumber)) }

                    Box {
                        Cell(
                                text = rowData.getContent(columnHeader, resources),
                                rowType = rowData::class,
                                columnType = columnHeader,
                                modifier = modifier,
                        )
                        DropdownMenu(
                                expanded = dropdownMenuOpenForEndNumber != null
                                        && columnHeader == ColumnHeader.CONTENT
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
 * @param rowType null for the column headers row
 * @param columnType null for the row headers column
 */
@Composable
private fun Cell(
        text: String,
        rowType: KClass<out ScorePadRow>?,
        columnType: ColumnHeader?,
        modifier: Modifier = Modifier,
) {
    val isTotalRow = rowType != null && rowType != ScorePadRow.End::class
    val isHeaderOrTotal = isTotalRow || rowType == null || columnType == null

    val backgroundColour = when {
        isTotalRow -> CodexTheme.colors.listAccentRowItemOnAppBackground
        rowType == null && columnType == null -> null
        rowType != null && columnType != null -> CodexTheme.colors.listItemOnAppBackground
        else -> CodexTheme.colors.listAccentRowItemOnAppBackground
    }
    val backgroundModifier = backgroundColour?.let { Modifier.background(it) } ?: Modifier

    Text(
            text = text,
            style = CodexTypography.NORMAL.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = if (isHeaderOrTotal) FontWeight.Bold else FontWeight.Normal,
            ),
            modifier = modifier
                    .fillMaxWidth()
                    .padding(2.dp)
                    .then(backgroundModifier)
                    .padding(vertical = 5.dp, horizontal = 10.dp)
    )
}

@Composable
private fun DropdownMenu(
        expanded: Boolean,
        listener: (ScorePadIntent) -> Unit
) {
    DropdownMenu(
            expanded = expanded,
            onDismissRequest = { listener(CloseDropdownMenu) }
    ) {
        DropdownMenuItem.values().forEach { item ->
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
        ScorePadScreen(null, ArcherRoundPreviewHelper.SIMPLE.scorePadData) {}
    }
}