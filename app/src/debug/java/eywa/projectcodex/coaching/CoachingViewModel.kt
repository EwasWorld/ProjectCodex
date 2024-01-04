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
        val crossHairParams: CoachingCrossHairParams? = null,
        val mode: CoachingMode = CoachingMode.SELECT_VIDEO,
)

sealed class CoachingIntent {
    data class VideoSelected(val uri: Uri?) : CoachingIntent()
    data class CrossHairParamsUpdated(
            val value: CoachingCrossHairParams? = null,
            val mode: CoachingMode? = null,
    ) : CoachingIntent()
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
                _state.update {
                    it.copy(
                            selectedImage = action.uri,
                            mode = CoachingMode.FIND_PLACE_IN_VIDEO,
                    )
                }
                player.setMediaItem(state.value.selectedImage?.let { MediaItem.fromUri(it) } ?: return)
            }

            is CoachingIntent.CrossHairParamsUpdated ->
                _state.update {
                    it.copy(
                            crossHairParams = action.value ?: it.crossHairParams,
                            mode = action.mode ?: it.mode,
                    )
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
