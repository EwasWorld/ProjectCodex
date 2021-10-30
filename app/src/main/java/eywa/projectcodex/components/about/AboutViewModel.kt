package eywa.projectcodex.components.about

import androidx.lifecycle.ViewModel
import eywa.projectcodex.common.utils.UpdateDefaultRounds
import eywa.projectcodex.components.archerRoundScore.ArcherRoundScoreViewModel

/**
 * @see ArcherRoundScoreViewModel
 */
class AboutViewModel : ViewModel() {
    val updateDefaultRoundsState = UpdateDefaultRounds.taskProgress.getState()
    val updateDefaultRoundsProgressMessage = UpdateDefaultRounds.taskProgress.getMessage()
}