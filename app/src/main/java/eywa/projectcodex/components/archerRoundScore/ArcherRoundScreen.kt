package eywa.projectcodex.components.archerRoundScore

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import eywa.projectcodex.components.archerRoundScore.archerRoundStats.ArcherRoundStatsScreen
import eywa.projectcodex.components.archerRoundScore.inputEnd.ui.EditEndScreen
import eywa.projectcodex.components.archerRoundScore.inputEnd.ui.InputEndScreen
import eywa.projectcodex.components.archerRoundScore.inputEnd.ui.InsertEndScreen
import eywa.projectcodex.components.archerRoundScore.scorePad.ScorePadScreen
import eywa.projectcodex.components.archerRoundScore.settings.ArcherRoundSettingsScreen

@Composable
fun ArcherRoundScreen(
        state: ArcherRoundState,
        listener: (ArcherRoundIntent) -> Unit,
) {
    Column {
        when {
            state.isInsertingEndNumber != null -> InsertEndScreen(state, listener)
            state.isEditingEndNumber != null -> EditEndScreen(state, listener)
            else -> when (state.currentScreen) {
                ArcherRoundScreen.INPUT_END -> InputEndScreen(state, listener)
                ArcherRoundScreen.SCORE_PAD ->
                    ScorePadScreen(state.scorePadDropdownOpenForEndNumber, state.scorePadData, listener)
                ArcherRoundScreen.STATS -> ArcherRoundStatsScreen(state.fullArcherRoundInfo, state.goldsType)
                ArcherRoundScreen.SETTINGS -> ArcherRoundSettingsScreen(
                        state.inputEndSize,
                        state.scorePadEndSize,
                        listener
                )
            }
        }
        if (state.isOnSubScreen) {
            ArcherRoundBottomNavBar(state.currentScreen) { listener(ArcherRoundIntent.NavBarClicked(it)) }
        }
    }
}
