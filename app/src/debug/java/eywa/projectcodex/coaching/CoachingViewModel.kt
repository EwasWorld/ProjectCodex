package eywa.projectcodex.coaching

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CoachingState(
        val selectedImage: Uri? = null,
)

sealed class CoachingIntent {
    data class VideoSelected(val uri: Uri?) : CoachingIntent()
}

@HiltViewModel
class CoachingViewModel @Inject constructor(
        val player: Player
) : ViewModel() {
    private val _state = MutableStateFlow(CoachingState())
    val state = _state.asStateFlow()

    init {
        player.prepare()
    }

    fun handle(action: CoachingIntent) {
        when (action) {
            is CoachingIntent.VideoSelected -> {
                _state.update { it.copy(selectedImage = action.uri) }
                playVideo()
            }
        }
    }

    fun playVideo() {
        player.setMediaItem(state.value.selectedImage?.let { MediaItem.fromUri(it) } ?: return)
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
