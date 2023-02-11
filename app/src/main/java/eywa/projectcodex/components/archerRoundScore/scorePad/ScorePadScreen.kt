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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseMap
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
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
import eywa.projectcodex.components.archerRoundScore.ArcherRoundSubScreen
import eywa.projectcodex.components.archerRoundScore.ArcherRoundsPreviewHelper
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadDataNew.ColumnHeader
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadDataNew.ScorePadRow
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState


private val COLUMN_HEADER_ORDER = listOf(
        ColumnHeader.ARROWS,
        ColumnHeader.HITS,
        ColumnHeader.SCORE,
        ColumnHeader.GOLDS,
        ColumnHeader.RUNNING_TOTAL,
)

@Composable
fun ColumnHeader.getHelpTitle() = stringResource(
        when (this) {
            ColumnHeader.ARROWS -> R.string.help_score_pad__arrow_column_title
            ColumnHeader.HITS -> R.string.help_score_pad__hits_column_title
            ColumnHeader.SCORE -> R.string.help_score_pad__score_column_title
            ColumnHeader.GOLDS -> R.string.help_score_pad__golds_column_title
            ColumnHeader.RUNNING_TOTAL -> R.string.help_score_pad__running_column_title
        }
)

@Composable
fun ColumnHeader.getHelpBody(goldsType: GoldsType) =
        when (this) {
            ColumnHeader.ARROWS -> stringResource(R.string.help_score_pad__arrow_column_body)
            ColumnHeader.HITS -> stringResource(R.string.help_score_pad__hits_column_body)
            ColumnHeader.SCORE -> stringResource(R.string.help_score_pad__score_column_body)
            ColumnHeader.GOLDS ->
                stringResource(R.string.help_score_pad__golds_column_body, stringResource(goldsType.helpString))
            ColumnHeader.RUNNING_TOTAL -> stringResource(R.string.help_score_pad__running_column_body)
        }

class ScorePadScreen : ArcherRoundSubScreen() {
    private val helpInfo = ComposeHelpShowcaseMap().apply {
        // TODO Implement no shape help info
//        add(
//                ComposeHelpShowcaseItem(
//                        helpTitle = R.string.help_score_pad__main_title,
//                        helpBody = R.string.help_score_pad__main_body,
//                )
//        )
    }

    @Composable
    override fun ComposeContent(
            state: ArcherRoundState.Loaded,
            listener: (ArcherRoundIntent) -> Unit,
    ) {
        ScreenContent(state, listener)
    }

    override fun getHelpShowcases() = helpInfo.getItems()

    override fun getHelpPriority(): Int? = null

    @Composable
    private fun ScreenContent(
            state: ScorePadState,
            listener: (ArcherRoundIntent) -> Unit,
    ) {
        SimpleDialog(
                isShown = state.scorePadData.isNullOrEmpty(),
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
                isShown = state.displayDeleteEndConfirmationDialog,
                onDismissListener = { listener(ArcherRoundIntent.DeleteEndDialogCancelClicked) },
        ) {
            SimpleDialogContent(
                    title = stringResource(R.string.score_pad_menu__delete_dialog_title),
                    message = stringResource(
                            R.string.score_pad_menu__delete_dialog_body,
                            state.dropdownMenuOpenForEndNumber ?: -1
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

        if (state.scorePadData.isNullOrEmpty()) return

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
                state.scorePadData.data.forEach { rowData ->
                    Cell(
                            rowData = rowData,
                            listener = listener,
                    )
                }
            }

            COLUMN_HEADER_ORDER.forEach { columnHeader ->
                helpInfo.add(
                        ComposeHelpShowcaseItem(
                                helpTitle = columnHeader.getHelpTitle(),
                                helpBody = columnHeader.getHelpBody(state.scorePadData.goldsType),
                        )
                )

                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    Cell(
                            text = stringResource(columnHeader.getShortResourceId(state.scorePadData.goldsType)),
                            columnType = columnHeader,
                            listener = listener,
                            modifier = Modifier.updateHelpDialogPosition(helpInfo, columnHeader.getHelpTitle())
                    )

                    state.scorePadData.data.forEach { rowData ->
                        Box {
                            Cell(
                                    rowData = rowData,
                                    columnType = columnHeader,
                                    listener = listener,
                            )
                            DropdownMenu(
                                    isRoundFull = state.isRoundFull,
                                    expanded = state.dropdownMenuOpenForEndNumber != null
                                            && columnHeader == ColumnHeader.ARROWS
                                            && (rowData as? ScorePadRow.End)?.endNumber == state.dropdownMenuOpenForEndNumber,
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
            modifier: Modifier = Modifier,
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
                modifier = modifier
                        .fillMaxWidth()
                        .padding(2.dp)
                        .then(backgroundModifier)
                        .padding(vertical = 5.dp, horizontal = 10.dp)
                        .then(clickModifier)
                        .testTag(TestTag.CELL)
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
                        modifier = Modifier.testTag(TestTag.DROPDOWN_MENU_ITEM)
                ) {
                    Text(
                            text = stringResource(id = item.title),
                            style = CodexTypography.NORMAL,
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

    object TestTag {
        private const val PREFIX = "SCORE_PAD_"
        const val CELL = "${PREFIX}CELL"
        const val DROPDOWN_MENU_ITEM = "${PREFIX}DROPDOWN_MENU_ITEM"
    }

    @Preview(
            showBackground = true,
            backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
    )
    @Composable
    fun ScorePadScreen_Preview() {
        CodexTheme {
            ScreenContent(ArcherRoundsPreviewHelper.WITH_SHOT_ARROWS) {}
        }
    }
}
