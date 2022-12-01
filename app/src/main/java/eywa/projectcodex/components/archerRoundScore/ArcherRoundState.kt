package eywa.projectcodex.components.archerRoundScore

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.archeryObjects.FullArcherRoundInfo
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.components.archerRoundScore.ArcherRoundScreen.INPUT_END
import eywa.projectcodex.components.archerRoundScore.ArcherRoundScreen.INSERT_END
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadDataNew

sealed class ArcherRoundState {
    open val showNavBar: Boolean = false

    data class Loading(
            val currentScreen: ArcherRoundScreen? = null,
            val fullArcherRoundInfo: FullArcherRoundInfo? = null,
    ) : ArcherRoundState()

    data class Loaded(
            val currentScreen: ArcherRoundScreen,
            val fullArcherRoundInfo: FullArcherRoundInfo,
            val goldsType: GoldsType =
                    fullArcherRoundInfo.round?.let { GoldsType.getGoldsType(it) } ?: GoldsType.defaultGoldsType,
            val inputEndSize: Int = 6,
            val scorePadEndSize: Int = 6,
            val scorePadSelectedRow: Int? = null,
            val scorePadDropdownOpenForEndNumber: Int? = null,
            val inputArrows: List<Arrow> = listOf(),
            val subScreenInputArrows: List<Arrow> = listOf(),
            val displayRoundCompletedDialog: Boolean = false,
    ) : ArcherRoundState() {
        val scorePadData by lazy { ScorePadDataNew(fullArcherRoundInfo, scorePadEndSize, goldsType) }

        override val showNavBar: Boolean = currentScreen.isMainScreen

        val scorePadSelectedRowFirstArrowNumber by lazy { scorePadEndSize * (scorePadSelectedRow!! - 1) }

        /**
         * The end size for the [currentScreen].
         *
         * Capped based on [FullArcherRoundInfo.round] if appropriate:
         * No cap if [displayRoundCompletedDialog] is true
         * - [ArcherRoundScreen.INPUT_END] caps at arrows remaining for the current distance
         * - [ArcherRoundScreen.INSERT_END] caps at arrows remaining
         */
        val currentScreenEndSize by lazy {
            val endSize = if (currentScreen == INPUT_END) inputEndSize else scorePadEndSize
            val maxArrows = when {
                displayRoundCompletedDialog -> null
                fullArcherRoundInfo.round == null -> null
                currentScreen == INPUT_END -> fullArcherRoundInfo.remainingArrowsAtDistances!!.first().first
                currentScreen == INSERT_END -> fullArcherRoundInfo.remainingArrows!!
                else -> null
            } ?: return@lazy endSize
            minOf(maxArrows.coerceAtLeast(0), endSize).takeIf { it > 0 }!!
        }

        val currentScreenInputArrows by lazy {
            if (!currentScreen.isMainScreen) subScreenInputArrows else inputArrows
        }

        /**
         * @return a copy of this where either [inputArrows] or [subScreenInputArrows]
         * has been replaced with [newInputArrows], depending on [ArcherRoundScreen.isMainScreen]
         */
        fun copy(newInputArrows: List<Arrow>) =
                if (!currentScreen.isMainScreen) copy(subScreenInputArrows = newInputArrows)
                else copy(inputArrows = newInputArrows)
    }
}

enum class ArcherRoundScreen(val bottomNavItemInfo: ArcherRoundBottomNavItemInfo? = null) {
    INPUT_END(
            ArcherRoundBottomNavItemInfo(
                    notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_add_box_filled),
                    selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_add_box_outline),
                    label = R.string.input_end__title,
            )
    ),
    SCORE_PAD(
            ArcherRoundBottomNavItemInfo(
                    notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_assignment_filled),
                    selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_assignment_outline),
                    label = R.string.score_pad__title,
            )
    ),
    STATS(
            ArcherRoundBottomNavItemInfo(
                    notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_chart_filled),
                    selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_chart_outline),
                    label = R.string.archer_round_stats__title,
            )
    ),
    SETTINGS(
            ArcherRoundBottomNavItemInfo(
                    notSelectedIcon = CodexIconInfo.VectorIcon(Icons.Outlined.Settings),
                    selectedIcon = CodexIconInfo.VectorIcon(Icons.Filled.Settings),
                    label = R.string.archer_round_settings__title,
            )
    ),

    EDIT_END,
    INSERT_END,
    ;

    val isMainScreen = bottomNavItemInfo == null
}

data class ArcherRoundBottomNavItemInfo(
        val notSelectedIcon: CodexIconInfo,
        val selectedIcon: CodexIconInfo? = null,
        @StringRes val label: Int,
)