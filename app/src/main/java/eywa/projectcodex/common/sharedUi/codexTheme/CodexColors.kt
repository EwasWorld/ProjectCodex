package eywa.projectcodex.common.sharedUi.codexTheme

import androidx.compose.ui.graphics.Color

// TODO_CURRENT Add contextual colours to theme
object CodexColors {
    val COLOR_PRIMARY = Color(Raw.COLOR_PRIMARY)
    val COLOR_PRIMARY_LIGHT = Color(0xFFB3EAFF)
    val COLOR_LIGHT_ACCENT = Color(Raw.COLOR_LIGHT_ACCENT)
    val COLOR_EXTRA_LIGHT_ACCENT = Color(0xFFE2F7FF)
    val COLOR_ACCENT = Color(0xFF5FEFB3)
    val COLOR_PRIMARY_DARK = Color(0xFF14248F)
    val COLOR_PRIMARY_DARK_TRANSPARENT = Color(0xDA14248F)
    val COLOR_ACCENT_DARK = Color(0xFF317882)
    val COLOR_TERTIARY = Color(0xFF59D6C1)

    val COLOR_PRIMARY_PINK = Color(0xFFFF69FC)

    val BLACK = Color(0xFF000000)
    val OFF_BLACK = Color(0xFF242424)
    val GREY = Color(0xFF8F8F8F)
    val WHITE = Color(0xFFFFFFFF)
    val OFF_WHITE = Color(0xFFBCBCBC)

    val SCORE_PAD_TEXT = OFF_BLACK
    val INPUT_END_TEXT = WHITE
    val WARNING_TEXT = Color(0xFFD61A00)

    val TARGET_FACE_GREEN = Color(0xFF26FF00)
    val TARGET_FACE_WHITE = Color(0xFFFFFFFF)
    val TARGET_FACE_BLACK = Color(0xFF000000)
    val TARGET_FACE_BLUE = Color(0xFF0099FF)
    val TARGET_FACE_RED = Color(0xFFFF0000)
    val TARGET_FACE_GOLD = Color(0xFFFFDD00)

    object Raw {
        const val COLOR_PRIMARY = 0xFF69BEFF
        const val COLOR_LIGHT_ACCENT = 0xFFCCF1FF
    }
}