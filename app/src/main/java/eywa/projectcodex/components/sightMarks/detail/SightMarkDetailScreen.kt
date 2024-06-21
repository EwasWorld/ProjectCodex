package eywa.projectcodex.components.sightMarks.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.CODEX_CHIP_SPACING
import eywa.projectcodex.common.sharedUi.CodexChip
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.CodexTextField
import eywa.projectcodex.common.sharedUi.CodexTextFieldRoundedSurface
import eywa.projectcodex.common.sharedUi.CodexTextFieldState
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.numberField.CodexLabelledNumberField
import eywa.projectcodex.common.sharedUi.numberField.CodexNumberFieldErrorText
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailIntent.*
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailTestTag.*
import eywa.projectcodex.database.rounds.getDistanceUnitRes
import eywa.projectcodex.model.SightMark
import java.util.Calendar

@Composable
fun SightMarkDetailScreen(
        navController: NavController,
        viewModel: SightMarkDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: SightMarkDetailIntent -> viewModel.handle(it) }
    SightMarkDetailScreen(state, listener)

    LaunchedEffect(state) { handleEffects(state, navController, listener) }
}

private fun handleEffects(
        state: SightMarkDetailState?,
        navController: NavController,
        listener: (SightMarkDetailIntent) -> Unit,
) {
    if (state == null) return

    if (state.closeScreen) {
        listener(CloseHandled)
        navController.popBackStack()
    }
}

