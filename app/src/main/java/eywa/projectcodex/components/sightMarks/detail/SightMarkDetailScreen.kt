package eywa.projectcodex.components.sightMarks.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailIntent.*
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailTestTag.*
import eywa.projectcodex.database.rounds.getDistanceUnitRes
import eywa.projectcodex.model.SightMark
import java.util.*

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
                    .testTag(SCREEN.getTestTag())
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
                NumberSetting(
                        clazz = String::class,
                        title = stringResource(R.string.sight_marks__sight),
                        currentValue = state.sightMark,
                        errorMessage = state.sightMarkValidatorError?.let { stringResource(it) },
                        placeholder = "2.3",
                        testTag = SIGHT.getTestTag(),
                        onValueChanged = { listener(SightMarkUpdated(it ?: "")) },
                        helpState = HelpState(
                                helpListener = helpListener,
                                helpTitle = stringResource(R.string.help_sight_marks__sight_title),
                                helpBody = stringResource(R.string.help_sight_marks__sight_body),
                        ),
                )
                if (state.sightMarkValidatorError != null) {
                    Text(
                            text = stringResource(state.sightMarkValidatorError),
                            style = CodexTypography.SMALL,
                            color = CodexTheme.colors.errorOnAppBackground,
                            modifier = Modifier
                                    .testTag(SIGHT_ERROR_TEXT.getTestTag())
                                    .clearAndSetSemantics { }
                    )
                }
            }
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    // TODO help bubble should wrap units too, maybe even error?
                    NumberSetting(
                            clazz = String::class,
                            title = stringResource(R.string.sight_marks__distance),
                            currentValue = state.distance,
                            errorMessage = state.distanceValidatorError?.let { stringResource(it) },
                            placeholder = "50",
                            testTag = DISTANCE.getTestTag(),
                            onValueChanged = { listener(DistanceUpdated(it ?: "")) },
                            helpState = HelpState(
                                    helpListener = helpListener,
                                    helpTitle = stringResource(R.string.help_sight_marks__distance_title),
                                    helpBody = stringResource(R.string.help_sight_marks__distance_body),
                            ),
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
                                    .testTag(DISTANCE_UNIT.getTestTag())
                                    .semantics {
                                        contentDescription = unitContentDescription
                                    }
                    )
                }
                if (state.distanceValidatorError != null) {
                    Text(
                            text = stringResource(state.distanceValidatorError),
                            style = CodexTypography.SMALL,
                            color = CodexTheme.colors.errorOnAppBackground,
                            modifier = Modifier
                                    .testTag(DISTANCE_ERROR_TEXT.getTestTag())
                                    .clearAndSetSemantics { }
                    )
                }
            }

            state.originalSightMark?.dateSet?.let { dateSet ->
                DataRow(
                        title = stringResource(R.string.sight_marks__date_set),
                        text = DateTimeFormat.SHORT_DATE.format(dateSet),
                        helpState = HelpState(
                                helpListener = helpListener,
                                helpTitle = stringResource(R.string.help_sight_marks__date_title),
                                helpBody = stringResource(R.string.help_sight_marks__date_body),
                        ),
                        textModifier = Modifier.testTag(DATE.getTestTag())
                )
            }
            FlowRow(
                    mainAxisSpacing = CODEX_CHIP_SPACING,
                    mainAxisAlignment = FlowMainAxisAlignment.Center,
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
            ) {
                CodexChip(
                        text = stringResource(R.string.sight_marks__marked),
                        selected = state.isMarked,
                        testTag = MARKED.getTestTag(),
                        onToggle = { listener(ToggleIsMarked) },
                        helpState = HelpState(
                                helpListener,
                                stringResource(R.string.help_sight_marks__marked_title),
                                stringResource(R.string.help_sight_marks__marked_body),
                        ),
                )
                CodexChip(
                        text = stringResource(R.string.sight_marks__archived),
                        selected = state.isArchived,
                        testTag = ARCHIVED.getTestTag(),
                        onToggle = { listener(ToggleIsArchived) },
                        helpState = HelpState(
                                helpListener,
                                stringResource(R.string.help_sight_marks__archived_title),
                                stringResource(R.string.help_sight_marks__archived_body),
                        ),
                )
            }
            CodexTextFieldRoundedSurface {
                CodexTextField(
                        state = CodexTextFieldState(
                                text = state.note ?: "",
                                onValueChange = { listener(NoteUpdated(it)) },
                                testTag = NOTE.getTestTag(),
                        ),
                        placeholderText = stringResource(R.string.sight_marks__note_placeholder),
                        labelText = stringResource(R.string.sight_marks__note),
                        helpState = HelpState(
                                helpListener,
                                stringResource(R.string.help_sight_marks__note_title),
                                stringResource(R.string.help_sight_marks__note_body),
                        ),
                        modifier = Modifier.padding(5.dp)
                )
            }
            FlowRow(
                    mainAxisSpacing = 5.dp,
                    mainAxisAlignment = FlowMainAxisAlignment.Center,
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
            ) {
                if (state.originalSightMark != null) {
                    CodexIconButton(
                            icon = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.general_delete),
                            captionBelow = stringResource(R.string.general_delete),
                            onClick = { isDeleteConfirmationShown = true },
                            helpState = HelpState(
                                    helpListener,
                                    stringResource(R.string.help_sight_marks__delete_title),
                                    stringResource(R.string.help_sight_marks__delete_body),
                            ),
                            modifier = Modifier.testTag(DELETE_BUTTON.getTestTag())
                    )
                    CodexIconButton(
                            icon = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.general__reset_edits),
                            captionBelow = stringResource(R.string.general__reset_edits),
                            onClick = { listener(ResetClicked) },
                            helpState = HelpState(
                                    helpListener,
                                    stringResource(R.string.help_sight_marks__reset_title),
                                    stringResource(R.string.help_sight_marks__reset_body),
                            ),
                            modifier = Modifier.testTag(RESET_BUTTON.getTestTag())
                    )
                }
                CodexIconButton(
                        icon = Icons.Default.Check,
                        contentDescription = stringResource(R.string.general_save),
                        captionBelow = stringResource(R.string.general_save),
                        onClick = { listener(SaveClicked) },
                        enabled = state.isFormValid,
                        helpState = HelpState(
                                helpListener,
                                stringResource(R.string.help_sight_marks__save_title),
                                stringResource(R.string.help_sight_marks__save_body),
                        ),
                        modifier = Modifier.testTag(SAVE_BUTTON.getTestTag())
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
