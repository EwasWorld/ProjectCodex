package eywa.projectcodex.common.sharedUi.selectRoundFaceDialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.DialogPreviewHelper
import eywa.projectcodex.common.sharedUi.OutlinedButton
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexThemeColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogIntent.*
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogTestTag.*
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.getDistanceUnitRes


/**
 * The text to display on the [DataRow] for selecting [RoundFace]s
 */
@Composable
fun SelectFaceRow(
        selectedFaces: List<RoundFace>?,
        modifier: Modifier = Modifier,
        helpListener: (HelpShowcaseIntent) -> Unit,
        onClick: (() -> Unit)?,
) {
    val text = if (selectedFaces == null || selectedFaces.size < 2 || selectedFaces.distinct().size == 1) {
        stringResource((selectedFaces?.firstOrNull() ?: RoundFace.FULL).text)
    }
    else {
        val context = LocalContext.current
        selectedFaces.joinToString { context.resources.getString(it.text) }
    }

    DataRow(
            title = stringResource(R.string.create_round__select_a_face_title),
            text = text,
            helpState = HelpShowcaseItem(
                    helpTitle = stringResource(R.string.help_create_round__face_title),
                    helpBody = stringResource(R.string.help_create_round__face_body),
            ).asHelpState(helpListener),
            onClick = onClick,
            textModifier = Modifier.testTag(ROW_TEXT),
            modifier = modifier
    )
}

@Composable
fun SelectRoundFaceDialog(
        state: SelectRoundFaceDialogState,
        helpListener: (HelpShowcaseIntent) -> Unit,
        listener: (SelectRoundFaceDialogIntent) -> Unit,
) {
    if (state.round != null && !state.round.isMetric && state.round.isOutdoor) return

    val count = state.distances?.takeIf { it.isNotEmpty() }?.size ?: 1
    val pluralCount = if (state.isSingleMode) 1 else count

    SelectFaceRow(
            selectedFaces = state.selectedFaces,
            helpListener = helpListener,
    ) { listener(Open) }

    SimpleDialog(
            isShown = state.isShown,
            onDismissListener = { listener(Close) },
    ) {
        SimpleDialogContent(
                title = pluralStringResource(R.plurals.create_round__select_a_face_dialog_title, pluralCount),
                message = stringResource(R.string.create_round__select_a_face_subtitle)
                        .takeIf { state.isSingleMode && count > 1 },
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener(Close) },
                ).takeIf { state.isSingleMode },
                positiveButton = ButtonState(
                        text = stringResource(R.string.general_complete),
                        onClick = { listener(Close) },
                ).takeIf { !state.isSingleMode },
                modifier = Modifier.testTag(DIALOG)
        ) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // TODO_CURRENT Face type help - need a way to show a single help showcase
//                CodexButton(
//                        text = stringResource(R.string.create_round__face_type_help_button),
//                        buttonStyle = object : TextButton() {
//                            override fun getTextColor(themeColors: CodexThemeColors): Color =
//                                    themeColors.dialogNegativeText
//
//                            override val textStyle: TextStyle
//                                get() = CodexTypography.NORMAL
//                                        .copy(fontStyle = FontStyle.Italic, textDecoration = TextDecoration.Underline)
//                        },
//                        onClick = { listener(FaceTypeHelpClicked) },
//                )
//                Spacer(modifier = Modifier.height(7.dp))

                Selectors(state, listener)
                Spacer(modifier = Modifier.height(10.dp))

                if (count > 1) {
                    CodexButton(
                            text = stringResource(
                                    if (state.isSingleMode) R.string.create_round__select_a_face_all_diff
                                    else R.string.create_round__select_a_face_all_same,
                            ),
                            buttonStyle = object : OutlinedButton() {
                                override fun getTextColor(themeColors: CodexThemeColors): Color =
                                        themeColors.filledButton

                                override val textStyle: TextStyle
                                    get() = CodexTypography.SMALL
                            },
                            onClick = { listener(ToggleSingleMode) },
                            modifier = Modifier
                                    .padding(vertical = 5.dp)
                                    .testTag(
                                            if (state.isSingleMode) SWITCH_TO_MULTI_BUTTON
                                            else SWITCH_TO_SINGLE_BUTTON,
                                    )
                    )
                }
            }
        }
    }
}

