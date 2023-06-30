package eywa.projectcodex.common.helpShowcase

import androidx.annotation.StringRes
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIf
import eywa.projectcodex.common.utils.ResOrActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.reflect.KClass

fun Modifier.updateHelpDialogPosition(helpListener: (HelpShowcaseIntent) -> Unit, @StringRes key: Int) =
        onGloballyPositioned { helpListener(HelpShowcaseIntent.UpdateCoordinates(key, it)) }

fun Modifier.updateHelpDialogPosition(helpListener: (HelpShowcaseIntent) -> Unit, key: String) =
        onGloballyPositioned { helpListener(HelpShowcaseIntent.UpdateCoordinates(key, it)) }

fun Modifier.updateHelpDialogPosition(helpItemsMap: HelpShowcaseUseCase, @StringRes key: Int) =
        onGloballyPositioned { helpItemsMap.updateItem(key, it) }

fun Modifier.updateHelpDialogPosition(helpState: HelpState?) =
        modifierIf(helpState != null) { updateHelpDialogPosition(helpState!!.helpListener, helpState.helpTitle) }

class HelpShowcaseUseCase {
    private val _state = MutableStateFlow(HelpShowcaseState())
    val state = _state.asStateFlow()

    internal fun updateItem(@StringRes key: Int, layoutCoordinates: LayoutCoordinates) =
            updateItem(ResOrActual.fromRes(key), layoutCoordinates)

    internal fun updateItem(key: String, layoutCoordinates: LayoutCoordinates) =
            updateItem(ResOrActual.fromActual(key), layoutCoordinates)

    internal fun updateItem(key: ResOrActual<String>, layoutCoordinates: LayoutCoordinates) {
        _state.update {
            val item = it.helpInfoMap[key]?.copy(layoutCoordinates = layoutCoordinates)
                    ?: return@update it
            it.copy(helpInfoMap = it.helpInfoMap.plus(item.helpTitle to item))
        }
    }

    fun startShowcase(screen: ActionBarHelp?) {
        _state.update {
            if (screen == null || screen::class != it.currentScreen || it.helpInfoMap.isEmpty()) {
                return@update it.copy(startedButNoItems = true)
            }

            it.copy(
                    currentShowcase = it.helpInfoMap.values
                            .plus(it.dynamicHelpShowcaseInfo?.start() ?: emptyList())
                            .sortedBy { v -> v.priority }
                            .map { v -> v.helpTitle },
                    currentlyDisplayedIndex = 0,
            )
        }
    }

    fun nextShowcase() {
        _state.update {
            val nextIndex = it.currentlyDisplayedIndex?.plus(1)
            if (nextIndex == null || it.currentShowcase.isNullOrEmpty()) return@update it

            if (nextIndex in it.currentShowcase.indices) return@update it.copy(currentlyDisplayedIndex = nextIndex)

            it.dynamicHelpShowcaseInfo?.end()
            it.copy(currentShowcase = null, currentlyDisplayedIndex = null)
        }
    }

    fun endShowcase() {
        _state.update {
            it.dynamicHelpShowcaseInfo?.end()
            it.copy(currentShowcase = null, currentlyDisplayedIndex = null)
        }
    }

    fun clearNoShowcaseFlag() {
        _state.update { it.copy(startedButNoItems = false) }
    }

    fun handle(action: HelpShowcaseIntent, screen: KClass<out ActionBarHelp>? = null) {
        when (action) {
            is HelpShowcaseIntent.Add ->
                _state.update {
                    if (it.currentScreen != screen) return@update it
                    it.copy(helpInfoMap = it.helpInfoMap.plus(action.item.helpTitle to action.item))
                }
            is HelpShowcaseIntent.AddDynamicInfo -> {
                require(action.info.type == screen!!) { "Incorrect screen" }
                _state.update {
                    if (it.currentScreen != screen) return@update it
                    it.copy(dynamicHelpShowcaseInfo = action.info)
                }
            }
            HelpShowcaseIntent.Clear -> _state.update { it.copy(helpInfoMap = emptyMap()) }
            is HelpShowcaseIntent.Remove -> _state.update { it.copy(helpInfoMap = it.helpInfoMap.minus(action.key)) }
            is HelpShowcaseIntent.UpdateCoordinates -> updateItem(action.key, action.layoutCoordinates)
            is HelpShowcaseIntent.SetScreen -> _state.update {
                require(screen == null || action.screen == screen) { "Incorrect screen" }
                if (it.currentScreen == screen) return@update it
                HelpShowcaseState(currentScreen = action.screen)
            }
        }
    }

    companion object {
        fun combineContent(showcases: List<HelpShowcaseUseCase>): Map<ResOrActual<String>, HelpShowcaseItem> {
            val allStates = showcases.map { it.state.value }
            val screens = allStates.mapNotNull { it.currentScreen }.distinct()
            require(screens.size <= 1) { "Must all be the same screen" }
            return allStates.flatMap { s -> s.helpInfoMap.map { it.key to it.value } }.toMap()
        }
    }
}
