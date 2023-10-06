package eywa.projectcodex.components.archerHandicaps

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsIntent.*
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsMenuItem.DELETE
import eywa.projectcodex.components.archerHandicaps.add.ArcherHandicapsBottomSheetAdd
import eywa.projectcodex.database.archer.DatabaseArcherHandicap


@Composable
fun ArcherHandicapsScreen(
        navController: NavController,
        viewModel: ArcherHandicapsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    if (state.isLoaded) ArcherHandicapsScreen(state) { viewModel.handle(it) }
    else LoadingScreen()

    LaunchedEffect(state.openAddDialog) {
        if (state.openAddDialog) {
            ArcherHandicapsBottomSheetAdd.navigate(navController)
            viewModel.handle(AddHandled)
        }
    }
}

@Composable
fun ArcherHandicapsScreen(
        state: ArcherHandicapsState,
        listener: (ArcherHandicapsIntent) -> Unit,
) {
    Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.testTag(ArcherHandicapsTestTag.SCREEN.getTestTag())
    ) {
        LazyColumn(
                verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 70.dp),
                modifier = Modifier.fillMaxSize()
        ) {
            if (state.handicapsForDisplay.isEmpty()) {
                item {
                    Text(
                            text = stringResource(R.string.archer_handicaps__no_handicaps_message),
                            style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag(ArcherHandicapsTestTag.NO_HANDICAPS_MESSAGE)
                    )
                }
            }

            items(
                    count = state.handicapsForDisplay.size,
                    key = { state.handicapsForDisplay[it].archerHandicapId },
            ) {
                HandicapRow(
                        index = it,
                        state = state,
                        listener = listener,
                )
            }
        }

        CodexFloatingActionButton(
                icon = CodexIconInfo.VectorIcon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.archer_handicaps__add_submit),
                ),
                onClick = { listener(AddClicked) },
                modifier = Modifier
                        .padding(20.dp)
                        .testTag(ArcherHandicapsTestTag.ADD_BUTTON)
        )
    }


    DeleteDialog(
            handicapForDeletion = state.handicapForDeletion,
            listener = listener,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HandicapRow(
        index: Int,
        state: ArcherHandicapsState,
        listener: (ArcherHandicapsIntent) -> Unit,
) {
    val item = state.handicapsForDisplay[index]
    val currentIds = state.currentHandicaps.orEmpty().map { it.archerHandicapId }
    val isFirstNonCurrentHandicap = !currentIds.contains(item.archerHandicapId)
            && currentIds.contains(state.handicapsForDisplay[index - 1].archerHandicapId)

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                    .fillMaxWidth()
                    .testTag(ArcherHandicapsTestTag.ROW)
    ) {
        if (index == 0 || isFirstNonCurrentHandicap) {
            Text(
                    text =
                    stringResource(
                            if (index == 0) R.string.archer_handicaps__current_separator
                            else R.string.archer_handicaps__past_separator
                    ),
                    style = CodexTypography.LARGE.copy(color = CodexTheme.colors.onAppBackground),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                            .padding(top = 15.dp, bottom = 10.dp)
                            .testTag(ArcherHandicapsTestTag.LIST_HEADER)
            )
        }
        Surface(
                color = CodexTheme.colors.listItemOnAppBackground,
                onClick = { listener(RowClicked(item)) },
                modifier = Modifier.testTag(ArcherHandicapsTestTag.ROW_LIST_ITEM)
        ) {
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(30.dp),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Column(
                        modifier = Modifier.weight(1f)
                ) {
                    Text(
                            text = DateTimeFormat.TIME_24_HOUR.format(item.dateSet),
                            style = CodexTypography.SMALL.copy(color = CodexTheme.colors.onListItemLight),
                            modifier = Modifier.testTag(ArcherHandicapsTestTag.ROW_TIME)
                    )
                    Text(
                            text = DateTimeFormat.SHORT_DATE.format(item.dateSet),
                            style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onListItemAppOnBackground),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                    .widthIn(min = 140.dp)
                                    .testTag(ArcherHandicapsTestTag.ROW_DATE)
                    )
                }
                Text(
                        text = item.handicap.toString(),
                        style = CodexTypography.LARGE.copy(color = CodexTheme.colors.onListItemAppOnBackground),
                        modifier = Modifier.testTag(ArcherHandicapsTestTag.ROW_HANDICAP)
                )
            }
        }
        HandicapRowDropdownMenu(item, state, listener)
    }
}

@Composable
private fun DeleteDialog(
        handicapForDeletion: DatabaseArcherHandicap?,
        listener: (ArcherHandicapsIntent) -> Unit,
) {
    val message = handicapForDeletion?.dateSet
            ?.let {
                stringResource(
                        R.string.archer_handicap__delete_dialog_body,
                        DateTimeFormat.SHORT_DATE_TIME.format(it)
                )
            }
            ?: stringResource(R.string.archer_handicap__delete_dialog_body_generic)

    SimpleDialog(
            isShown = handicapForDeletion != null,
            onDismissListener = { listener(DeleteDialogCancelClicked) },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.archer_handicap__delete_dialog_title),
                message = message,
                positiveButton = ButtonState(
                        text = stringResource(R.string.general_delete),
                        onClick = { listener(DeleteDialogOkClicked) },
                ),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener(DeleteDialogCancelClicked) },
                ),
        )
    }
}

