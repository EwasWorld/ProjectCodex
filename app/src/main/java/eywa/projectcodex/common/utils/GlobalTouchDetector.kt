package eywa.projectcodex.common.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object GlobalTouchDetector {
    private val _effects = MutableSharedFlow<Boolean>()
    val effects = _effects.asSharedFlow()

    suspend fun pressDetected() {
        _effects.emit(true)
    }

    suspend fun antiPressDetected() {
        _effects.emit(false)
    }
}
