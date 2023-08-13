package eywa.projectcodex.components.archerRoundScore.state

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.components.archerRoundScore.ArcherRoundSubScreen
import eywa.projectcodex.components.archerRoundScore.settings.ArcherRoundSettingsScreen
import eywa.projectcodex.components.archerRoundScore.settings.ArcherRoundSettingsState
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen.*
import eywa.projectcodex.components.archerRoundScore.stats.ArcherRoundStatsScreen
import eywa.projectcodex.components.archerRoundScore.stats.ArcherRoundStatsState
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.model.GoldsType
import eywa.projectcodex.model.ScorePadData

private const val DEFAULT_END_SIZE = 6

sealed class ArcherRoundState : HasBetaFeaturesFlag {
    data class InvalidArcherRoundError(
            val mainMenuClicked: Boolean = false,
    ) : ArcherRoundState() {
        override val useBetaFeatures: Boolean = false
    }

    data class Loading(
            val startingScreen: ArcherRoundScreen,
            override val useBetaFeatures: Boolean = DatastoreKey.UseBetaFeatures.defaultValue,
    ) : ArcherRoundState() {
        init {
            require(startingScreen.isMainScreen) { "Must navigate to a main screen" }
        }

        fun transitionToLoaded(fullShootInfo: FullShootInfo) =
                when {
                    // Cannot input an end into a completed round
                    // TODO Investigate
                    //  not sure how this state is possible but have had multiple crashes on Google play
                    (fullShootInfo.remainingArrows ?: 1) == 0 && startingScreen == INPUT_END -> {
                        Loaded(
                                startingScreen,
                                fullShootInfo,
                                useBetaFeatures = useBetaFeatures,
                                displayRoundCompletedDialog = true
                        )
                    }
                    else -> Loaded(startingScreen, fullShootInfo, useBetaFeatures = useBetaFeatures)
                }
    }

    // TODO Disable submit functions while waiting for a submit action to return
    data class Loaded(
            val currentScreen: ArcherRoundScreen,
            override val fullShootInfo: FullShootInfo,
            override val goldsType: GoldsType =
                    fullShootInfo.round?.let { GoldsType.getGoldsType(it) } ?: GoldsType.defaultGoldsType,
            override val inputEndSizePartial: Int? = DEFAULT_END_SIZE,
            override val scorePadEndSizePartial: Int? = DEFAULT_END_SIZE,
            val scorePadSelectedEnd: Int? = null,
            val inputArrows: List<Arrow> = listOf(),
            val subScreenInputArrows: List<Arrow> = listOf(),
            val displayRoundCompletedDialog: Boolean = false,
            val displayCannotInputEndDialog: Boolean = false,
            val displayDeleteEndConfirmationDialog: Boolean = false,
            override val useBetaFeatures: Boolean = DatastoreKey.UseBetaFeatures.defaultValue,
            val errors: Set<eywa.projectcodex.components.archerRoundScore.ArcherRoundError> = emptySet(),
            override val openEditScoreScreen: Boolean = false,
    ) : ArcherRoundState(), ArcherRoundStatsState, ArcherRoundSettingsState {
        val scorePadData by lazy {
            ScorePadData(
                    info = fullShootInfo,
                    endSize = scorePadEndSize,
                    goldsType = goldsType
            )
        }

        override val scorePadEndSize
            get() = scorePadEndSizePartial!!
        val endSize: Int
            get() = currentScreenEndSize

        val showNavBar
            get() = currentScreen.isMainScreen

        private val scorePadSelectedEndFirstArrowNumberAndEndSize by lazy {
            val all = scorePadData.data
                    .filterIsInstance<ScorePadData.ScorePadRow.End>()
                    .sortedBy { it.endNumber }
            val end = scorePadSelectedEnd!! - 1
            all.take(end)
                    .sumOf { it.arrowScores.size }
                    // +1 because arrowNumbers are 1-indexed
                    .plus(1) to all[end].arrowScores.size
        }

        val scorePadSelectedEndFirstArrowNumber
            get() = scorePadSelectedEndFirstArrowNumberAndEndSize.first
        val scorePadSelectedEndSize
            get() = scorePadSelectedEndFirstArrowNumberAndEndSize.second

        /**
         * The end size for the [currentScreen].
         *
         * Capped based on [FullShootInfo.round] if appropriate:
         * No cap if [displayRoundCompletedDialog] is true
         * - [ArcherRoundScreen.INPUT_END] caps at arrows remaining for the current distance
         * - [ArcherRoundScreen.INSERT_END] caps at arrows remaining
         */
        val currentScreenEndSize by lazy {
            val endSize = if (currentScreen == INPUT_END) inputEndSizePartial!! else scorePadEndSize
            val maxArrows = when {
                fullShootInfo.round == null || fullShootInfo.isRoundComplete -> endSize
                currentScreen == INSERT_END -> fullShootInfo.remainingArrows!!
                currentScreen == EDIT_END -> scorePadSelectedEndSize
                currentScreen == INPUT_END -> fullShootInfo.remainingArrowsAtDistances!!.first().first
                else -> throw IllegalStateException()
            }
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

        val enteredArrows
            get() = currentScreenInputArrows
        val selectedEndNumber
            get() = scorePadSelectedEnd!!

        val isRoundFull
            get() = fullShootInfo.isRoundComplete
        val dropdownMenuOpenForEndNumber
            get() = scorePadSelectedEnd
    }
}

enum class ArcherRoundScreen(val bottomNavItemInfo: ArcherRoundBottomNavItemInfo? = null) {
    INPUT_END(
            ArcherRoundBottomNavItemInfo(
                    notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_add_box_outline),
                    selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_add_box_filled),
                    label = R.string.input_end__title,
            )
    ),
    SCORE_PAD(
            ArcherRoundBottomNavItemInfo(
                    notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_assignment_outline),
                    selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_assignment_filled),
                    label = R.string.score_pad__title,
            )
    ),
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

    EDIT_END,
    INSERT_END,
    ;

    val isMainScreen = bottomNavItemInfo != null
    open fun getScreen(): ArcherRoundSubScreen? = null
}

data class ArcherRoundBottomNavItemInfo(
        val notSelectedIcon: CodexIconInfo,
        val selectedIcon: CodexIconInfo? = null,
        @StringRes val label: Int,
)