@Composable
private fun HandicapRowDropdownMenu(
        item: DatabaseArcherHandicap,
        state: ArcherHandicapsState,
        listener: (ArcherHandicapsIntent) -> Unit,
) {
    AnimatedVisibility(
            visible = item.archerHandicapId == state.menuShownForId,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
    ) {
        val resources = LocalContext.current.resources

        Surface(
                color = CodexTheme.colors.listItemOnAppBackground,
                shape = RoundedCornerShape(0, 0, 20, 20),
                modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 5.dp, top = 2.dp)
                        .clickable(
                                onClickLabel = DELETE.asAccessibilityActions(resources, listener).label,
                                onClick = { listener(DELETE.intent) },
                        )
        ) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                            .padding(top = 5.dp, bottom = 4.dp)
                            .animateContentSize()
                            .clearAndSetSemantics { }
            ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                                .widthIn(min = 140.dp)
                ) {
                    DELETE.IconButton(listener)
                }
            }
        }
    }
}

private enum class ArcherHandicapsMenuItem(
        val icon: CodexIconInfo,
        @StringRes val contentDescription: Int,
        val intent: ArcherHandicapsIntent,
        val testTag: CodexTestTag,
) {
    DELETE(
            icon = CodexIconInfo.VectorIcon(imageVector = Icons.Default.Delete),
            contentDescription = R.string.general_delete,
            intent = DeleteClicked,
            testTag = ArcherHandicapsTestTag.DELETE_BUTTON,
    ),
    ;

    fun asAccessibilityActions(
            resources: Resources,
            listener: (ArcherHandicapsIntent) -> Unit,
    ) = CustomAccessibilityAction(resources.getString(contentDescription)) { listener(intent); true }

    @Composable
    fun IconButton(
            isShown: Boolean,
            listener: (ArcherHandicapsIntent) -> Unit,
    ) = AnimatedVisibility(
            visible = isShown,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
    ) { IconButton(listener) }

    @Composable
    fun IconButton(
            listener: (ArcherHandicapsIntent) -> Unit,
    ) = CodexIconButton(
            icon = icon.copyIcon(
                    contentDescription = stringResource(contentDescription),
                    tint = CodexTheme.colors.iconButtonOnListItem,
            ),
            onClick = { listener(intent) },
            modifier = Modifier.testTag(testTag.getTestTag())
    )
}

enum class ArcherHandicapsTestTag : CodexTestTag {
    SCREEN,
    NO_HANDICAPS_MESSAGE,
    LIST_HEADER,
    ROW,
    ROW_LIST_ITEM,
    ROW_TIME,
    ROW_DATE,
    ROW_HANDICAP,
    DELETE_BUTTON,
    ADD_BUTTON,
    ADD_HANDICAP_VALUE,
    ADD_HANDICAP_ERROR_TEXT,
    ADD_HANDICAP_SUBMIT,
    ;

    override val screenName: String
        get() = "ARCHER_HANDICAPS"

    override fun getElement(): String = name
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun ArcherHandicapsScreen_Preview(
        @PreviewParameter(ArcherHandicapsScreenPreviewParamProvider::class) param: ArcherHandicapsState
) {
    var state by remember { mutableStateOf(param) }
    val context = LocalContext.current

    CodexTheme {
        ArcherHandicapsScreen(state) { action ->
            when (action) {
                is RowClicked ->
                    state = state.copy(
                            menuShownForId = action.item.archerHandicapId.takeIf { state.menuShownForId != it }
                    )

                else -> Unit // ToastSpamPrevention.displayToast(context, action::class.simpleName.toString())
            }
        }
    }
}

class ArcherHandicapsScreenPreviewParamProvider : CollectionPreviewParameterProvider<ArcherHandicapsState>(
        listOf(
                ArcherHandicapsState(
                        currentHandicaps = ArcherHandicapsPreviewHelper.handicaps.take(1),
                        allHandicaps = ArcherHandicapsPreviewHelper.handicaps.drop(1),
                ),
                ArcherHandicapsState(
                        currentHandicaps = ArcherHandicapsPreviewHelper.handicaps.take(1),
                ),
                ArcherHandicapsState(),
                ArcherHandicapsState(
                        currentHandicaps = ArcherHandicapsPreviewHelper.handicaps.take(1),
                        allHandicaps = ArcherHandicapsPreviewHelper.handicaps.drop(1),
                        menuShownForId = 2,
                        openAddDialog = true,
                ),
                ArcherHandicapsState(
                        currentHandicaps = ArcherHandicapsPreviewHelper.handicaps.take(1),
                        allHandicaps = ArcherHandicapsPreviewHelper.handicaps.drop(1),
                        menuShownForId = 2,
                        openAddDialog = true,
                ),
        )
)
