package eywa.projectcodex.components.shootDetails.scorePad

import androidx.annotation.StringRes
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.HelpShowcaseShape
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.grid.CodexGridWithHeaders
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.commonUi.HandleMainEffects
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsMainScreen
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadIntent.*
import eywa.projectcodex.model.scorePadData.ScorePadColumn
import eywa.projectcodex.model.scorePadData.ScorePadData.ScorePadColumnType
import eywa.projectcodex.model.scorePadData.ScorePadRow


private val COLUMN_ORDER = listOf(
        ScorePadColumnType.HEADER,
        ScorePadColumnType.ARROWS,
        ScorePadColumnType.HITS,
        ScorePadColumnType.SCORE,
        ScorePadColumnType.GOLDS,
        ScorePadColumnType.RUNNING_TOTAL,
)

@Composable
fun ScorePadScreen(
        navController: NavController,
        viewModel: ScorePadViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: ScorePadIntent -> viewModel.handle(it) }

    ShootDetailsMainScreen(
            currentScreen = CodexNavRoute.SHOOT_DETAILS_SCORE_PAD,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    ) { it, modifier -> ScorePadScreen(it, modifier, listener) }

    HandleMainEffects(
            navController = navController,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    )
    HandleEffects(navController, state, listener)
}

@Composable
fun HandleEffects(
        navController: NavController,
        state: ShootDetailsResponse<ScorePadState>,
        listener: (ScorePadIntent) -> Unit,
) {
    val loadedState = state.getData() ?: return
    LaunchedEffect(loadedState.insertEndClicked, loadedState.editEndClicked) {
        if (loadedState.insertEndClicked) {
            CodexNavRoute.SHOOT_DETAILS_INSERT_END.navigate(
                    navController,
                    mapOf(NavArgument.SHOOT_ID to loadedState.shootId.toString()),
            )
            listener(InsertEndHandled)
        }
        if (loadedState.editEndClicked) {
            CodexNavRoute.SHOOT_DETAILS_EDIT_END.navigate(
                    navController,
                    mapOf(NavArgument.SHOOT_ID to loadedState.shootId.toString()),
            )
            listener(EditEndHandled)
        }
    }
}

@Composable
private fun ScorePadScreen(
        state: ScorePadState,
        modifier: Modifier = Modifier,
        listener: (ScorePadIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    helpListener(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_score_pad__main_title),
                            helpBody = stringResource(R.string.help_score_pad__main_body),
                            shape = HelpShowcaseShape.NO_SHAPE,
                    )
            ),
    )

    SimpleDialog(
            isShown = state.scorePadData.isNullOrEmpty(),
            onDismissListener = { listener(NoArrowsDialogOkClicked) },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.archer_round_stats__no_arrows_dialog_title),
                positiveButton = ButtonState(
                        text = stringResource(R.string.archer_round_stats__no_arrows_dialog_button),
                        onClick = { listener(NoArrowsDialogOkClicked) },
                ),
        )
    }
    SimpleDialog(
            isShown = state.deleteEndDialogIsShown,
            onDismissListener = { listener(DeleteEndDialogCancelClicked) },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.score_pad_menu__delete_dialog_title),
                message = stringResource(
                        R.string.score_pad_menu__delete_dialog_body,
                        state.dropdownMenuOpenForEndNumber ?: -1,
                ),
                positiveButton = ButtonState(
                        text = stringResource(R.string.general_delete),
                        onClick = { listener(DeleteEndDialogOkClicked) },
                ),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener(DeleteEndDialogCancelClicked) },
                ),
        )
    }


    // TODO Make the row and column headers stick
    if (state.scorePadData.isNullOrEmpty()) {
        Box(
                modifier = modifier.testTag(ScorePadTestTag.SCREEN)
        )
        return
    }

    val columnMetadata = COLUMN_ORDER.flatMap { state.scorePadData.toColumnMetadata(it) }
    CodexGridWithHeaders(
            columnMetadata = columnMetadata,
            helpListener = helpListener,
            modifier = modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(5.dp)
                    .testTag(ScorePadTestTag.SCREEN)
    ) {
        state.scorePadData.data.forEach { row ->
            columnMetadata.forEach { column ->
                item(
                        backgroundColor = {
                            val isHeader = row !is ScorePadRow.End || column is ScorePadColumn.Header
                            if (isHeader) CodexTheme.colors.listAccentRowItemOnAppBackground
                            else CodexTheme.colors.listItemOnAppBackground
                        },
                ) {
                    val endNumber = (row as? ScorePadRow.End)?.endNumber

                    Box {
                        Cell(
                                rowData = row,
                                scorePadColumn = column,
                                listener = listener,
                        )
                        DropdownMenu(
                                isRoundFull = state.isRoundFull,
                                endNumber = endNumber ?: -1,
                                expanded = state.dropdownMenuOpenForEndNumber != null
                                        && state.isDropdownMenuOpen
                                        && column == ScorePadColumn.FixedData.ARROWS
                                        && endNumber == state.dropdownMenuOpenForEndNumber,
                                listener = listener,
                        )
                    }
                }
            }
        }
    }
}

