package eywa.projectcodex.components.mainMenu

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcase
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import javax.inject.Inject

@HiltViewModel
class MainMenuViewModel @Inject constructor(
        private val helpShowcase: HelpShowcase,
) : ViewModel() {
    fun handle(action: HelpShowcaseIntent) = helpShowcase.handle(action, MainMenuFragment::class)
}
