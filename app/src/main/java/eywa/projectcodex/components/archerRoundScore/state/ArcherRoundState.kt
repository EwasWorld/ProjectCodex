package eywa.projectcodex.components.archerRoundScore.state

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.archeryObjects.FullArcherRoundInfo
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.components.archerRoundScore.ArcherRoundSubScreen
import eywa.projectcodex.components.archerRoundScore.arrowInputs.editEnd.EditEndScreen
import eywa.projectcodex.components.archerRoundScore.arrowInputs.editEnd.EditEndState
import eywa.projectcodex.components.archerRoundScore.arrowInputs.inputEnd.InputEndScreen
import eywa.projectcodex.components.archerRoundScore.arrowInputs.inputEnd.InputEndState
import eywa.projectcodex.components.archerRoundScore.arrowInputs.insertEnd.InsertEndScreen
import eywa.projectcodex.components.archerRoundScore.arrowInputs.insertEnd.InsertEndState
import eywa.projectcodex.components.archerRoundScore.scorePad.ScorePadScreen
import eywa.projectcodex.components.archerRoundScore.scorePad.ScorePadState
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadDataNew
import eywa.projectcodex.components.archerRoundScore.settings.ArcherRoundSettingsScreen
import eywa.projectcodex.components.archerRoundScore.settings.ArcherRoundSettingsState
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen.INPUT_END
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen.INSERT_END
import eywa.projectcodex.components.archerRoundScore.stats.ArcherRoundStatsScreen
import eywa.projectcodex.components.archerRoundScore.stats.ArcherRoundStatsState
import eywa.projectcodex.database.rounds.Round

private const val DEFAULT_END_SIZE = 6

sealed class ArcherRoundState {
    open val showNavBar: Boolean = false
    open val interruptBackButtonListener = false

    data class Loading(
            val currentScreen: ArcherRoundScreen? = null,
            val fullArcherRoundInfo: FullArcherRoundInfo? = null,
    ) : ArcherRoundState()

    // TODO Disable submit functions while waiting for a submit action to return
    data class Loaded(
            val currentScreen: ArcherRoundScreen,
            override val fullArcherRoundInfo: FullArcherRoundInfo,
            override val goldsType: GoldsType =
                    fullArcherRoundInfo.round?.let { GoldsType.getGoldsType(it) } ?: GoldsType.defaultGoldsType,
            override val inputEndSize: Int? = DEFAULT_END_SIZE,
            override val scorePadEndSize: Int? = DEFAULT_END_SIZE,
            val scorePadSelectedEnd: Int? = null,
            val inputArrows: List<Arrow> = listOf(),
            val subScreenInputArrows: List<Arrow> = listOf(),
            val displayRoundCompletedDialog: Boolean = false,
            override val displayDeleteEndConfirmationDialog: Boolean = false,
    ) : ArcherRoundState(), InputEndState, ArcherRoundStatsState, ScorePadState, ArcherRoundSettingsState, EditEndState,
            InsertEndState {
        override val scorePadData by lazy {
            ScorePadDataNew(
                    info = fullArcherRoundInfo,
                    endSize = scorePadEndSize ?: DEFAULT_END_SIZE,
                    goldsType = goldsType
            )
        }

        override val showNavBar: Boolean = currentScreen.isMainScreen

        override val interruptBackButtonListener: Boolean = !currentScreen.isMainScreen

        val scorePadSelectedEndFirstArrowNumber by lazy {
            // +1 because arrowNumbers are 1-indexed
            (scorePadEndSize ?: DEFAULT_END_SIZE) * (scorePadSelectedEnd!! - 1) + 1
        }

        /**
         * The end size for the [currentScreen].
         *
         * Capped based on [FullArcherRoundInfo.round] if appropriate:
         * No cap if [displayRoundCompletedDialog] is true
         * - [ArcherRoundScreen.INPUT_END] caps at arrows remaining for the current distance
         * - [ArcherRoundScreen.INSERT_END] caps at arrows remaining
         */
        val currentScreenEndSize by lazy {
            val endSize = (if (currentScreen == INPUT_END) inputEndSize else scorePadEndSize) ?: DEFAULT_END_SIZE
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

        override fun getEnteredArrows(): List<Arrow> = currentScreenInputArrows
        override fun getSelectedEndNumber(): Int = scorePadSelectedEnd!!

        override fun getEndSize(): Int = currentScreenEndSize
        override val isRoundFull: Boolean = (fullArcherRoundInfo.remainingArrows ?: 1) == 0
        override val dropdownMenuOpenForEndNumber: Int? =
                scorePadSelectedEnd.takeIf { !displayDeleteEndConfirmationDialog }

        override fun getRound(): Round? = fullArcherRoundInfo.round
    }
}

enum class ArcherRoundScreen(val bottomNavItemInfo: ArcherRoundBottomNavItemInfo? = null) {
    INPUT_END(
            ArcherRoundBottomNavItemInfo(
                    notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_add_box_outline),
                    selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_add_box_filled),
                    label = R.string.input_end__title,
            )
    ) {
        override fun getScreen() = InputEndScreen()
    },
    SCORE_PAD(
            ArcherRoundBottomNavItemInfo(
                    notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_assignment_outline),
                    selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_assignment_filled),
                    label = R.string.score_pad__title,
            )
    ) {
        override fun getScreen() = ScorePadScreen()
    },
    STATS(
            ArcherRoundBottomNavItemInfo(
                    notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_chart_outline),
                    selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_chart_filled),
                    label = R.string.archer_round_stats__title,
            )
    ) {
        override fun getScreen() = ArcherRoundStatsScreen()
    },
    SETTINGS(
            ArcherRoundBottomNavItemInfo(
                    notSelectedIcon = CodexIconInfo.VectorIcon(Icons.Outlined.Settings),
                    selectedIcon = CodexIconInfo.VectorIcon(Icons.Filled.Settings),
                    label = R.string.archer_round_settings__title,
            )
    ) {
        override fun getScreen() = ArcherRoundSettingsScreen()
    },

    EDIT_END {
        override fun getScreen() = EditEndScreen()
    },
    INSERT_END {
        override fun getScreen() = InsertEndScreen()
    },
    ;

    val isMainScreen = bottomNavItemInfo != null
    abstract fun getScreen(): ArcherRoundSubScreen
}

data class ArcherRoundBottomNavItemInfo(
        val notSelectedIcon: CodexIconInfo,
        val selectedIcon: CodexIconInfo? = null,
        @StringRes val label: Int,
)