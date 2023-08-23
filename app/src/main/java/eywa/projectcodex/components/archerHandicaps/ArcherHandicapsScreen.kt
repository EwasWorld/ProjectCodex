package eywa.projectcodex.components.archerHandicaps

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsIntent.*
import eywa.projectcodex.database.archer.DatabaseArcherHandicap
import eywa.projectcodex.database.archer.HandicapType


@Composable
fun ArcherHandicapsScreen(
        viewModel: ArcherHandicapsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    ArcherHandicapsScreen(state) { viewModel.handle(it) }
}

@Composable
fun ArcherHandicapsScreen(
        state: ArcherHandicapsState,
        listener: (ArcherHandicapsIntent) -> Unit,
) {
    Column(
            verticalArrangement = Arrangement.spacedBy(25.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
    ) {
        Column(
                verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .padding(horizontal = 7.dp)
        ) {
            state.displayHandicaps.forEach {
                HandicapRow(
                        item = it,
                        state = state,
                        listener = listener,
                )
            }
            AddHandicapRow(state, listener)
        }
    }

    SelectHandicapTypeDialog(state, listener)
}

@Composable
private fun AddHandicapRow(
        state: ArcherHandicapsState,
        listener: (ArcherHandicapsIntent) -> Unit,
) {
    Surface(
            border = BorderStroke(3.dp, CodexTheme.colors.listItemOnAppBackground),
            color = Color.Transparent,
            modifier = Modifier
                    .fillMaxWidth()
    ) {
        ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
            ) {
                CodexIconButton(
                        icon = CodexIconInfo.VectorIcon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.archer_handicaps__add_submit),
                                tint = CodexTheme.colors.onAppBackground,
                                modifier = Modifier
                                        .scale(1.4f)
                                        .padding(13.dp)
                                        .fillMaxWidth()
                        ),
                        onClick = { listener(AddClicked) },
                )
                AnimatedVisibility(
                        visible = state.addDialogOpen,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                ) {
                    Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                    ) {
                        LabelledNumberSetting(
                                title = stringResource(R.string.archer_handicaps__handicap_header),
                                currentValue = state.addHandicap,
                                testTag = ArcherHandicapsTestTag.ADD_HANDICAP_VALUE.getTestTag(),
                                placeholder = "75",
                                errorMessage = state.handicapValidatorError,
                                onValueChanged = { listener(AddHandicapTextUpdated(it)) },
                                helpState = null,
                        )
                        NumberSettingErrorText(
                                errorText = state.handicapValidatorError,
                                testTag = ArcherHandicapsTestTag.ADD_HANDICAP_ERROR_TEXT,
                                modifier = Modifier.padding(top = 3.dp)
                        )
                        DataRow(
                                title = stringResource(R.string.archer_handicaps__handicap_type),
                                text = stringResource(state.addHandicapType.text),
                                helpState = null,
                                onClick = { listener(SelectHandicapTypeOpen) },
                                modifier = Modifier.padding(vertical = 12.dp)
                        )
                        AnimatedVisibility(
                                visible = state.handicapTypeDuplicateErrorShown,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically(),
                        ) {
                            Text(
                                    text = stringResource(R.string.archer_handicaps__add_duplicate_message),
                                    style = CodexTypography.SMALL.copy(color = CodexTheme.colors.onAppBackground),
                                    textAlign = TextAlign.Center,
                            )
                        }
                        CodexButton(
                                text = stringResource(R.string.archer_handicaps__add_submit),
                                buttonStyle = CodexButtonDefaults.DefaultOutlinedButton,
                                onClick = { listener(AddSubmit) },
                                helpState = null,
                                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HandicapRow(
        item: DatabaseArcherHandicap,
        state: ArcherHandicapsState,
        listener: (ArcherHandicapsIntent) -> Unit,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
                color = CodexTheme.colors.listItemOnAppBackground,
                onClick = { listener(RowClicked(item)) },
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
                            text = DateTimeFormat.SHORT_DATE.format(item.dateSet),
                            style = CodexTypography.SMALL.copy(color = CodexTheme.colors.onListItemLight),
                    )
                    Text(
                            text = stringResource(item.handicapType.text),
                            style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onListItemAppOnBackground),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(min = 140.dp)
                    )
                }
                Text(
                        text = item.handicap.toString(),
                        style = CodexTypography.LARGE.copy(color = CodexTheme.colors.onListItemAppOnBackground),
                )
            }
        }
        HandicapRowDropdownMenu(item, state, listener)
    }
}

