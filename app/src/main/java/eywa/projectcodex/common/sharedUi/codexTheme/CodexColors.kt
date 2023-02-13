package eywa.projectcodex.common.sharedUi.codexTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.components.archerRoundScore.arrowInputs.arrowButton.ArrowButtonGroup
import eywa.projectcodex.components.viewScores.ui.MultiSelectBarListener
import eywa.projectcodex.components.viewScores.ui.ViewScoresMultiSelectBar
import eywa.projectcodex.components.viewScores.utils.ConvertScoreType

val LocalCodexThemeColors = staticCompositionLocalOf { CodexThemeColors() }

@Immutable
data class CodexThemeColors(
        val unassignedColor: Color = Color.Transparent,

        val appBackground: Color = CodexColors.COLOR_PRIMARY,
        val onAppBackground: Color = Color.White,
        val warningOnAppBackground: Color = CodexColors.WARNING_TEXT,
        val errorOnAppBackground: Color = CodexColors.ERROR_TEXT,

        val listItemOnAppBackground: Color = CodexColors.COLOR_LIGHT_ACCENT,
        val listAccentRowItemOnAppBackground: Color = Color(0xFFC4FFF6),
        val listItemOnAppBackgroundBorder: Color = CodexColors.COLOR_PRIMARY_DARK,
        val onListItemAppOnBackground: Color = Color.Black,

        val surfaceOnBackground: Color = CodexColors.COLOR_EXTRA_LIGHT_ACCENT,
        val onSurfaceOnBackground: Color = Color.Black,
        val disabledOnSurfaceOnBackground: Color = Color.LightGray.copy(alpha = 0.5f),
        val textFieldFocussedOutline: Color = CodexColors.COLOR_ACCENT,
        val textFieldUnfocussedOutline: Color = CodexColors.COLOR_PRIMARY.copy(alpha = 0.5f),

        val bottomNavBar: Color = CodexColors.COLOR_PRIMARY_DARK,
        val onBottomNavBar: Color = Color.White,

        // Dialogs
        val dialogBackground: Color = Color.White,
        val onDialogBackground: Color = Color.Black,
        val dialogRadioButton: Color = CodexColors.COLOR_PRIMARY,
        val dialogPositiveText: Color = CodexColors.COLOR_PRIMARY,
        val dialogNegativeText: Color = Color.Black.copy(alpha = 0.55f),

        val helpShowcaseScrim: Color = CodexColors.COLOR_PRIMARY_DARK.copy(alpha = 0.9f),
        val helpShowcaseTitle: Color = CodexColors.COLOR_LIGHT_ACCENT,
        val helpShowcaseMessage: Color = Color.White.copy(alpha = 0.7f),
        val helpShowcaseButton: Color = Color.White,

        // Buttons
        val linkText: Color = CodexColors.COLOR_PRIMARY_DARK, // Text string that's been turned into a link
        val disabledButton: Color = Color.LightGray.copy(alpha = 0.7f),
        val onDisabledButton: Color = Color.Gray.copy(alpha = 0.8f),
        val filledButton: Color = CodexColors.COLOR_PRIMARY_DARK,
        val onFilledButton: Color = Color.White,
        val floatingActions: Color = CodexColors.COLOR_PRIMARY_DARK,
        val onFloatingActions: Color = Color.White,
        val textButtonOnPrimary: Color = Color.Black,

        // Chips
        val chipOnPrimarySelected: Color = CodexColors.COLOR_PRIMARY_LIGHT,
        val chipOnPrimarySelectedText: Color = CodexColors.COLOR_ON_PRIMARY_LIGHT,
        val chipOnPrimaryUnselected: Color = Color.White,
        val chipOnDialogSelected: Color = CodexColors.COLOR_PRIMARY_LIGHT,
        val chipOnDialogSelectedText: Color = CodexColors.COLOR_ON_PRIMARY_LIGHT,
        val chipOnDialogUnselected: Color = CodexColors.COLOR_ON_PRIMARY_LIGHT,

        // Pill selector
        val pillSelectorSelected: Color = CodexColors.COLOR_PRIMARY_DARK,
        val onPillSelectorSelected: Color = Color.White,
        val pillSelectorNotSelected: Color = Color.White,
        val onPillSelectorNotSelected: Color = Color.Black,
        val pillSelectorBorder: Color = Color.LightGray,

        val targetFaceGreen: Color = CodexColors.TARGET_FACE_GREEN,
        val onTargetFaceGreen: Color = Color.Black,
        val targetFaceWhite: Color = CodexColors.TARGET_FACE_WHITE,
        val onTargetFaceWhite: Color = Color.Black,
        val targetFaceBlack: Color = CodexColors.TARGET_FACE_BLACK,
        val onTargetFaceBlack: Color = Color.White,
        val targetFaceBlue: Color = CodexColors.TARGET_FACE_BLUE,
        val onTargetFaceBlue: Color = Color.Black,
        val targetFaceRed: Color = CodexColors.TARGET_FACE_RED,
        val onTargetFaceRed: Color = Color.Black,
        val targetFaceGold: Color = CodexColors.TARGET_FACE_GOLD,
        val onTargetFaceGold: Color = Color.Black,

        )