/**
 * @param rowData null for the column headers
 * @param scorePadColumn null for the row headers
 */
@Composable
private fun Cell(
        modifier: Modifier = Modifier,
        rowData: ScorePadRow,
        scorePadColumn: ScorePadColumn,
        listener: (ScorePadIntent) -> Unit,
) {
    val isHeader = rowData !is ScorePadRow.End || scorePadColumn is ScorePadColumn.Header

    val clickModifier = if (rowData !is ScorePadRow.End) Modifier
    else Modifier.pointerInput(rowData) {
        detectTapGestures(
                onTap = { listener(RowClicked(rowData.endNumber)) },
                onLongPress = { listener(RowClicked(rowData.endNumber)) },
        )
    }

    val contentDescription = scorePadColumn.cellContentDescription(rowData, Unit)?.get()

    val customActions =
            if (rowData is ScorePadRow.End) {
                DropdownMenuItem.entries.map {
                    CustomAccessibilityAction(stringResource(it.title)) { listener(it.action(rowData.endNumber)); true }
                }
            }
            else null
    val semanticsModifier =
            if (contentDescription == null) Modifier.clearAndSetSemantics {
                if (customActions != null) this.customActions = customActions
            }
            else Modifier.semantics {
                this.contentDescription = contentDescription
                if (customActions != null) this.customActions = customActions
            }

    Text(
            text = scorePadColumn.mapping(rowData).get(),
            style = CodexTypography.NORMAL,
            textAlign = TextAlign.Center,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = CodexTheme.colors.onListItemAppOnBackground,
            modifier = modifier
                    .padding(vertical = 5.dp, horizontal = 10.dp)
                    .then(clickModifier)
                    .then(semanticsModifier)
                    .testTag(ScorePadTestTag.CELL)
    )
}

@Composable
private fun DropdownMenu(
        isRoundFull: Boolean,
        expanded: Boolean,
        endNumber: Int,
        listener: (ScorePadIntent) -> Unit
) {
    DropdownMenu(
            expanded = expanded,
            onDismissRequest = { listener(CloseDropdownMenu) },
    ) {
        DropdownMenuItem.entries.forEach { item ->
            if (isRoundFull && item == DropdownMenuItem.INSERT_END) return@forEach

            DropdownMenuItem(
                    onClick = {
                        check(endNumber > 0) { "Invalid end number" }
                        listener(item.action(endNumber))
                    },
                    modifier = Modifier.testTag(ScorePadTestTag.DROPDOWN_MENU_ITEM)
            ) {
                Text(
                        text = stringResource(id = item.title),
                        style = CodexTypography.NORMAL,
                )
            }
        }
    }
}

private enum class DropdownMenuItem(@StringRes val title: Int, val action: (endNumber: Int) -> ScorePadIntent) {
    EDIT_END(R.string.score_pad_menu__edit, { EditEndClicked(it) }),
    INSERT_END(R.string.score_pad_menu__insert, { InsertEndClicked(it) }),
    DELETE_END(R.string.score_pad_menu__delete, { DeleteEndClicked(it) }),
}

enum class ScorePadTestTag : CodexTestTag {
    SCREEN,
    CELL,
    DROPDOWN_MENU_ITEM,
    ;

    override val screenName: String
        get() = "SHOOT_DETAILS_SCORE_PAD"

    override fun getElement(): String = name
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        heightDp = 1200,
        widthDp = 450,
)
@Composable
fun York_ScorePadScreen_Preview() {
    val data = ShootPreviewHelperDsl.create {
        round = RoundPreviewHelper.yorkRoundData
        completeRoundWithFullSet()
    }
    CodexTheme {
        ScorePadScreen(
                ScorePadState(
                        main = ShootDetailsState(fullShootInfo = data, shootId = 1),
                        extras = ScorePadExtras(),
                ),
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        widthDp = 450,
)
@Composable
fun WA70_ScorePadScreen_Preview() {
    val data = ShootPreviewHelperDsl.create {
        round = RoundPreviewHelper.wa70RoundData
        completeRoundWithFullSet()
    }
    CodexTheme {
        ScorePadScreen(
                ScorePadState(
                        main = ShootDetailsState(fullShootInfo = data, shootId = 1),
                        extras = ScorePadExtras(),
                ),
        ) {}
    }
}
