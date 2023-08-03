package eywa.projectcodex.common.diActivityHelpers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ShootIdsUseCase {
    private val items: MutableStateFlow<List<Int>?> = MutableStateFlow(null)
    val getItems = items.asStateFlow()

    fun setItems(list: List<Int>) {
        items.update { list }
    }

    fun clear() {
        items.update { null }
    }
}