object CodexColors {
    val COLOR_PRIMARY = Color(Raw.COLOR_PRIMARY)
    val COLOR_PRIMARY_LIGHT = Color(0xFFB3EAFF)
    val COLOR_ON_PRIMARY_LIGHT = Color(0xFF136E91)
    val COLOR_LIGHT_ACCENT = Color(Raw.COLOR_LIGHT_ACCENT)
    val COLOR_EXTRA_LIGHT_ACCENT = Color(0xFFE2F7FF)
    val COLOR_ACCENT = Color(0xFF5FEFB3)
    val COLOR_ON_ACCENT = Color(0xFF0A613D)
    val COLOR_PRIMARY_DARK = Color(0xFF14248F)
    val COLOR_PRIMARY_DARK_TRANSPARENT = Color(0xDA14248F)
    val COLOR_ACCENT_DARK = Color(0xFF317882)
    val COLOR_TERTIARY = Color(0xFF59D6C1)

    val COLOR_PRIMARY_PINK = Color(0xFFFF69FC)

    val OFF_BLACK = Color(0xFF242424)
    val GREY = Color(0xFF8F8F8F)
    val OFF_WHITE = Color(0xFFBCBCBC)

    val SCORE_PAD_TEXT = OFF_BLACK
    val INPUT_END_TEXT = Color.White
    val WARNING_TEXT = Color(0xFFFFF176)
    val ERROR_TEXT = Color(0xFFF44336)

    val TARGET_FACE_GREEN = Color(0xFF26FF00)
    val TARGET_FACE_WHITE = Color.White
    val TARGET_FACE_BLACK = Color.Black
    val TARGET_FACE_BLUE = Color(0xFF0099FF)
    val TARGET_FACE_RED = Color.Red
    val TARGET_FACE_GOLD = Color(0xFFFFDD00)

    object Raw {
        const val COLOR_PRIMARY = 0xFF69BEFF
        const val COLOR_LIGHT_ACCENT = 0xFFCCF1FF
    }
}

// TODO Create a showcase fragment for this that's only visible in debug
@Preview(
        heightDp = 1300,
        widthDp = 480,
)
@Composable
fun CodexTheme_Preview(@PreviewParameter(CodexThemePreviewProvider::class) theme: AppTheme) {
    CodexTheme(theme) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(30.dp),
                modifier = Modifier
                        .fillMaxSize()
                        .background(CodexTheme.colors.appBackground)
                        .padding(20.dp)
        ) {
            Text(
                    text = "Text on background",
                    style = CodexTypography.NORMAL,
                    color = CodexTheme.colors.onAppBackground,
            )

            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                CodexButton(text = "Default button", buttonStyle = CodexButtonDefaults.DefaultButton()) {}
                CodexButton(text = "Text button", buttonStyle = CodexButtonDefaults.DefaultTextButton) {}
            }

            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                CodexChip(text = "Chip 1", selected = true, testTag = "") {}
                CodexChip(text = "Chip 2", selected = false, testTag = "") {}
                CodexChip(text = "Chip 3", selected = true, enabled = false, testTag = "") {}
                CodexChip(text = "Chip 4", selected = false, enabled = false, testTag = "") {}
            }

            Box {
                Column {
                    repeat(3) {
                        Surface(
                                color = CodexTheme.colors.listItemOnAppBackground,
                                modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(3.dp)
                        ) {
                            Column(
                                    modifier = Modifier.padding(5.dp)
                            ) {
                                Text(
                                        text = "Title",
                                        style = CodexTypography.NORMAL,
                                        color = CodexTheme.colors.onListItemAppOnBackground,
                                )
                                Text(
                                        text = "Content",
                                        style = CodexTypography.NORMAL,
                                        color = CodexTheme.colors.onListItemAppOnBackground,
                                )
                            }
                        }
                    }
                }
                ViewScoresMultiSelectBar(
                        listener = object : MultiSelectBarListener {
                            override fun selectAllOrNoneClicked() {}
                            override fun multiSelectEmailClicked() {}
                            override fun toggleMultiSelectMode() {}
                            override fun addHelpShowcase(item: HelpShowcaseItem) {}
                            override fun updateHelpDialogPosition(
                                    helpTitle: Int, layoutCoordinates: LayoutCoordinates
                            ) {
                            }
                        },
                        isInMultiSelectMode = false,
                        isEveryItemSelected = false,
                        modifier = Modifier.align(Alignment.BottomCenter),
                )
            }

            Row {
                repeat(5) { columnIndex ->
                    Column {
                        repeat(6) { rowIndex ->
                            val color = when {
                                rowIndex in listOf(0, 3, 5) || columnIndex == 0 ->
                                    CodexTheme.colors.listAccentRowItemOnAppBackground
                                else -> CodexTheme.colors.listItemOnAppBackground
                            }
                            Text(
                                    text = "XXX",
                                    style = CodexTypography.NORMAL,
                                    color = CodexTheme.colors.onListItemAppOnBackground,
                                    modifier = Modifier
                                            .padding(2.dp)
                                            .background(color)
                                            .padding(5.dp)
                            )
                        }
                    }
                }
            }

            RadioButtonDialogContent(
                    title = R.string.view_score__convert_score_dialog_title,
                    message = R.string.view_score__convert_score_dialog_body,
                    positiveButtonText = R.string.general_ok,
                    onPositiveButtonPressed = {},
                    negativeButton = ButtonState(stringResource(R.string.general_cancel)) {},
                    state = rememberRadioButtonDialogState(items = ConvertScoreType.values().toList())
            )

            ArrowButtonGroup(null) {}
        }
    }
}

class CodexThemePreviewProvider : CollectionPreviewParameterProvider<AppTheme>(AppTheme.values().toList())
