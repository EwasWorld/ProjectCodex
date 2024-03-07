package eywa.projectcodex.common.helpShowcase

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent.Add
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent.Clear
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent.Remove
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent.SetScreen
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent.SetScreenSize
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent.SetVisibleScreenSize
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent.UpdateCoordinates
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIfNotNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.reflect.KClass

fun Modifier.updateHelpDialogPosition(helpState: HelpState?, id: Int = 0) =
        modifierIfNotNull(helpState) { state ->
            // Null pointer causing a crash, not sure how when modifierIf mean this is skipped when null
            state.add()
            Modifier.onGloballyPositioned {
                state.helpListener(UpdateCoordinates(state.helpShowcaseItem.helpTitle, it, id))
            }
        }

class HelpShowcaseUseCase(startScreen: KClass<out ActionBarHelp> = CodexNavRoute.MAIN_MENU::class) {
    private val _state = MutableStateFlow(HelpShowcaseInternalState(currentScreen = startScreen))
    val state = _state.map { it.asExternalState() }.distinctUntilChanged()

    internal fun updateItem(key: String, layoutCoordinates: LayoutCoordinates, id: Int) {
        _state.update {
            val old = it.helpInfoMap[key] ?: return@update it
            val item = old.copy(layoutCoordinates = old.layoutCoordinates.plus(id to layoutCoordinates))
            it.copy(helpInfoMap = it.helpInfoMap.plus(item.helpTitle to item))
        }
    }

    fun startShowcase(screen: ActionBarHelp?) {
        _state.update {
            if (screen == null || screen::class != it.currentScreen || it.helpInfoMap.isEmpty()) {
                return@update it.copy(startedButNoItems = true)
            }

            val newState = it.copy(
                    currentShowcase = it.helpInfoMap.values
                            .sortedBy { v -> v.priority }
                            .map { v -> v.helpTitle },
            )
            val index = newState.nextItemIndex ?: return@update it.copy(startedButNoItems = true)
            newState.copy(currentlyDisplayedIndex = index)
        }
    }

    fun nextShowcase() {
        _state.update {
            val nextIndex = it.nextItemIndex
                    ?: return@update it.copy(currentShowcase = null, currentlyDisplayedIndex = null)

            check(
                    !it.currentShowcase.isNullOrEmpty() && nextIndex in it.currentShowcase.indices
            ) { "Invalid index" }

            it.copy(currentlyDisplayedIndex = nextIndex)
        }
    }

    fun endShowcase() {
        _state.update { it.copy(currentShowcase = null, currentlyDisplayedIndex = null) }
    }

    fun clearNoShowcaseFlag() {
        _state.update { it.copy(startedButNoItems = false) }
    }

    fun handle(action: HelpShowcaseIntent, screen: KClass<out ActionBarHelp>? = null) {
        when (action) {
            is Add ->
                _state.update {
                    if (it.currentScreen != screen) return@update it
                    if (it.helpInfoMap.containsKey(action.item.helpTitle)) return@update it
                    it.copy(helpInfoMap = it.helpInfoMap.plus(action.item.helpTitle to action.item))
                }

            Clear -> _state.update { it.copy(helpInfoMap = emptyMap()) }
            is Remove -> _state.update { it.copy(helpInfoMap = it.helpInfoMap.minus(action.key)) }
            is UpdateCoordinates -> updateItem(action.key, action.layoutCoordinates, action.id)
            is SetScreen -> _state.update {
                require(screen == null || action.screen == screen) { "Incorrect screen" }
                if (it.currentScreen == screen) return@update it
                HelpShowcaseInternalState(
                        currentScreen = action.screen,
                        boundaries = emptyMap(),
                        screenSize = it.screenSize,
                )
            }

            is SetScreenSize -> _state.update { it.copy(screenSize = action.size) }
            is SetVisibleScreenSize -> _state.update {
                if (it.currentScreen != screen) return@update it
                it.copy(boundaries = it.boundaries.plus(action.id to action.boundary))
            }
        }
    }
}