@Composable
fun SightMarkDetailScreen(
        state: SightMarkDetailState?,
        listener: (SightMarkDetailIntent) -> Unit,
) {
    Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                    .fillMaxSize()
                    .background(CodexTheme.colors.appBackground)
                    .verticalScroll(rememberScrollState())
                    .testTag(SCREEN)
    ) {
        if (state == null) {
            Text(
                    text = stringResource(R.string.sight_marks__detail_loading),
                    style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
            )
        }
        else {
            SightMarkDetail(state, listener)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SightMarkDetail(
        state: SightMarkDetailState,
        listener: (SightMarkDetailIntent) -> Unit,
) {
    var isDeleteConfirmationShown by remember { mutableStateOf(false) }
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    SimpleDialog(
            isShown = isDeleteConfirmationShown,
            onDismissListener = { isDeleteConfirmationShown = false },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.sight_marks__delete_confirmation_title),
                message = stringResource(
                        R.string.sight_marks__delete_confirmation_body,
                        state.originalSightMark?.distance ?: state.distance.toIntOrNull() ?: 0,
                        stringResource(getDistanceUnitRes(state.originalSightMark?.isMetric ?: true)!!),
                ),
                positiveButton = ButtonState(
                        text = stringResource(R.string.general_delete),
                        onClick = { listener(DeleteClicked) },
                ),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { isDeleteConfirmationShown = false },
                ),
        )
    }

    ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
        ) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CodexLabelledNumberField(
                        title = stringResource(R.string.sight_marks__sight),
                        currentValue = state.sightMark,
                        errorMessage = state.sightMarkValidatorError,
                        placeholder = "2.3",
                        selectAllOnFocus = false,
                        testTag = SIGHT,
                        onValueChanged = { listener(SightMarkUpdated(it ?: "")) },
                        helpState = HelpShowcaseItem(
                                helpTitle = stringResource(R.string.help_sight_marks__sight_title),
                                helpBody = stringResource(R.string.help_sight_marks__sight_body),
                        ).asHelpState(helpListener)
                )
                CodexNumberFieldErrorText(
                        errorText = state.sightMarkValidatorError,
                        testTag = SIGHT_ERROR_TEXT,
                )
            }
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    // TODO help bubble should wrap units too, maybe even error?
                    CodexLabelledNumberField(
                            title = stringResource(R.string.sight_marks__distance),
                            currentValue = state.distance,
                            errorMessage = state.distanceValidatorError,
                            placeholder = "50",
                            selectAllOnFocus = false,
                            testTag = DISTANCE,
                            onValueChanged = { listener(DistanceUpdated(it ?: "")) },
                            helpState = HelpShowcaseItem(
                                    helpTitle = stringResource(R.string.help_sight_marks__distance_title),
                                    helpBody = stringResource(R.string.help_sight_marks__distance_body),
                            ).asHelpState(helpListener)
                    )

                    val unitContentDescription = stringResource(
                            if (state.isMetric) R.string.units_meters
                            else R.string.units_yards
                    )
                    Text(
                            text = stringResource(getDistanceUnitRes(state.isMetric)!!),
                            color = CodexTheme.colors.linkText,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                    .clickable { listener(ToggleIsMetric) }
                                    .testTag(DISTANCE_UNIT)
                                    .semantics {
                                        contentDescription = unitContentDescription
                                    }
                    )
                }
                CodexNumberFieldErrorText(
                        errorText = state.distanceValidatorError,
                        testTag = DISTANCE_ERROR_TEXT,
                )
            }

            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                state.originalSightMark?.dateSet?.let { dateSet ->
                    DataRow(
                            title = stringResource(R.string.sight_marks__date_set),
                            text = DateTimeFormat.SHORT_DATE.format(dateSet),
                            helpState = HelpShowcaseItem(
                                    helpTitle = stringResource(R.string.help_sight_marks__date_title),
                                    helpBody = stringResource(R.string.help_sight_marks__date_body),
                            ).asHelpState(helpListener),
                            textModifier = Modifier
                                    .clickable { listener(ToggleUpdateDateSet) }
                                    .testTag(DATE)
                    )
                }
                if (state.originalSightMark != null) {
                    Text(
                            text = stringResource(
                                    if (state.updateDateSet) R.string.sight_marks__update_date_set_true
                                    else R.string.sight_marks__update_date_set_false
                            ),
                            style = CodexTypography.SMALL
                                    .copy(color = CodexTheme.colors.onAppBackground)
                                    .asClickableStyle(),
                            modifier = Modifier
                                    .clickable { listener(ToggleUpdateDateSet) }
                                    .updateHelpDialogPosition(
                                            HelpShowcaseItem(
                                                    helpTitle = stringResource(R.string.help_sight_marks__update_date_set_title),
                                                    helpBody = stringResource(R.string.help_sight_marks__update_date_set_body),
                                            ).asHelpState(helpListener),
                                    )
                    )
                }
            }
            FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(CODEX_CHIP_SPACING),
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
            ) {
                CodexChip(
                        text = stringResource(R.string.sight_marks__marked),
                        selected = state.isMarked,
                        testTag = MARKED,
                        onToggle = { listener(ToggleIsMarked) },
                        helpState = HelpShowcaseItem(
                                helpTitle = stringResource(R.string.help_sight_marks__marked_title),
                                helpBody = stringResource(R.string.help_sight_marks__marked_body),
                        ).asHelpState(helpListener),
                        modifier = Modifier.align(Alignment.CenterVertically)
                )
                CodexChip(
                        text = stringResource(R.string.sight_marks__archived),
                        selected = state.isArchived,
                        testTag = ARCHIVED,
                        onToggle = { listener(ToggleIsArchived) },
                        helpState = HelpShowcaseItem(
                                helpTitle = stringResource(R.string.help_sight_marks__archived_title),
                                helpBody = stringResource(R.string.help_sight_marks__archived_body),
                        ).asHelpState(helpListener),
                        modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            CodexTextFieldRoundedSurface {
                CodexTextField(
                        state = CodexTextFieldState(
                                text = state.note ?: "",
                                onValueChange = { listener(NoteUpdated(it)) },
                                testTag = NOTE,
                        ),
                        placeholderText = stringResource(R.string.sight_marks__note_placeholder),
                        labelText = stringResource(R.string.sight_marks__note),
                        helpState = HelpShowcaseItem(
                                helpTitle = stringResource(R.string.help_sight_marks__note_title),
                                helpBody = stringResource(R.string.help_sight_marks__note_body),
                        ).asHelpState(helpListener),
                        modifier = Modifier.padding(5.dp)
                )
            }
            FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
            ) {
                if (state.originalSightMark != null) {
                    CodexIconButton(
                            icon = CodexIconInfo.VectorIcon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.general_delete),
                            ),
                            captionBelow = stringResource(R.string.general_delete),
                            onClick = { isDeleteConfirmationShown = true },
                            helpState = HelpShowcaseItem(
                                    helpTitle = stringResource(R.string.help_sight_marks__delete_title),
                                    helpBody = stringResource(R.string.help_sight_marks__delete_body),
                            ).asHelpState(helpListener),
                            modifier = Modifier
                                    .testTag(DELETE_BUTTON)
                                    .align(Alignment.CenterVertically)
                    )
                    CodexIconButton(
                            icon = CodexIconInfo.VectorIcon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.general__reset_edits),
                            ),
                            captionBelow = stringResource(R.string.general__reset_edits),
                            onClick = { listener(ResetClicked) },
                            helpState = HelpShowcaseItem(
                                    helpTitle = stringResource(R.string.help_sight_marks__reset_title),
                                    helpBody = stringResource(R.string.help_sight_marks__reset_body),
                            ).asHelpState(helpListener),
                            modifier = Modifier
                                    .testTag(RESET_BUTTON)
                                    .align(Alignment.CenterVertically)
                    )
                }
                CodexIconButton(
                        icon = CodexIconInfo.VectorIcon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.general_save),
                        ),
                        captionBelow = stringResource(R.string.general_save),
                        onClick = { listener(SaveClicked) },
                        enabled = state.isFormValid,
                        helpState = HelpShowcaseItem(
                                helpTitle = stringResource(R.string.help_sight_marks__save_title),
                                helpBody = stringResource(R.string.help_sight_marks__save_body),
                        ).asHelpState(helpListener),
                        modifier = Modifier
                                .testTag(SAVE_BUTTON)
                                .align(Alignment.CenterVertically)
                )
            }
        }
    }
}


enum class SightMarkDetailTestTag : CodexTestTag {
    SCREEN,
    SIGHT,
    SIGHT_ERROR_TEXT,
    DISTANCE,
    DISTANCE_ERROR_TEXT,
    DISTANCE_UNIT,
    DATE,
    MARKED,
    ARCHIVED,
    NOTE,
    SAVE_BUTTON,
    RESET_BUTTON,
    DELETE_BUTTON,
    ;

    override val screenName: String
        get() = "SIGHT_MARK_DETAIL"

    override fun getElement(): String = name
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun SightMarkDetail_Preview() {
    SightMarkDetailScreen(
            SightMarkDetailState.fromOriginalSightMark(
                    SightMark(
                            id = 0,
                            distance = 50,
                            isMetric = false,
                            dateSet = Calendar.getInstance(),
                            sightMark = 2.3f,
                            note = "This is a note",
                            isMarked = false,
                    )
            )
    ) {}
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Error_SightMarkDetail_Preview() {
    SightMarkDetailScreen(
            SightMarkDetailState(
                    distance = "-50",
                    isMetric = false,
                    sightMark = "hi",
                    note = "This is a note",
                    isMarked = false,
            )
    ) {}
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Loading_SightMarkDetail_Preview() {
    SightMarkDetailScreen(null) {}
}
