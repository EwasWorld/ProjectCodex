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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailIntent.*
import eywa.projectcodex.model.SightMark
import java.util.*

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
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }
    // TODO_CURRENT Validation

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
                        title = R.string.sight_marks__sight,
                        currentValue = state.sightMark,
                        isError = state.sightMarkValidatorError != null,
                        placeholder = "2.3f",
                        testTag = SightMarkDetailTestTags.SIGHT,
                        onValueChanged = { listener(SightMarkUpdated(it ?: "")) },
                        helpListener = helpListener,
                        helpTitle = R.string.help_sight_marks__sight_title,
                        helpBody = R.string.help_sight_marks__sight_body,
                )
                if (state.sightMarkValidatorError != null) {
                    Text(
                            text = stringResource(state.sightMarkValidatorError),
                            style = CodexTypography.SMALL,
                            color = CodexTheme.colors.errorOnAppBackground,
                    )
                }
            }
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    NumberSetting(
                            clazz = String::class,
                            title = R.string.sight_marks__distance,
                            currentValue = state.distance,
                            isError = state.distanceValidatorError != null,
                            placeholder = "50",
                            testTag = SightMarkDetailTestTags.SIGHT,
                            onValueChanged = { listener(DistanceUpdated(it ?: "")) },
                            helpListener = helpListener,
                            helpTitle = R.string.help_sight_marks__distance_title,
                            helpBody = R.string.help_sight_marks__distance_body,
                    )
                    if (state.distanceValidatorError != null) {
                        Text(
                                text = stringResource(state.distanceValidatorError),
                                style = CodexTypography.SMALL,
                                color = CodexTheme.colors.errorOnAppBackground,
                        )
                    }
                }
                Text(
                        text = stringResource(
                                if (state.isMetric) R.string.units_meters_short else R.string.units_yards_short
                        ),
                        color = CodexTheme.colors.linkText,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { listener(ToggleIsMetric) }
                )
            }

            state.originalSightMark?.dateSet?.let { dateSet ->
                DataRow(
                        title = R.string.sight_marks__date_set,
                        text = DateTimeFormat.SHORT_DATE.format(dateSet),
                        helpListener = helpListener,
                        helpTitle = R.string.help_sight_marks__date_title,
                        helpBody = R.string.help_sight_marks__date_body,
                )
            }
            FlowRow(
                    mainAxisSpacing = CODEX_CHIP_SPACING,
                    mainAxisAlignment = FlowMainAxisAlignment.Center,
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
            ) {
                // TODO_CURRENT Help info
                CodexChip(
                        text = stringResource(R.string.sight_marks__marked),
                        selected = state.isMarked,
                        testTag = SightMarkDetailTestTags.MARKED,
                        onToggle = { listener(ToggleIsMarked) },
                )
                CodexChip(
                        text = stringResource(R.string.sight_marks__archived),
                        selected = state.isArchived,
                        testTag = SightMarkDetailTestTags.ARCHIVED,
                        onToggle = { listener(ToggleIsArchived) },
                )
            }
            CodexTextFieldRoundedSurface {
                // TODO_CURRENT Help info
                CodexTextField(
                        state = CodexTextFieldState(
                                text = state.note ?: "",
                                onValueChange = { listener(NoteUpdated(it)) },
                                testTag = SightMarkDetailTestTags.NOTE,
                        ),
                        placeholderText = stringResource(R.string.sight_marks__note_placeholder),
                        labelText = stringResource(R.string.sight_marks__note),
                        modifier = Modifier.padding(10.dp)
                )
            }
            FlowRow(
                    mainAxisSpacing = 5.dp,
                    mainAxisAlignment = FlowMainAxisAlignment.Center,
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
            ) {
                // TODO_CURRENT Help info
                if (state.originalSightMark != null) {
                    CodexIconButton(
                            icon = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.general_delete),
                            captionBelow = stringResource(R.string.general_delete),
                            onClick = { listener(DeleteClicked) },
                    )
                    CodexIconButton(
                            icon = Icons.Default.Close,
                            contentDescription = stringResource(R.string.general_cancel),
                            captionBelow = stringResource(R.string.general_cancel),
                            onClick = { listener(CancelClicked) },
                    )
                    CodexIconButton(
                            icon = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.general__reset_edits),
                            captionBelow = stringResource(R.string.general__reset_edits),
                            onClick = { listener(ResetClicked) },
                    )
                }
                CodexIconButton(
                        icon = Icons.Default.Check,
                        contentDescription = stringResource(R.string.general_save),
                        captionBelow = stringResource(R.string.general_save),
                        onClick = { listener(SaveClicked) },
                        enabled = state.isFormValid,
                )
            }
        }
    }
}

object SightMarkDetailTestTags {
    private const val PREFIX = "SIGHT_MARK_DETAIL_"

    const val SCREEN = "${PREFIX}SCREEN"
    const val SIGHT = "${PREFIX}SIGHT_INPUT"
    const val DISTANCE = "${PREFIX}DISTANCE_INPUT"
    const val DISTANCE_UNIT = "${PREFIX}DISTANCE_UNIT"
    const val DATE = "${PREFIX}DATE"
    const val MARKED = "${PREFIX}MARKED"
    const val ARCHIVED = "${PREFIX}ARCHIVED"
    const val NOTE = "${PREFIX}NOTE"
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
