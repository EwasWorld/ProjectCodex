package eywa.projectcodex.components.archerRoundScore.scorePad

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import eywa.projectcodex.common.archeryObjects.ScorePadData.ColumnHeader
import eywa.projectcodex.common.archeryObjects.ScorePadData.ScorePadRow
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
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
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundStatePreviewHelper


private val COLUMN_HEADER_ORDER = listOf(
        ColumnHeader.ARROWS,
        ColumnHeader.HITS,
        ColumnHeader.SCORE,
        ColumnHeader.GOLDS,
        ColumnHeader.RUNNING_TOTAL,
)

class ScorePadScreen : ArcherRoundSubScreen() {
    // TODO Implement no shape help info
//        add(
//                ComposeHelpShowcaseItem(
//                        helpTitle = R.string.help_score_pad__main_title,
//                        helpBody = R.string.help_score_pad__main_body,
//                )
//        )

    @Composable
    override fun ComposeContent(
            state: ArcherRoundState.Loaded,
            modifier: Modifier,
            listener: (ArcherRoundIntent) -> Unit,
    ) {
        ScreenContent(state, modifier, listener)
    }

    @Composable
    private fun ScreenContent(
            state: ScorePadState,
            modifier: Modifier = Modifier,
            listener: (ArcherRoundIntent) -> Unit,
    ) {
        val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

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


        // TODO Make the row and column headers stick
        Row(
                verticalAlignment = Alignment.Top,
                modifier = modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(5.dp)
                        .testTag(TestTag.SCREEN)
        ) {
            if (!state.scorePadData.isNullOrEmpty()) {
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
                    helpListener(
                            HelpShowcaseIntent.Add(
                                    HelpShowcaseItem(
                                            helpTitle = columnHeader.getHelpTitle(),
                                            helpBody = columnHeader.getHelpBody(state.scorePadData.goldsType),
                                    )
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
                                modifier = Modifier.updateHelpDialogPosition(helpListener, columnHeader.getHelpTitle())
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
        const val SCREEN = "${PREFIX}SCREEN"
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
            ScreenContent(ArcherRoundStatePreviewHelper.WITH_SHOT_ARROWS) {}
        }
    }
}