@Composable
private fun Selectors(
        state: SelectRoundFaceDialogState,
        listener: (SelectRoundFaceDialogIntent) -> Unit,
) {
    val availableFaces =
            if (state.round == null) RoundFace.entries
            else RoundFace.entries.filter { it.shouldShow(state.round) }

    ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onDialogBackground)) {
        if (state.isSingleMode || state.distances == null || state.distances.size < 2) {
            availableFaces.forEach {
                Text(
                        text = stringResource(it.text),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                                .clickable { listener(SingleFaceClicked(it)) }
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .testTag(SINGLE_OPTION)
                )
            }
        }
        else {
            IndividualSelectors(
                    availableFaces = availableFaces,
                    selectedFaces = state.selectedFaces,
                    round = state.round,
                    distances = state.distances,
                    dropdownExpandedFor = state.dropdownExpandedFor,
                    listener = listener,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IndividualSelectors(
        availableFaces: List<RoundFace>,
        selectedFaces: List<RoundFace>?,
        round: Round?,
        distances: List<Int>,
        dropdownExpandedFor: Int? = null,
        listener: (SelectRoundFaceDialogIntent) -> Unit,
) {
    val distanceUnit = stringResource(round.getDistanceUnitRes()!!)
    val facesActual =
            if (selectedFaces.orEmpty().size > 1) selectedFaces
            else List(distances.size) { selectedFaces?.firstOrNull() }

    distances.sortedByDescending { it }.forEachIndexed { index, distance ->
        val title = "$distance$distanceUnit:"
        val face = stringResource((facesActual?.getOrNull(index) ?: RoundFace.FULL).text)


        DataRow(
                title = title,
                helpState = null,
                modifier = Modifier
                        .padding(vertical = 8.dp)
                        .testTag(MULTI_OPTION)
                        .clearAndSetSemantics {
                            contentDescription = "$title $face"
                            role = Role.DropdownList
                            onClick { listener(OpenDropdown(index)); true }
                        }
        ) {
            Text(
                    text = face,
                    style = LocalTextStyle.current.asClickableStyle(),
                    modifier = Modifier
                            .clickable { listener(OpenDropdown(index)) }
                            .align(Alignment.CenterVertically)
            )
            DropdownMenu(
                    expanded = dropdownExpandedFor == index,
                    onDismissRequest = { listener(CloseDropdown) },
            ) {
                availableFaces.forEach {
                    DropdownMenuItem(
                            onClick = { listener(DropdownItemClicked(it)) },
                    ) {
                        Text(
                                text = stringResource(it.text),
                                modifier = Modifier.testTag(MULTI_DROPDOWN_OPTION)
                        )
                    }
                }
            }
        }
    }
}

enum class SelectRoundFaceDialogTestTag : CodexTestTag {
    DIALOG,
    ROW_TEXT,
    SWITCH_TO_MULTI_BUTTON,
    SWITCH_TO_SINGLE_BUTTON,
    SINGLE_OPTION,
    MULTI_OPTION,
    MULTI_DROPDOWN_OPTION,
    ;

    override val screenName: String
        get() = "SELECT_ROUND_FACE_DIALOG"

    override fun getElement(): String = name
}

@Preview
@Composable
fun RoundSingle_SelectRoundFaceDialog_Preview() {
    DialogPreviewHelper {
        SelectRoundFaceDialog(
                SelectRoundFaceDialogState(
                        isShown = true,
                        isSingleMode = true,
                        distances = listOf(70, 60, 50, 30),
                        selectedFaces = listOf(RoundFace.FULL, RoundFace.FULL, RoundFace.HALF, RoundFace.HALF),
                        round = Round(roundId = 1, name = "", displayName = "", isOutdoor = true, isMetric = true),
                        dropdownExpandedFor = null,
                ),
                helpListener = {},
        ) {}
    }
}

@Preview
@Composable
fun RoundMulti_SelectRoundFaceDialog_Preview() {
    DialogPreviewHelper {
        SelectRoundFaceDialog(
                SelectRoundFaceDialogState(
                        isShown = true,
                        isSingleMode = false,
                        distances = listOf(70, 60, 50, 30),
                        selectedFaces = listOf(RoundFace.FULL, RoundFace.FULL, RoundFace.HALF, RoundFace.HALF),
                        round = Round(roundId = 1, name = "", displayName = "", isOutdoor = true, isMetric = true),
                        dropdownExpandedFor = null,
                ),
                helpListener = {},
        ) {}
    }
}

@Preview
@Composable
fun Worcester_SelectRoundFaceDialog_Preview() {
    DialogPreviewHelper {
        SelectRoundFaceDialog(
                SelectRoundFaceDialogState(
                        isShown = true,
                        isSingleMode = false,
                        distances = listOf(20),
                        selectedFaces = null,
                        round = Round(
                                roundId = 1, name = "", displayName = "", isOutdoor = false,
                                isMetric = false, defaultRoundId = 22,
                        ),
                        dropdownExpandedFor = null,
                ),
                helpListener = {},
        ) {}
    }
}

@Preview
@Composable
fun NoRound_SelectRoundFaceDialog_Preview() {
    DialogPreviewHelper {
        SelectRoundFaceDialog(
                SelectRoundFaceDialogState(
                        isShown = true,
                        isSingleMode = false,
                        distances = null,
                        selectedFaces = null,
                        round = null,
                        dropdownExpandedFor = null,
                ),
                helpListener = {},
        ) {}
    }
}