@OptIn(ExperimentalMaterialApi::class)
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
        val customActions =
                ArcherHandicapsMenuItem.values().associateWith { it.asAccessibilityActions(resources, listener) }

        Surface(
                color = CodexTheme.colors.listItemOnAppBackground,
                onClick = { listener.takeIf { !state.editDialogOpen }?.invoke(EditClicked) },
                shape = RoundedCornerShape(0, 0, 20, 20),
                modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 5.dp, top = 2.dp)
                        .semantics {
                            if (!state.editDialogOpen) {
                                val action = customActions[ArcherHandicapsMenuItem.EDIT]!!
                                onClick(action.label, action.action)
                            }
                        }
        ) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                            .padding(top = 5.dp, bottom = 4.dp)
                            .animateContentSize()
            ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                                .widthIn(min = 140.dp)
                ) {
                    ArcherHandicapsMenuItem.EDIT.IconButton(listener)
                    AnimatedVisibility(
                            visible = state.editDialogOpen,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally(),
                    ) {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                        ) {
                            NumberSetting(
                                    contentDescription = stringResource(R.string.archer_handicaps__handicap_header),
                                    currentValue = state.addHandicap,
                                    testTag = ArcherHandicapsTestTag.EDIT_HANDICAP_VALUE.getTestTag(),
                                    placeholder = "75",
                                    errorMessage = state.handicapValidatorError,
                                    onValueChanged = { listener(AddHandicapTextUpdated(it)) },
                                    modifier = Modifier.padding(horizontal = 5.dp)
                            )
                            ArcherHandicapsMenuItem.EDIT_SUBMIT.IconButton(listener)
                        }
                    }
                }
                NumberSettingErrorText(
                        errorText = state.handicapValidatorError,
                        testTag = ArcherHandicapsTestTag.EDIT_HANDICAP_ERROR_TEXT,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                                .padding(horizontal = 15.dp)
                                .padding(bottom = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun SelectHandicapTypeDialog(
        state: ArcherHandicapsState,
        listener: (ArcherHandicapsIntent) -> Unit,
) {
    SimpleDialog(
            isShown = state.selectHandicapTypeDialogOpen,
            onDismissListener = { listener(SelectHandicapTypeDialogClose) },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.archer_handicaps__select_handicap_type_dialog_title),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener(SelectHandicapTypeDialogClose) },
                ),
                modifier = Modifier.testTag(ArcherHandicapsTestTag.ADD_SELECT_HANDICAP_TYPE_DIALOG.getTestTag())
        ) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                HandicapType.values().sortedBy { it.ordinal }.forEach {
                    Text(
                            text = stringResource(it.text),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                    .clickable { listener(SelectHandicapTypeDialogItemClicked(it)) }
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .testTag(ArcherHandicapsTestTag.ADD_SELECT_HANDICAP_TYPE_ITEM.getTestTag())
                    )
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
    EDIT(
            icon = CodexIconInfo.VectorIcon(imageVector = Icons.Default.Edit),
            contentDescription = R.string.archer_handicaps__menu_edit,
            intent = EditClicked,
            testTag = ArcherHandicapsTestTag.EDIT_MENU_ITEM,
    ),
    EDIT_SUBMIT(
            icon = CodexIconInfo.VectorIcon(imageVector = Icons.Default.Check),
            contentDescription = R.string.general_save,
            intent = EditSubmit,
            testTag = ArcherHandicapsTestTag.EDIT_SUBMIT,
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
    EDIT_MENU_ITEM,
    EDIT_SUBMIT,
    EDIT_HANDICAP_VALUE,
    EDIT_HANDICAP_ERROR_TEXT,
    ADD_HANDICAP_VALUE,
    ADD_HANDICAP_ERROR_TEXT,
    ADD_SELECT_HANDICAP_TYPE_DIALOG,
    ADD_SELECT_HANDICAP_TYPE_ITEM,
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
fun ArcherHandicapsScreen_Preview() {
    ArcherHandicapsPreviewHelper.Display(
            ArcherHandicapsState(
                    archerHandicaps = ArcherHandicapsPreviewHelper.handicaps,
                    menuShownForId = 2,
            )
    )
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun AddOpen_ArcherHandicapsScreen_Preview() {
    ArcherHandicapsPreviewHelper.Display(
            ArcherHandicapsState(
                    archerHandicaps = ArcherHandicapsPreviewHelper.handicaps,
                    menuShownForId = 2,
                    addDialogOpen = true,
                    addHandicapType = HandicapType.INDOOR_TOURNAMENT,
                    editDialogOpen = true,
            )
    )
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Errors_ArcherHandicapsScreen_Preview() {
    ArcherHandicapsPreviewHelper.Display(
            ArcherHandicapsState(
                    archerHandicaps = ArcherHandicapsPreviewHelper.handicaps,
                    menuShownForId = 2,
                    addDialogOpen = true,
                    editDialogOpen = true,
                    addHandicap = "200",
            )
    )
}
