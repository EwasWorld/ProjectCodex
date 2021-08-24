package eywa.projectcodex.components.about

import androidx.lifecycle.ViewModel
import eywa.projectcodex.common.utils.UpdateDefaultRounds

/**
 * @see InputEndViewModel
 */
class AboutViewModel : ViewModel() {
    val updateDefaultRoundsState = UpdateDefaultRounds.taskProgress.getState()
    val updateDefaultRoundsProgressMessage = UpdateDefaultRounds.taskProgress.getMessage()
}