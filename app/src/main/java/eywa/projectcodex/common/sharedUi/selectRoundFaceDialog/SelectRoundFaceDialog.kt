package eywa.projectcodex.common.sharedUi.selectRoundFaceDialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexThemeColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogIntent.*
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.Round

/**
 * @param selectSingle use the same face for all distances (if multiple distances)
 * @param dropdownExpandedFor the index in [distances] (sorted descending) that the dropdown is expanded for.
 * Null if no dropdown expanded
 */
@Composable
fun SelectRoundFaceDialog(
        isShown: Boolean,
        selectSingle: Boolean,
        selectedFaces: List<RoundFace>?,
        round: Round?,
        distances: List<Int>?,
        dropdownExpandedFor: Int? = null,
        listener: (SelectRoundFaceDialogIntent) -> Unit,
) {
    val count = distances?.takeIf { it.isNotEmpty() }?.size ?: 1
    val pluralCount = if (selectSingle) 1 else count

    DataRow(
            title = stringResource(R.string.create_round__select_a_face_title),
            text = stringResource(getRowText(selectedFaces)),
            helpState = null,
            onClick = { listener(Open) },
    )

    SimpleDialog(
            isShown = isShown,
            onDismissListener = { listener(Close) },
    ) {
        SimpleDialogContent(
                title = pluralStringResource(R.plurals.create_round__select_a_face_dialog_title, pluralCount),
                message = stringResource(R.string.create_round__select_a_face_subtitle)
                        .takeIf { selectSingle && count > 1 },
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener(Close) },
                ).takeIf { selectSingle },
                positiveButton = ButtonState(
                        text = stringResource(R.string.general_complete),
                        onClick = { listener(Close) },
                ).takeIf { !selectSingle },
                modifier = Modifier.testTag(SelectRoundFaceDialogTestTag.DIALOG.getTestTag())
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

                Selectors(selectedFaces, selectSingle, round, distances, dropdownExpandedFor, listener)
                Spacer(modifier = Modifier.height(10.dp))

                if (count > 1) {
                    CodexButton(
                            text = stringResource(
                                    if (selectSingle) R.string.create_round__select_a_face_all_diff
                                    else R.string.create_round__select_a_face_all_same
                            ),
                            buttonStyle = object : OutlinedButton() {
                                override fun getTextColor(themeColors: CodexThemeColors): Color =
                                        themeColors.filledButton

                                override val textStyle: TextStyle
                                    get() = CodexTypography.SMALL
                            },
                            onClick = { listener(ToggleAllDifferentAllSame) },
                            modifier = Modifier.padding(vertical = 5.dp)
                    )
                }
            }
        }
    }
}

/**
 * The text to display on the [DataRow] for selecting [RoundFace]s
 */
private fun getRowText(selectedFaces: List<RoundFace>?) =
        if (selectedFaces == null || selectedFaces.size < 2 || selectedFaces.distinct().size == 1) {
            (selectedFaces?.firstOrNull() ?: RoundFace.FULL).text
        }
        else {
            R.string.create_round__select_a_face_various
        }

@Composable
private fun Selectors(
        selectedFaces: List<RoundFace>?,
        selectSingle: Boolean,
        round: Round?,
        distances: List<Int>?,
        dropdownExpandedFor: Int? = null,
        listener: (SelectRoundFaceDialogIntent) -> Unit,
) {
    val availableFaces =
            if (round == null) RoundFace.values().toList()
            else RoundFace.values().filter { it.shouldShow(round) }

    ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onDialogBackground)) {
        if (selectSingle || distances == null || distances.size < 2) {
            availableFaces.forEach {
                Text(
                        text = stringResource(it.text),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                                .clickable { listener(SingleFaceClicked(it)) }
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                )
            }
        }
        else {
            IndividualSelectors(
                    availableFaces = availableFaces,
                    selectedFaces = selectedFaces,
                    round = round,
                    distances = distances,
                    dropdownExpandedFor = dropdownExpandedFor,
                    listener = listener,
            )
        }
    }
}

@Composable
private fun IndividualSelectors(
        availableFaces: List<RoundFace>,
        selectedFaces: List<RoundFace>?,
        round: Round?,
        distances: List<Int>,
        dropdownExpandedFor: Int? = null,
        listener: (SelectRoundFaceDialogIntent) -> Unit,
) {
    val distanceUnit =
            stringResource(if (round!!.isMetric) R.string.units_meters_short else R.string.units_yards_short)
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
                        .clearAndSetSemantics {
                            contentDescription = "$title $face"
                            role = Role.DropdownList
                            onClick {
                                listener(OpenDropdown(index))
                                true
                            }
                        }
        ) {
            Text(
                    text = face,
                    style = LocalTextStyle.current.asClickableStyle(),
                    modifier = Modifier
                            .clearAndSetSemantics { }
                            .clickable { listener(OpenDropdown(index)) }
            )
            DropdownMenu(
                    expanded = dropdownExpandedFor == index,
                    onDismissRequest = { listener(CloseDropdown) },
            ) {
                availableFaces.forEach {
                    DropdownMenuItem(
                            onClick = { listener(DropdownItemClicked(it, index)) },
                    ) {
                        Text(
                                text = stringResource(it.text),
                        )
                    }
                }
            }
        }
    }
}

enum class SelectRoundFaceDialogTestTag : CodexTestTag {
    DIALOG,
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
                isShown = true,
                selectSingle = true,
                distances = listOf(70, 60, 50, 30),
                selectedFaces = listOf(RoundFace.FULL, RoundFace.FULL, RoundFace.HALF, RoundFace.HALF),
                round = Round(roundId = 1, name = "", displayName = "", isOutdoor = true, isMetric = true),
                dropdownExpandedFor = null,
        ) {}
    }
}

@Preview
@Composable
fun RoundMulti_SelectRoundFaceDialog_Preview() {
    DialogPreviewHelper {
        SelectRoundFaceDialog(
                isShown = true,
                selectSingle = false,
                distances = listOf(70, 60, 50, 30),
                selectedFaces = listOf(RoundFace.FULL, RoundFace.FULL, RoundFace.HALF, RoundFace.HALF),
                round = Round(roundId = 1, name = "", displayName = "", isOutdoor = true, isMetric = true),
                dropdownExpandedFor = null,
        ) {}
    }
}

@Preview
@Composable
fun Worcester_SelectRoundFaceDialog_Preview() {
    DialogPreviewHelper {
        SelectRoundFaceDialog(
                isShown = true,
                selectSingle = false,
                distances = listOf(20),
                selectedFaces = null,
                round = Round(
                        roundId = 1, name = "", displayName = "", isOutdoor = false,
                        isMetric = false, defaultRoundId = 22,
                ),
                dropdownExpandedFor = null,
        ) {}
    }
}

@Preview
@Composable
fun NoRound_SelectRoundFaceDialog_Preview() {
    DialogPreviewHelper {
        SelectRoundFaceDialog(
                isShown = true,
                selectSingle = false,
                distances = null,
                selectedFaces = null,
                round = null,
                dropdownExpandedFor = null,
        ) {}
    }
}
