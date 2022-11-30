package eywa.projectcodex.components.archerRoundScore

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadDataNew
import eywa.projectcodex.database.archerRound.FullArcherRoundInfo

data class ArcherRoundState(
        val currentScreen: ArcherRoundScreen,
        val fullArcherRoundInfo: FullArcherRoundInfo,
        val goldsType: GoldsType,
        val inputEndSize: Int = 6,
        val scorePadEndSize: Int = 6,
        val isEditingEndNumber: Int? = null,
        val isInsertingEndNumber: Int? = null,
        val scorePadDropdownOpenForEndNumber: Int? = null,
        val inputArrows: List<Arrow> = listOf(),
) {
    val scorePadData by lazy { ScorePadDataNew(fullArcherRoundInfo, scorePadEndSize, goldsType) }

    val isOnSubScreen = isEditingEndNumber != null || isInsertingEndNumber != null
}

enum class ArcherRoundScreen(
        val notSelectedIcon: CodexIconInfo,
        val selectedIcon: CodexIconInfo? = null,
        @StringRes val label: Int,
) {
    INPUT_END(
            notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_add_box_filled),
            selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_add_box_outline),
            label = R.string.input_end__title,
    ),
    SCORE_PAD(
            notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_assignment_filled),
            selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_assignment_outline),
            label = R.string.score_pad__title,
    ),
    STATS(
            notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_chart_filled),
            selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_chart_outline),
            label = R.string.archer_round_stats__title,
    ),
    SETTINGS(
            notSelectedIcon = CodexIconInfo.VectorIcon(Icons.Outlined.Settings),
            selectedIcon = CodexIconInfo.VectorIcon(Icons.Filled.Settings),
            label = R.string.archer_round_settings__title,
    ),
}